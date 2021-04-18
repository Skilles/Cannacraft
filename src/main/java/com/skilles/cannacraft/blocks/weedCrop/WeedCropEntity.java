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
    private boolean identified;

    public void setData(int index, int thc, boolean identifed) {
        this.index = index;
        this.thc = thc;
        this.identified = identifed;
        System.out.println(writeNbt(new NbtCompound()));
    }

    @Override
    public NbtCompound writeNbt(NbtCompound tag) {
        super.writeNbt(tag);
        tag.putInt("ID", index);
        tag.putInt("THC", thc);
        tag.putBoolean("Identified", identified);
        return tag;

    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
            this.identified = nbt.getBoolean("Identified");
            this.index = nbt.getInt("ID");
            this.thc = nbt.getInt("THC");
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
