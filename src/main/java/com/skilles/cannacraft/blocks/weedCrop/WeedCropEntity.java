package com.skilles.cannacraft.blocks.weedCrop;

import com.skilles.cannacraft.CannacraftClient;
import com.skilles.cannacraft.config.ModConfig;
import com.skilles.cannacraft.registry.ModEntities;
import com.skilles.cannacraft.strain.Gene;
import com.skilles.cannacraft.strain.GeneTypes;
import com.skilles.cannacraft.strain.Strain;
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
import static com.skilles.cannacraft.util.StrainUtil.getStrain;

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
    boolean isMale;
    private int seedId;
    private boolean resource;
    private int breedTimer = 0;
    private NbtList attributes;
    private static final int maxBreedTime = 50;
    private int cachedLimit;
    public boolean boosted;

    public void setData(int index, int thc, boolean identified, boolean isMale, NbtList attributes) {
        this.index = index;
        this.thc = thc;
        this.seedThc = thc;
        this.identified = identified;
        this.isMale = isMale;
        this.seedId = index;
        this.attributes = attributes;
    }
    public void setData(NbtCompound tag, NbtList attributes) {
        this.index = tag.getInt("ID");
        this.thc = tag.getInt("THC");
        this.identified = tag.getBoolean("Identified");
        this.isMale = tag.getBoolean("Male");
        this.resource = tag.getBoolean("Resource");
        this.seedId = index;
        this.seedThc = thc;
        this.attributes = attributes;
        log("Data for BE crop set" + tag);
    }
    float multiplier() {
        float multiplier = 1;
        if(hasGene(GeneTypes.SPEED)) multiplier *= (1.0F + ((float) getGene(GeneTypes.SPEED).level()) / 2.0F); // 1: 50%, 2: 100%, 3: 150%
        multiplier *= config.getCrop().speed;
        if(boosted) multiplier *= 2;
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
        if (breedTimer >= (maxBreedTime / multiplier())) {
            stopBreeding();
            return false;
        }
        return breedTimer > 0;
    }
    public boolean hasBred() {
        return breedTimer < 0;
    }
    public void incrementBreedTick() {
        if(isBreeding()) breedTimer++;
    }
    public boolean canBreed() {
        if(breedingProgress() != -1 && !this.isMale) {
            int COUNT = 0;
            for (Direction direction : Direction.Type.HORIZONTAL) {
                BlockEntity blockEntity2 = world.getBlockEntity(pos.offset(direction));
                if (blockEntity2 instanceof WeedCropEntity cropToBreed) {
                    //NbtCompound tag = blockEntity2.writeNbt(new NbtCompound());
                    if (cropToBreed.isMale && this.resource == cropToBreed.resource) {
                        COUNT++;
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
     * Crosses name/type/thc with adjacent male. Gets highest THC male if more than one.
     * TODO: add check/randomness if ID is unknown
     */
    void breedCrops(World world, BlockPos pos, Random random) {
        if(canBreed()) {
            // Cross thc/names/type
            int id = this.index;
            int thc = this.thc;
            int maleId = 0;
            int maleThc = 0;
            boolean maleIdentified = false;
            NbtCompound maleTag = null;
            List<WeedCropEntity> entityList = new ArrayList<>();
            for (Direction direction : Direction.Type.HORIZONTAL) {
                BlockEntity blockEntity2 = world.getBlockEntity(pos.offset(direction));
                if (blockEntity2 instanceof WeedCropEntity weedBlockEntity2) {
                    if (weedBlockEntity2.isMale) {
                        entityList.add(weedBlockEntity2);
                        if(entityList.size() == 1 || !config.getCrop().randomBreed) { // if only 1 male or randomBreed is off
                            if(weedBlockEntity2.thc > maleThc) { // if thc hasn't been set or is greater than previous male's
                                maleThc = weedBlockEntity2.thc; // highest male thc
                                // maleId = weedBlockEntity2.index; // highest thc male id
                                maleIdentified = weedBlockEntity2.identified;
                                maleTag = weedBlockEntity2.writeNbt(new NbtCompound());
                            }
                        }
                    }
                }
            }
            if(config.getCrop().randomBreed) {
                WeedCropEntity randCrop = entityList.get(random.nextInt(entityList.size()));
                maleThc = randCrop.thc;
                // maleId = randCrop.index; // random thc male id
                maleIdentified = randCrop.identified;
                maleTag = randCrop.writeNbt(new NbtCompound());
            }
            // Set thc
            this.seedThc = CrossUtil.crossThc(maleThc, this.thc);
            // Set strain
            NbtCompound myTag = this.writeNbt(new NbtCompound());
            Strain crossedStrain = CrossUtil.crossStrains(getStrain(myTag), getStrain(maleTag));
            log("Strain 1 " + getStrain(myTag));
            log("Strain 2 " + getStrain(maleTag));
            log("Name of crossed strain: " + crossedStrain.name());
            this.seedId = crossedStrain.id();
            if(this.identified) this.identified = maleIdentified;
            log("New tag: " + this.writeNbt(new NbtCompound()));

            world.setBlockState(pos, WeedCrop.withBreeding(world.getBlockState(pos), false), 2);
            world.markDirty(pos);
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
