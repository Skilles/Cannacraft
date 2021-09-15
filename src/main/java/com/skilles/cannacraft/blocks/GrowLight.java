package com.skilles.cannacraft.blocks;

import com.skilles.cannacraft.blocks.weedCrop.WeedCropEntity;
import net.minecraft.block.*;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import java.util.Random;

public class GrowLight extends Block {
    public static final DirectionProperty FACING;
    public static final BooleanProperty LIT;
    private static final int RANGE = 2;

    public GrowLight(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.NORTH));
        this.setDefaultState(this.stateManager.getDefaultState().with(LIT, false));
    }

    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean notify) {
        if (!world.isClient) {
            boolean bl = state.get(LIT);
            if (bl != world.isReceivingRedstonePower(pos)) {
                if (bl) {
                    world.getBlockTickScheduler().schedule(pos, this, 4);
                } else {
                    world.setBlockState(pos, state.cycle(LIT), 2);
                }
            }
        }
    }

    @Override
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        world.getBlockTickScheduler().schedule(pos, this, getTickRate());
    }

    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (state.get(LIT)) {
            if (!world.isReceivingRedstonePower(pos))
                world.setBlockState(pos, state.cycle(LIT), 2);

            BlockPos.stream(pos.add(RANGE, 0, -RANGE).down(), pos.down().add(-RANGE, 0, RANGE))
                    .filter(aoePos -> world.getBlockState(aoePos).getBlock() instanceof Fertilizable)
                    .forEachOrdered(aoePos -> {
                        if (world.getBlockEntity(aoePos) instanceof WeedCropEntity weedEntity) {
                            weedEntity.boosted = true;
                            /*NbtCompound beTag = weedEntity.writeNbt(new NbtCompound());
                            beTag.putBoolean("boosted", true);
                            weedEntity.readNbt(beTag);*/
                        } else {
                            world.getBlockState(aoePos).randomTick(world, aoePos, world.random);
                        }
                        // world.spawnParticles(ParticleTypes.GLOW, aoePos.getX() + 0.5, aoePos.getY() + 0.5, aoePos.getZ() + 0.5, 10, 0, 0, 0, 0);
                    });
            world.getBlockTickScheduler().schedule(pos, this, getTickRate());
        }
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        //With inheriting from BlockWithEntity this defaults to INVISIBLE, so we need to change that!
        return BlockRenderType.MODEL;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        if (state.get(FACING).getAxis() == Direction.Axis.Z) {
            return Block.createCuboidShape(0.0D, 12.0D, 5.0D, 16.0D, 16.0D, 11.0D);
        }
        return Block.createCuboidShape(5.0D, 12.0D, 0.0D, 11.0D, 16.0D, 16.0D);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        Direction direction = ctx.getPlayerFacing().getOpposite();
        return this.getDefaultState().with(FACING, direction).with(LIT, ctx.getWorld().isReceivingRedstonePower(ctx.getBlockPos()));
    }
    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> stateManager) {
        stateManager.add(FACING, LIT);
    }

    static {
        FACING = HorizontalFacingBlock.FACING;
        LIT = RedstoneTorchBlock.LIT;
    }
    private static int getTickRate() {
        double variance = Math.random() * (1.1 - 0.9) + 0.9;
        //return (int) (ModConfigs.GROW_LIGHT_COOLDOWN * variance) * 20;
        return (int) (10 * variance) * 20;
    }

}
