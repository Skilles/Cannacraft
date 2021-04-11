package com.skilles.cannacraft.blocks.weedCrop;

import com.skilles.cannacraft.registry.ModEntities;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.CompoundTag;
import com.skilles.cannacraft.StrainType;

import java.util.HashMap;
import java.util.Map;

public class WeedCropEntity extends BlockEntity {

    public WeedCropEntity(BlockEntityType<?> blockEntityType) {
        super(ModEntities.WEED_CROP_ENTITY);
    }
    public WeedCropEntity() {
        this(ModEntities.WEED_CROP_ENTITY);
    }


    // Stores strain name and type. Contains setStrain() with either index or strainName parameters, getStrain(), and getType().


    private int thc = 0;


    private StrainType strain = new StrainType();
    public void setStrain(int strainIndex) {
        this.strain.setStrain(strainIndex);
    }
    public void setStrain(String strainName) { this.strain.setStrain(strainName); }
    public void setThc(int thc) {
        this.thc = thc;
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        super.toTag(tag);
        tag.putString("Strain", strain.getStrain());
        tag.putInt("THC", strain.getTHC());
        tag.putString("Type", strain.getType());
        return tag;
    }

    @Override
    public void fromTag(BlockState state, CompoundTag compoundTag) {
        super.fromTag(state, compoundTag);
        if(compoundTag != null) {
            setStrain(compoundTag.getString("Strain")); //  set strain based on nbt tag
            thc = compoundTag.getInt("THC");
            setThc(thc);
        }
    }
}
