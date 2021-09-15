package com.skilles.cannacraft.util;

import com.skilles.cannacraft.blocks.ImplementedInventory;
import com.skilles.cannacraft.blocks.weedCrop.WeedCropEntity;
import com.skilles.cannacraft.dna.genome.Genome;
import com.skilles.cannacraft.dna.genome.gene.TraitGene;
import com.skilles.cannacraft.misc.WeedToast;
import com.skilles.cannacraft.registry.ModEntities;
import com.skilles.cannacraft.registry.ModItems;
import com.skilles.cannacraft.registry.ModMisc;
import com.skilles.cannacraft.strain.Strain;
import com.skilles.cannacraft.strain.StrainInfo;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Rarity;
import net.minecraft.util.Util;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static com.skilles.cannacraft.Cannacraft.log;

/**
 * This class contains utilities for modifying strain attributes such as THC, name, and type
 */
public final class MiscUtil {

    private static final Random random =  new Random();

    public static Random random() {
        return random;
    }

    public static void appendTooltips(List<Text> tooltip, ItemStack stack, boolean shiftGenes) {
        NbtCompound tag = WeedRegistry.getStrainTag(stack);
        Genome genome = ModMisc.STRAIN.get(stack).getGenome();
        boolean identified = tag.getBoolean("Identified");
        StrainInfo info = DnaUtil.convertStrain(genome, identified);
        MinecraftClient client = MinecraftClient.getInstance();
        if (!ModMisc.PLAYER.get(client.player).isDiscovered(info.strain())) {
            client.getToastManager().add(new WeedToast(WeedToast.Type.DISCOVER, info.strain()));
        }
        String sex = info.male() ? "Male" : "Female";
        int id = info.strain().id();
        int thc = info.thc();
        List<TraitGene> genes = ModMisc.STRAIN.get(stack).getExpressed();
        if (identified) {
            tooltip.add(new LiteralText("Strain: ").formatted(Formatting.GRAY).append(new LiteralText(info.strain().name()).formatted(Formatting.GREEN)));
            tooltip.add(new LiteralText("Type: ").formatted(Formatting.GRAY).append(new LiteralText(StringUtils.capitalize(StringUtils.lowerCase(info.strain().type().name()))).formatted(Formatting.DARK_GREEN)));
            tooltip.add(new LiteralText("THC: ").formatted(Formatting.GRAY).append(new LiteralText(thc + "%").formatted(Formatting.DARK_GREEN)));
            Rarity rarity = info.strain().getRarity();
            tooltip.add(new LiteralText("Rarity: ").formatted(Formatting.GRAY).append(new LiteralText(StringUtils.capitalize(StringUtils.lowerCase(rarity.toString()))).formatted(rarity.formatting)));
            if (stack.isOf(ModItems.WEED_SEED)) {
                tooltip.add(new LiteralText("Sex: ").formatted(Formatting.GRAY).append(new LiteralText(sex).formatted(Formatting.DARK_GREEN)));
            }
            if (shiftGenes) {
                if (!genes.isEmpty()) {
                    tooltip.add(new LiteralText("Press ").append(new LiteralText("SHIFT ").formatted(Formatting.GOLD).append(new LiteralText("to view Genes").formatted(Formatting.WHITE))));
                }
                tooltip.add(new LiteralText("Press ").formatted(Formatting.GRAY).append(new LiteralText("CTRL ").formatted(Formatting.AQUA).append(new LiteralText("to view DNA").formatted(Formatting.GRAY))));
            }
        } else {
            tooltip.add(new LiteralText("Strain: ").formatted(Formatting.GRAY).append(new LiteralText("Unidentified").formatted(Formatting.GREEN)));
            tooltip.add(new LiteralText("Type: ").formatted(Formatting.GRAY).append(new LiteralText("Unknown").formatted(Formatting.DARK_GREEN)));
            if (stack.isOf(ModItems.WEED_SEED)) {
                tooltip.add(new LiteralText("Sex: ").formatted(Formatting.GRAY).append(new LiteralText("Unknown").formatted(Formatting.DARK_GREEN)));
            }
        }
    }

