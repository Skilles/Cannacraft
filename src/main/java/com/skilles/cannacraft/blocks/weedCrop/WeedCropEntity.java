package com.skilles.cannacraft.blocks.weedCrop;

import com.skilles.cannacraft.CannacraftClient;
import com.skilles.cannacraft.config.ModConfig;
import com.skilles.cannacraft.registry.ModEntities;
import com.skilles.cannacraft.strain.Gene;
import com.skilles.cannacraft.strain.GeneTypes;
import com.skilles.cannacraft.strain.StrainMap;
import com.skilles.cannacraft.util.CrossUtil;
import com.skilles.cannacraft.util.MiscUtil;
import me.shedaniel.autoconfig.ConfigData;
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.skilles.cannacraft.Cannacraft.log;
import static com.skilles.cannacraft.util.StrainUtil.*;

// TODO: drop seedId, seedThc if male
// TODO: cross plants by collecting pollen from male
// TODO: hide THC for males (possibly completely remove thc for males, selecting for THC will be done by breeding crosses)
public class WeedCropEntity extends BlockEntity implements BlockEntityClientSerializable {

    public WeedCropEntity(BlockPos pos, BlockState state) {
        super(ModEntities.WEED_CROP_ENTITY, pos, state);
        setData(0, 0, false, false, new NbtList());
        config = CannacraftClient.config;
        try {
            config.validatePostLoad();
        } catch (ConfigData.ValidationException e) {
            e.printStackTrace();
        }
    }
    private final ModConfig config;
    private int index;
    private int thc;
    private int seedThc;
    private boolean identified;
    private boolean isMale;
    private int seedId;
    private int breedTimer = 0;
    private NbtList attributes;
    private static final int maxBreedTime = 50;
    private int cachedLimit;

    public void setData(int index, int thc, boolean identified, boolean isMale, NbtList attributes) {
        this.index = index;
        this.thc = thc;
        this.seedThc = thc;
        this.identified = identified;
        this.isMale = isMale;
        this.seedId = index;
        this.attributes = attributes;
    }
    float multiplier() {
        float multiplier = 1;
        if(hasGene(GeneTypes.SPEED)) multiplier *= (1.0F + ((float) getGene(GeneTypes.SPEED).level()) / 2.0F); // 1: 50%, 2: 100%, 3: 150%
        multiplier *= config.getCrop().speed;
        return multiplier;
    }

