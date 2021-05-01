package com.skilles.cannacraft.blocks.weedCrop;

import com.skilles.cannacraft.registry.ModEntities;
import com.skilles.cannacraft.strain.GeneticsManager;
import com.skilles.cannacraft.strain.StrainMap;
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.skilles.cannacraft.blocks.weedCrop.WeedCrop.withBreeding;
import static com.skilles.cannacraft.strain.StrainMap.*;

// TODO: drop seedId, seedThc if male
public class WeedCropEntity extends BlockEntity implements BlockEntityClientSerializable {

    public WeedCropEntity(BlockPos pos, BlockState state) {
        super(ModEntities.WEED_CROP_ENTITY, pos, state);
    }
    private int index;
    private int thc;
    private int seedThc;
    private boolean identified;
    private boolean isMale;
    private int seedId;
    private int breedTimer = 0;
    private static final int maxBreedTime = 50;

    public void setData(int index, int thc, boolean identified, boolean isMale) {
        this.index = index;
        this.thc = thc;
        this.seedThc = thc;
        this.identified = identified;
        this.isMale = isMale;
        this.seedId = index;
    }
    public void startBreeding() {
        System.out.println("start");
        breedTimer = 1;
    }
    public void stopBreeding() { System.out.println("stop");
        breedTimer = -1;
        breedCrops(getWorld(), pos, GeneticsManager.random());
        getWorld().setBlockState(pos, withBreeding(getCachedState(), false));
    }
    public int breedingProgress() {
        return breedTimer;
    }
    public boolean isBreeding() {
        if(breedTimer >= maxBreedTime) {
            stopBreeding();
            return false;
        }
        return breedTimer > 0;
    }
    public boolean hasBred() {
        return breedTimer < 0;
    }
    public void incrementBreedTick() { System.out.println(breedTimer);
        if(isBreeding()) {
           breedTimer++;
        }
    }
    public boolean isMale() {
        return isMale;
    }
    /**
     * Crosses name/type/thc with adjacent male. Gets random male if more than one. THC will not change if male THC is lower than female.
     */
    void breedCrops(World world, BlockPos pos, Random random){
        WeedCropEntity blockEntity = this;
        NbtCompound ogTag = blockEntity.writeNbt(new NbtCompound());
        if(!ogTag.getBoolean("Male")) {
            // Cross thc/names/type
            List<String> stringArray = new ArrayList<>();
            List<StrainMap.Type> typeArray = new ArrayList<>();
            int id = ogTag.getInt("ID");
            int thc = ogTag.getInt("Seed THC");
            int COUNT = 0;
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
                        COUNT++;
                    }
                }
            }
            if (COUNT > 0) {
                if (maleId == 0) {
                    thc = ogTag.getInt("Seed THC");
                    maleId = stringArray.size() > 1 ? indexOf((stringArray.get(GeneticsManager.random().nextInt(stringArray.size() - 1)))) : indexOf(stringArray.get(0));
                }
                // Set thc
                ogTag.putInt("Seed THC", GeneticsManager.crossThc(thc, ogTag.getInt("THC")));
                System.out.println("THC: " + ogTag.getInt("Seed THC"));
                // Set name/type
                int randId = random.nextInt(stringArray.size());
                String name1 = getStrain(id).name();
                String name2 = getStrain(maleId).name();
                StrainMap.Type type1 = getStrain(id).type();
                StrainMap.Type type2 = typeArray.get(randId);
                String crossedName = GeneticsManager.crossStrains(name1, name2);
                if (!isPresent(crossedName)) addStrain(crossedName, GeneticsManager.crossTypes(type1, type2));
                ogTag.putInt("Seed ID", indexOf(crossedName));

                // Save nbt
                blockEntity.readNbt(ogTag);
                world.markDirty(pos);

                ogTag = world.getBlockEntity(pos).writeNbt(new NbtCompound());

                System.out.println("New tag: " + ogTag);

                world.setBlockState(pos, WeedCrop.withBreeding(world.getBlockState(pos), false), 2);
            }
        }
        world.playSound(
                null,
                pos,
                SoundEvents.BLOCK_NOTE_BLOCK_BELL,
                SoundCategory.BLOCKS,
                0.2f,
                2f
        );
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
