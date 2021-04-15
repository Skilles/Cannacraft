package com.skilles.cannacraft.blocks.weedCrop;

import com.skilles.cannacraft.items.StrainInterface;
import com.skilles.cannacraft.registry.ModComponents;
import com.skilles.cannacraft.registry.ModEntities;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;

public class WeedCropEntity extends BlockEntity {

    public WeedCropEntity(BlockPos pos, BlockState state) {
        super(ModEntities.WEED_CROP_ENTITY, pos, state);
    }
    /*public WeedCropEntity() {
        this(ModEntities.WEED_CROP_ENTITY);
    }*/


    // Stores strain name and type. Contains setStrain() with either index or strainName parameters, getStrain(), and getType().



    private final StrainInterface strainInterface = ModComponents.STRAIN.get(this);


    @Override
    public NbtCompound writeNbt(NbtCompound tag) {
        super.writeNbt(tag);
        tag.putString("Strain", strainInterface.getStrain());
        tag.putInt("THC", strainInterface.getTHC());
        tag.putString("Type", strainInterface.getType());
        return tag;
    }

    @Override
    public void readNbt(NbtCompound compoundTag) {
        super.readNbt(compoundTag);
        if(compoundTag != null && compoundTag.contains("ID")) {
            strainInterface.setIndex(compoundTag.getInt("ID"));
        }
    }
}
