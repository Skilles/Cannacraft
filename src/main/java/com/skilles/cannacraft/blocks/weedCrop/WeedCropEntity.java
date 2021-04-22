package com.skilles.cannacraft.blocks.weedCrop;

import com.skilles.cannacraft.registry.ModEntities;
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;

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

    public void setData(int index, int thc, boolean identified, boolean isMale) {
        this.index = index;
        this.thc = thc;
        this.seedThc = thc;
        this.identified = identified;
        this.isMale = isMale;
        this.seedId = index;
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