    /**
     * Drops an itemstack with NBT
     * TODO: use BE and check if fully grown before dropping bred seed
     */
    public static void dropStack(World world, BlockPos pos, Item type, boolean brokenWithShears) {
        if (world != null) {
            ItemStack toDrop = new ItemStack(type);
            if (world.getBlockEntity(pos) instanceof WeedCropEntity weedEntity) {
                NbtCompound tag = weedEntity.writeNbt(new NbtCompound());
                Genome genome = weedEntity.getGenome();
                if (type.equals(ModItems.WEED_SEED) && !genome.isMale()) {
                    toDrop.setSubNbt("cannacraft:strain", trimTag(tag, type));
                    Block.dropStack(world, pos, toDrop);
                } else if (brokenWithShears && type.equals(ModItems.WEED_BUNDLE)) {
                    toDrop.setSubNbt("cannacraft:strain", trimTag(tag, type));
                    Block.dropStack(world, pos, toDrop);
                } else {
                    log("Error: NULLTAG");
                }
            } else {
                log("Error: NULLBENTITY");
            }
        } else {
            log("Error: NULLWORLD");
        }
    }
    public static void dropStack(World world, BlockPos pos, Item type) {
        dropStack(world, pos, type, type.equals(ModItems.WEED_SEED));
    }
    public static void dropStack(WorldAccess world, BlockPos pos, Item type) {
        dropStack(world.getBlockEntity(pos).getWorld(), pos, type, true);
    }

    static int getWeight(Strain strain) {
        // TODO: add to config
        int weight;
        switch (strain.getRarity().ordinal()) {
            case 0 -> weight = 50;
            case 1 -> weight = 30;
            case 2 -> weight = 15;
            case 3 -> weight = 5;
            default -> throw new IllegalStateException("Unexpected value: " + strain.getRarity().ordinal());
        };
        if (strain.isResource()) weight /= 2;
        return weight;
    }

    /**
     * Format Block NBT to conform with ItemStack
     * @param tag block NBT tag
     * @param type type of format
     * @return tag with trimmed NBT
     */
    public static NbtCompound trimTag(NbtCompound tag, @Nullable Item type, boolean identified)  {
        NbtCompound newTag = new NbtCompound();
        newTag.putString("DNA", tag.getString("DNA"));
        newTag.putBoolean("Identified", identified);

        if (type != null) {
            if (type.equals(ModItems.WEED_BUNDLE)) {
                newTag.putFloat("Status", 1F);
            } else if (type.equals(ModItems.WEED_SEED)) {
                NbtList nbtList = tag.getList("Seed DNA", NbtElement.STRING_TYPE);
                newTag.putString("DNA", nbtList.get(0).asString());
            }
        }

        return newTag;
    }

    public static NbtCompound trimTag(NbtCompound tag, @Nullable Item type) {
        return trimTag(tag, type, tag.getBoolean("Identified"));
    }

    public static NbtCompound trimTag(NbtCompound tag) {
        return trimTag(tag, null);
    }

    /**
     * @param pos position of the block you want to check
     * @param blocks the block(s) to compare against
     * @param toGrow check if adjacent block has nothing above
     * @return null if no block was found, otherwise returns direction of the found block
     */
    @Deprecated
    public static @Nullable Direction isAdjacentTo(ServerWorld world, BlockPos pos, boolean toGrow, Block... blocks) {
        // TODO: select random direction from valid directions
        List<Direction> validDirections = new ArrayList<>();
        for (Direction direction : Direction.Type.HORIZONTAL) {
            BlockState neighborState = world.getBlockState(pos.offset(direction));
            Arrays.stream(blocks).filter(block -> neighborState.isOf(block) && (!toGrow || world.getBlockState(pos.offset(direction).up()).isOf(Blocks.AIR))).map(block -> direction).forEachOrdered(validDirections::add);
        }
        return validDirections.isEmpty() ? null : Util.getRandom(validDirections, random());
    }

