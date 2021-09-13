package com.skilles.cannacraft.util;

import com.skilles.cannacraft.blocks.ImplementedInventory;
import com.skilles.cannacraft.blocks.weedCrop.WeedCropEntity;
import com.skilles.cannacraft.dna.genome.Genome;
import com.skilles.cannacraft.dna.genome.gene.TraitGene;
import com.skilles.cannacraft.registry.ModEntities;
import com.skilles.cannacraft.registry.ModItems;
import com.skilles.cannacraft.registry.ModMisc;
import com.skilles.cannacraft.strain.Gene;
import com.skilles.cannacraft.strain.GeneTypes;
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
import net.minecraft.util.Pair;
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
    public static ArrayList<Pair<GeneTypes, Integer>> geneArray = new ArrayList<>();
    private static final Random random =  new Random();
    public static Random random() {
        return random;
    }

    public static void appendTooltips(List<Text> tooltip, ItemStack stack, boolean shiftGenes) {
        NbtCompound tag = WeedRegistry.getStrainTag(stack);
        Genome genome = ModMisc.STRAIN.get(stack).getGenome();
        boolean identified = tag.getBoolean("Identified");
        StrainInfo info = DnaUtil.convertStrain(genome, identified);
        String sex = info.male() ? "Male" : "Female";
        int id = info.strain().id();
        int thc = info.thc();
        List<TraitGene> genes = ModMisc.STRAIN.get(stack).getExpressed();
        if(identified) {
            tooltip.add(new LiteralText("Strain: ").formatted(Formatting.GRAY).append(new LiteralText(info.strain().name()).formatted(Formatting.GREEN)));
            tooltip.add(new LiteralText("Type: ").formatted(Formatting.GRAY).append(new LiteralText(StringUtils.capitalize(StringUtils.lowerCase(info.strain().type().name()))).formatted(Formatting.DARK_GREEN)));
            tooltip.add(new LiteralText("THC: ").formatted(Formatting.GRAY).append(new LiteralText(thc + "%").formatted(Formatting.DARK_GREEN)));
            Rarity rarity = info.strain().getRarity();
            tooltip.add(new LiteralText("Rarity: ").formatted(Formatting.GRAY).append(new LiteralText(StringUtils.capitalize(StringUtils.lowerCase(rarity.toString()))).formatted(rarity.formatting)));
            if (stack.isOf(ModItems.WEED_SEED)) {
                tooltip.add(new LiteralText("Sex: ").formatted(Formatting.GRAY).append(new LiteralText(sex).formatted(Formatting.DARK_GREEN)));
            }
            if (!genes.isEmpty() && shiftGenes) {
                tooltip.add(new LiteralText("Press ").append( new LiteralText("SHIFT ").formatted(Formatting.GOLD).append( new LiteralText("to view Genes").formatted(Formatting.WHITE))));
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
        if(world != null) {
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
        if (type.equals(ModItems.WEED_SEED)) {
            dropStack(world, pos, type, true);
        } else {
            dropStack(world, pos, type, false);
        }
    }
    public static void dropStack(WorldAccess world, BlockPos pos, Item type) {
        dropStack(world.getBlockEntity(pos).getWorld(), pos, type, true);
    }

    /**
     * @param tag to randomize genes and ID for
     */
    public static boolean randomizeTag(NbtCompound tag) {

        Strain strain = WeedRegistry.randomStrain();
        log("Random: " + strain);
        tag.putInt("ID", strain.id());
        tag.put("Attributes", toNbtList(randGenes(strain)));
        tag.putBoolean("Resource", strain.isResource());
        return strain.isResource();
        //tag.putInt("ID", random.nextInt(StrainUtil.defaultStrains.size() - 1) + 1); // random id
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
        if(strain.isResource()) weight /= 2;
        return weight;
    }
    @Deprecated
    public static ArrayList<Gene> randGenes(Strain strain) {
        float chance = random.nextFloat() + 1F; // 1.0 - 2.0
        ArrayList<Gene> geneList = new ArrayList<>();
        if(random.nextInt(3 + strain.getRarity().ordinal()) == 0) { // 33% - 17% chance
            if (chance <= 0.2F) { // 20% chance
                geneList.add(new Gene(GeneTypes.SPEED, random.nextInt(GeneTypes.SPEED.getMax()) + 1));
                geneList.add(new Gene(GeneTypes.YIELD, random.nextInt(GeneTypes.YIELD.getMax()) + 1));
            } else if (chance > 0.2F && chance <= 0.5F) { // 30% chance
                geneList.add(new Gene(GeneTypes.YIELD, random.nextInt(GeneTypes.YIELD.getMax() - 1) + 1));
            } else if (chance > 0.5F) { // 50% chance
                geneList.add(new Gene(GeneTypes.SPEED, random.nextInt(GeneTypes.SPEED.getMax() - 1) + 1));
            }
        }
        return geneList;
    }
    @Deprecated
    public static ArrayList<Gene> randGenes() {
        float chance = random.nextFloat();
        ArrayList<Gene> geneList = new ArrayList<>();
        if(chance <= 0.1F) { // 10% chance
            geneList.add(new Gene(GeneTypes.SPEED, random.nextInt(GeneTypes.SPEED.getMax()) + 1));
            geneList.add(new Gene(GeneTypes.YIELD, random.nextInt(GeneTypes.YIELD.getMax()) + 1));
        } else if(chance > 0.1F && chance <= 0.25F) { // 15% chance
            geneList.add(new Gene(GeneTypes.YIELD, random.nextInt(GeneTypes.YIELD.getMax() - 1) + 1));
        } else if(chance > 0.25F && chance <= 0.45F) { // 20% chance
            geneList.add(new Gene(GeneTypes.SPEED, random.nextInt(GeneTypes.SPEED.getMax() - 1) + 1));
        } else if(chance > 0.45F && chance <= 1F) { // 65% chance
        }
        return geneList;
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

    @Deprecated
    public static NbtList toNbtList(ArrayList<Gene> list) {
        NbtList nbtList = new NbtList();
        for (Gene entry: list) {
            NbtCompound compound = new NbtCompound();
            compound.putString("Gene", entry.name());
            compound.putInt("Level", entry.level());
            String string = entry.name() + ":" + entry.level();
            nbtList.add(compound);
        }
        return nbtList;
    }
    @Deprecated
    public static ArrayList<Gene> fromNbtList(List<NbtCompound> list) {
        ArrayList<Gene> arrayList = new ArrayList<>();
        for (NbtCompound compoundEntry : list) {
            GeneTypes gene = GeneTypes.byName(compoundEntry.getString("Gene"));
            int level = compoundEntry.getInt("Level");
            arrayList.add(new Gene(gene, level));
        }
        return arrayList;
    }
    @Deprecated
    public static ArrayList<Gene> fromNbtList(NbtList list) {
        ArrayList<Gene> arrayList = new ArrayList<>();
        for (NbtElement compoundEntry : list) {
            GeneTypes geneType = GeneTypes.byName(((NbtCompound)compoundEntry).getString("Gene"));
            int level = ((NbtCompound)compoundEntry).getInt("Level");
            Gene geneObj = new Gene(geneType, level);
            arrayList.add(geneObj);
        }
        return arrayList;
    }
    @Deprecated
    public static boolean NbtListContains(NbtList nbtList, String name) {
        if(nbtList == null || nbtList.isEmpty()) return false;
        for (NbtElement nbtElement : nbtList) {
            NbtCompound entry = (NbtCompound) nbtElement;
            if (entry.getString("Gene").equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
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
        if(!validPosList.isEmpty()) return Util.getRandom(validPosList, random);
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
        if(world.getBlockEntity(originalPos) != null && world.getBlockEntity(copyToPos) != null) {
            world.getBlockEntity(copyToPos, ModEntities.WEED_CROP_ENTITY).get().readNbt(
                    world.getBlockEntity(originalPos, ModEntities.WEED_CROP_ENTITY).get().writeNbt(new NbtCompound()));
            world.markDirty(copyToPos);
        }
    }

    public static DefaultedList<ItemStack> getItemsFromNbt(ItemStack stack) {
        if(stack.isOf(ModItems.SEED_BAG) && stack.hasNbt()) {
            NbtCompound tag = stack.getNbt();
            assert tag != null;
            NbtList nbtList = tag.getList("Items", NbtElement.LIST_TYPE);
            List<ItemStack> itemList = new ArrayList<>();
            for(NbtElement element : nbtList) {
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
        if(stack.hasNbt()) {
            NbtCompound tag = stack.getSubNbt("cannacraft:strain");
            StrainInfo info = ModMisc.STRAIN.get(stack).getStrainInfo();
            String name = tag.getBoolean("Identified") ? info.strain().name() : "Unidentified";
            if(stack.isOf(ModItems.WEED_SEED)) {
                name += stack.getCount() > 1 ? " Seeds": " Seed";
            } else if(stack.isOf(ModItems.WEED_BROWNIE)) {
                name += stack.getCount() > 1 ? " Brownies": " Brownie";
            } else if(stack.isOf(ModItems.WEED_DISTILLATE)) {
                name += " Distillate";
            } else if(stack.isOf(ModItems.WEED_BUNDLE)) {

            }
            return Text.of(name);
        }
        return (Text) Text.EMPTY;
    }
    @Nullable
    public static ItemStack getCrosshairItem() {
        if(MinecraftClient.getInstance().crosshairTarget.getType().equals(HitResult.Type.ENTITY)) {
            if(((EntityHitResult) MinecraftClient.getInstance().crosshairTarget).getEntity() instanceof ItemEntity itemEntity) {
                return itemEntity.getStack();
            }
        }
        return null;
    }
}
