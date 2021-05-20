package com.skilles.cannacraft.util;

import com.skilles.cannacraft.registry.ModEntities;
import com.skilles.cannacraft.registry.ModItems;
import com.skilles.cannacraft.strain.Gene;
import com.skilles.cannacraft.strain.GeneTypes;
import com.skilles.cannacraft.strain.StrainMap;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
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
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
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

    public static void appendTooltips(List<Text> tooltip, NbtCompound tag) {
        String sex = "";
        if(tag.contains("Male")) sex = tag.getBoolean("Male") ? "Male" : "Female";
        int id = tag.getInt("ID");
        int thc = tag.getInt("THC");
        NbtList genes = new NbtList();
        if(tag.contains("Attributes")) genes = tag.getList("Attributes", NbtType.COMPOUND);
        if(tag.getBoolean("Identified")) {
            tooltip.add(new LiteralText("Strain: ").formatted(Formatting.GRAY).append(new LiteralText(StrainUtil.getStrain(id).name()).formatted(Formatting.GREEN)));
            tooltip.add(new LiteralText("Type: ").formatted(Formatting.GRAY).append(new LiteralText(StringUtils.capitalize(StringUtils.capitalize(StringUtils.lowerCase(StrainUtil.getStrain(id).type().name())))).formatted(Formatting.DARK_GREEN)));
            tooltip.add(new LiteralText("THC: ").formatted(Formatting.GRAY).append(new LiteralText(thc + "%").formatted(Formatting.DARK_GREEN)));
            if(!sex.isEmpty()) tooltip.add(new LiteralText("Sex: ").formatted(Formatting.GRAY).append(new LiteralText(sex).formatted(Formatting.DARK_GREEN)));
            if(!genes.isEmpty()) {
                tooltip.add(new LiteralText("Press ").append( new LiteralText("SHIFT ").formatted(Formatting.GOLD).append( new LiteralText("to view Genes").formatted(Formatting.WHITE))));
            }
        } else {
            tooltip.add(new LiteralText("Strain: ").formatted(Formatting.GRAY).append(new LiteralText("Unidentified").formatted(Formatting.GREEN)));
            tooltip.add(new LiteralText("Type: ").formatted(Formatting.GRAY).append(new LiteralText("Unknown").formatted(Formatting.DARK_GREEN)));
            tooltip.add(new LiteralText("Sex: ").formatted(Formatting.GRAY).append(new LiteralText("Unknown").formatted(Formatting.DARK_GREEN)));
        }
    }

    /**
     * Drops an itemstack with NBT
     */
    public static void dropStack(World world, BlockPos pos, Item type, boolean brokenWithShears) {
        ItemStack toDrop = new ItemStack(type);
        if(world.getBlockEntity(pos) != null) {
            NbtCompound tag = world.getBlockEntity(pos).writeNbt(new NbtCompound());
            if (tag != null) {
                tag.putInt("THC", tag.getInt("Seed THC"));
                if (type.equals(ModItems.WEED_SEED) && !tag.getBoolean("Male")) {
                    toDrop.putSubTag("cannacraft:strain", trimTag(tag, type));
                    Block.dropStack(world, pos, toDrop);
                } else if (brokenWithShears && type.equals(ModItems.WEED_BUNDLE)) {
                    toDrop.putSubTag("cannacraft:strain", trimTag(tag));
                    Block.dropStack(world, pos, toDrop);
                }
            } else {
                log("Error: NULLTAG");
            }
        } else {
            log("Error: NULLBENTITY");
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
    public static void randomizeTag(NbtCompound tag) {
        Random random = random();
        float chance = random.nextFloat();
        NbtList nbtList = new NbtList();
        ArrayList<Gene> geneList = new ArrayList<>();
        if(chance <= 0.1F) { // 10% chance
            geneList.add(new Gene(GeneTypes.SPEED, random.nextInt(GeneTypes.SPEED.getMax()) + 1));
            geneList.add(new Gene(GeneTypes.YIELD, random.nextInt(GeneTypes.YIELD.getMax()) + 1));
            tag.put("Attributes", nbtList);
        } else if(chance > 0.1F && chance <= 0.25F) { // 15% chance
            geneList.add(new Gene(GeneTypes.YIELD, random.nextInt(GeneTypes.YIELD.getMax() - 1) + 1));
        } else if(chance > 0.25F && chance <= 0.45F) { // 20% chance
            geneList.add(new Gene(GeneTypes.SPEED, random.nextInt(GeneTypes.SPEED.getMax() - 1) + 1));
        } else if(chance > 0.45F && chance <= 1F) { // 65% chance
        }
        tag.put("Attributes", toNbtList(geneList));
        tag.putInt("ID", MiscUtil.random().nextInt((StrainMap.ogStrainCount - 1)) + 1); // random id
    }
    /**
     * Format Block NBT to conform with ItemStack
     * @param tag block NBT tag
     * @param type type of format
     * @return tag with trimmed NBT
     */
    public static NbtCompound trimTag(NbtCompound tag, Item type){
        NbtCompound newTag = tag;
        if(tag != null) {
            newTag.remove("id");
            newTag.remove("x");
            newTag.remove("y");
            newTag.remove("z");
            newTag.putInt("THC", newTag.getInt("Seed THC"));
            newTag.remove("Seed THC");
            if(newTag.contains("Male") && !newTag.getBoolean("Male")) newTag.remove("Male");
            if(type !=null && type.equals(ModItems.WEED_SEED)) newTag.putInt("ID", newTag.getInt("Seed ID"));
            newTag.remove("Seed ID");
        }
        return newTag;
    }
    public static NbtCompound trimTag(NbtCompound tag) {
        return trimTag(tag, null);
    }
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
    public static ArrayList<Gene> fromNbtList(List<NbtCompound> list) {
        ArrayList<Gene> arrayList = new ArrayList<>();
        for (NbtCompound compoundEntry : list) {
            GeneTypes gene = GeneTypes.byName(compoundEntry.getString("Gene"));
            int level = compoundEntry.getInt("Level");
            arrayList.add(new Gene(gene, level));
        }
        return arrayList;
    }
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
            for (Block block : blocks) {
                if (neighborState.isOf(block) && (!toGrow || world.getBlockState(pos.offset(direction).up()).isOf(Blocks.AIR))) validDirections.add(direction);
            }
        }
        if(!validDirections.isEmpty()) return Util.getRandom(validDirections, random());
        return null;
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
}