    /**
     * @param parentPos position of the crop
     * @return null if not found
     */
    public static @Nullable BlockPos getValidSpreadPos(ServerWorld world, BlockPos parentPos, Random random) {
        List<BlockPos> validPosList = new ArrayList<>();
        for (Direction direction : Direction.Type.HORIZONTAL) {
            addValidPos(world, parentPos, validPosList, direction); // spread above
            addValidPos(world, parentPos.down(), validPosList, direction); // spread same level
            addValidPos(world, parentPos.down(2), validPosList, direction); // spread below
        }
        if (!validPosList.isEmpty()) return Util.getRandom(validPosList, random);
        return null;
    }
    private static void addValidPos(ServerWorld world, BlockPos parentPos, List<BlockPos> validPos, Direction direction) {
        BlockPos neighborPos = parentPos.offset(direction);
        BlockState neighborState = world.getBlockState(neighborPos);
        if ((neighborState.isOf(Blocks.GRASS_BLOCK) || neighborState.isOf(Blocks.FARMLAND) || neighborState.isOf(Blocks.DIRT)) && world.getBlockState(neighborPos.up()).isOf(Blocks.AIR)) {
            validPos.add(neighborPos.up());
        }
    }

    public static void copyNbt(ServerWorld world, BlockPos originalPos, BlockPos copyToPos) {
        if (world.getBlockEntity(originalPos) != null && world.getBlockEntity(copyToPos) != null) {
            world.getBlockEntity(copyToPos, ModEntities.WEED_CROP_ENTITY).get().readNbt(
                    world.getBlockEntity(originalPos, ModEntities.WEED_CROP_ENTITY).get().writeNbt(new NbtCompound()));
            world.markDirty(copyToPos);
        }
    }

    public static DefaultedList<ItemStack> getItemsFromNbt(ItemStack stack) {
        if (stack.isOf(ModItems.SEED_BAG) && stack.hasNbt()) {
            NbtCompound tag = stack.getNbt();
            assert tag != null;
            NbtList nbtList = tag.getList("Items", NbtElement.LIST_TYPE);
            List<ItemStack> itemList = new ArrayList<>();
            for (NbtElement element : nbtList) {
                NbtCompound compound = (NbtCompound) element;
                ItemStack seedStack = ModItems.WEED_SEED.getDefaultStack();
                NbtCompound bagTag = new NbtCompound();
                bagTag.putInt("ID", compound.getInt("ID"));
                bagTag.putInt("THC", compound.getInt("THC"));
                bagTag.put("Attrbiutes", compound.getList("Attributes", NbtElement.LIST_TYPE));
                seedStack.setSubNbt("cannacraft:strain", bagTag);
                itemList.add(seedStack);
            }
            return ImplementedInventory.of(DefaultedList.copyOf(ItemStack.EMPTY, itemList.toArray(new ItemStack[27]))).getItems();
        }
        return ImplementedInventory.ofSize(27).getItems();
    }

    public static Text getItemName(ItemStack stack) {
        if (stack.hasNbt()) {
            NbtCompound tag = stack.getSubNbt("cannacraft:strain");
            StrainInfo info = ModMisc.STRAIN.get(stack).getStrainInfo();
            String name = tag.getBoolean("Identified") ? info.strain().name() : "Unidentified";
            if (stack.isOf(ModItems.WEED_SEED)) {
                name += stack.getCount() > 1 ? " Seeds": " Seed";
            } else if (stack.isOf(ModItems.WEED_BROWNIE)) {
                name += stack.getCount() > 1 ? " Brownies": " Brownie";
            } else if (stack.isOf(ModItems.WEED_DISTILLATE)) {
                name += " Distillate";
            } else if (stack.isOf(ModItems.WEED_BUNDLE)) {

            }
            return Text.of(name);
        }
        return (Text) Text.EMPTY;
    }
    @Nullable
    public static ItemStack getCrosshairItem() {
        if (MinecraftClient.getInstance().crosshairTarget.getType().equals(HitResult.Type.ENTITY)) {
            if (((EntityHitResult) MinecraftClient.getInstance().crosshairTarget).getEntity() instanceof ItemEntity itemEntity) {
                return itemEntity.getStack();
            }
        }
        return null;
    }

    public static String[] splitEqual(String s, int parts) {
        int length = s.length(); //Get string length
        int whereToSplit; //store where will split

        if (length % 2 == 0) {
            whereToSplit = length / parts; //if length number is pair then it'll split equal
        } else {
            whereToSplit = (length + 1) / parts; //else the first value will have one char more than the other
        }

        return s.split("(?<=\\G.{" + whereToSplit + "})"); //split the string

    }
}
