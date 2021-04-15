package com.skilles.cannacraft.blocks.weedCrop;

import com.skilles.cannacraft.registry.ModComponents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.state.StateManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class WeedCrop extends CropBlock implements BlockEntityProvider {

    //public static final IntProperty STRAIN = IntProperty.of("strain", 0, 2);

    public WeedCrop(Settings settings) {
        super(settings);
        //setDefaultState(getStateManager().getDefaultState().with(STRAIN, 0));
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new WeedCropEntity(pos, state);
    }
    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> stateManager) {
        stateManager.add(AGE);
        //stateManager.add(STRAIN);
    }

    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        if (itemStack.hasTag()) {
            NbtCompound tag =  itemStack.getTag();
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof WeedCropEntity && tag != null && tag.contains("ID")) {
                ModComponents.STRAIN.get(blockEntity).setIndex(tag.getInt("ID"));
                if(tag.getBoolean("Identified")){
                    ModComponents.STRAIN.get(blockEntity).identify();
                }
            }
        }
    }
}