    public void startBreeding() {
        breedTimer = 1;
    }
    public void stopBreeding() {
        breedCrops(getWorld(), pos, MiscUtil.random());
        breedTimer = -1;
        getWorld().setBlockState(pos, WeedCrop.withBreeding(getCachedState(), false));
    }
    public int breedingProgress() {
        return breedTimer;
    }
    public boolean isBreeding() {
        if(hasGene(GeneTypes.SPEED)) {
            if (breedTimer >= (maxBreedTime / multiplier())) {
                stopBreeding();
                return false;
            }
        } else {
            if (breedTimer >= maxBreedTime) {
                stopBreeding();
                return false;
            }
        }
        return breedTimer > 0;
    }
    public boolean hasBred() {
        return breedTimer < 0;
    }
    public void incrementBreedTick() {
        if(isBreeding()) {
           breedTimer++;
        }
    }
    public boolean canBreed() {
        if(breedingProgress() != -1) {
            WeedCropEntity blockEntity = this;
            NbtCompound ogTag = blockEntity.writeNbt(new NbtCompound());
            int COUNT = 0;
            if (!ogTag.getBoolean("Male")) {
                List<Integer> thcValues = new ArrayList<>();
                for (Direction direction : Direction.Type.HORIZONTAL) {
                    BlockEntity blockEntity2 = world.getBlockEntity(pos.offset(direction));
                    if (blockEntity2 instanceof WeedCropEntity) {
                        NbtCompound tag = blockEntity2.writeNbt(new NbtCompound());
                        if (tag.getBoolean("Male")) {
                            COUNT++;
                        }
                    }
                }
            }
            return COUNT > 0;
        } else {
            return false;
        }
    }
    public boolean hasGene(GeneTypes gene) {
        return MiscUtil.NbtListContains(this.attributes, gene.getName());
    }
    public Gene getGene(GeneTypes type) {
        if(!this.attributes.isEmpty()) {
            for(int i = 0; i < this.attributes.size(); i++) {
                NbtCompound compound = this.attributes.getCompound(i);
                if(compound.getString("Gene").equalsIgnoreCase(type.getName())) return new Gene(compound);
            }
        }
        return null;
    }
    public int growLimit() {
        if(this.cachedLimit != 0) return cachedLimit;
        if(hasGene(GeneTypes.YIELD)) {
            cachedLimit = getGene(GeneTypes.YIELD).level() + 2;
        } else {
            cachedLimit = 2;
        }
        cachedLimit += config.getCrop().yield;
        return cachedLimit;
    }
    /**
     * Crosses name/type/thc with adjacent male. Gets random male if more than one. THC will not change if male THC is lower than female.
     */
    void breedCrops(World world, BlockPos pos, Random random) {
        if(canBreed()) {
            WeedCropEntity blockEntity = this;
            NbtCompound ogTag = blockEntity.writeNbt(new NbtCompound());
            // Cross thc/names/type
            List<String> stringArray = new ArrayList<>();
            List<StrainMap.Type> typeArray = new ArrayList<>();
            int id = ogTag.getInt("ID");
            int thc = ogTag.getInt("Seed THC");
            int maleId = 0;
            List<Integer> thcValues = new ArrayList<>();
            for (Direction direction : Direction.Type.HORIZONTAL) {
                BlockEntity blockEntity2 = world.getBlockEntity(pos.offset(direction));
                if (blockEntity2 instanceof WeedCropEntity) {
                    NbtCompound tag = blockEntity2.writeNbt(new NbtCompound());
                    if (tag.getBoolean("Male")) {
                        if (thc < tag.getInt("Seed THC")) {
                            thcValues.add(tag.getInt("Seed THC")); // adds iterative highest thc values
                            maleId = tag.getInt("ID"); // highest thc male id
                            thc = tag.getInt("Seed THC"); // highest thc value
                        }
                        stringArray.add(getStrain(tag.getInt("Seed ID")).name()); // all names of surrounding males
                        typeArray.add(getStrain(tag.getInt("Seed ID")).type());
                    }
                }
            }
            if (maleId == 0) {
                thc = ogTag.getInt("Seed THC");
                maleId = stringArray.size() > 1 ? indexOf((stringArray.get(MiscUtil.random().nextInt(stringArray.size() - 1)))) : indexOf(stringArray.get(0));
            }
            // Set thc
            ogTag.putInt("Seed THC", CrossUtil.crossThc(thc, ogTag.getInt("THC")));
            log("THC: " + ogTag.getInt("Seed THC"));
            // Set name/type
            int randId = random.nextInt(stringArray.size());
            String name1 = getStrain(id).name();
            String name2 = getStrain(maleId).name();
            StrainMap.Type type1 = getStrain(id).type();
            StrainMap.Type type2 = typeArray.get(randId);
            String crossedName = CrossUtil.crossStrains(name1, name2);
            if (!isPresent(crossedName)) addStrain(crossedName, CrossUtil.crossTypes(type1, type2));
            ogTag.putInt("Seed ID", indexOf(crossedName));

            // Save nbt
            blockEntity.readNbt(ogTag);
            world.markDirty(pos);

            ogTag = world.getBlockEntity(pos).writeNbt(new NbtCompound());

            log("New tag: " + ogTag);

            world.setBlockState(pos, WeedCrop.withBreeding(world.getBlockState(pos), false), 2);
            world.playSound(
                    null,
                    pos,
                    SoundEvents.BLOCK_NOTE_BLOCK_BELL,
                    SoundCategory.BLOCKS,
                    0.2f,
                    2f
            );
        }
    }
    @Override
    public NbtCompound writeNbt(NbtCompound tag) {
        super.writeNbt(tag);
        tag.putInt("ID", index);
        tag.putInt("THC", thc);
        tag.putInt("Seed THC", seedThc);
        tag.putBoolean("Identified", identified);
        tag.putBoolean("Male", isMale);
        tag.putInt("Seed ID", seedId);
        tag.put("Attributes", attributes);
        return tag;
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
            this.identified = nbt.getBoolean("Identified");
            this.index = nbt.getInt("ID");
            this.thc = nbt.getInt("THC");
            this.seedThc = nbt.getInt("Seed THC");
            this.isMale = nbt.getBoolean("Male");
            this.seedId = nbt.getInt("Seed ID");
            this.attributes = nbt.getList("Attributes", NbtType.COMPOUND);
            if(this.index == 0) this.index = this.seedId;
            if(this.thc == 0) this.seedThc = thc;
    }


    @Override
    public void fromClientTag(NbtCompound tag) {
        readNbt(tag);
    }

    @Override
    public NbtCompound toClientTag(NbtCompound tag) {
        return writeNbt(tag);
    }

    @Override
    public void sync() {
        if(!world.isClient)
            BlockEntityClientSerializable.super.sync();
    }
}
