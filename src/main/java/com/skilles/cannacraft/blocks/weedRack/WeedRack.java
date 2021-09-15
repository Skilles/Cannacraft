package com.skilles.cannacraft.blocks.weedRack;

import com.skilles.cannacraft.registry.ModEntities;
import com.skilles.cannacraft.registry.ModItems;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

public class WeedRack extends BlockWithEntity {

    public static final DirectionProperty FACING;

    public WeedRack(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.SOUTH));
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new WeedRackEntity(pos, state);
    }

    public static final VoxelShape NORTH_SHAPE = Block.createCuboidShape(0.0D, 12.0D, 12.0D, 16.0D, 16.0D, 16.0D);
    public static final VoxelShape SOUTH_SHAPE = Block.createCuboidShape(0.0D, 12.0D, 0.0D, 16.0D, 16.0D, 4.0D);
    public static final VoxelShape EAST_SHAPE = Block.createCuboidShape(0.0D, 12.0D, 0.0D, 4.0D, 16.0D, 16.0D);
    public static final VoxelShape WEST_SHAPE = Block.createCuboidShape(12.0D, 12.0D, 0.0D, 16.0D, 16.0D, 16.0D);


    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return checkType(type, ModEntities.WEED_RACK_ENTITY, WeedRackEntity::tick);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        //With inheriting from BlockWithEntity this defaults to INVISIBLE, so we need to change that!
        return BlockRenderType.MODEL;
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand,
                              BlockHitResult hit) {
        WeedRackEntity dryingRackEntity = (WeedRackEntity) world.getBlockEntity(pos);
        ItemStack stack = dryingRackEntity.getStack(0);
        if (!stack.isEmpty()) {
            // Remove hanging item
            if (!world.isClient && !player.giveItemStack(stack.split(4))) {
                player.dropItem(stack.split(4), false);
            }
            dryingRackEntity.clear();
            return ActionResult.SUCCESS;
        } else {
            // Hang item on rack
            ItemStack heldItem = player.getMainHandStack();
            if (!heldItem.isEmpty() && heldItem.isOf(ModItems.WEED_BUNDLE) && heldItem.hasNbt()) {
                if (!world.isClient && heldItem.getNbt().getCompound("cannacraft:strain").getFloat("Status") == 1.0f) {
                    if (player.isCreative()) dryingRackEntity.setStack(0, heldItem.copy());
                    else dryingRackEntity.setStack(0, heldItem.split(4));
                }
                return ActionResult.SUCCESS;
            }
            return ActionResult.FAIL;
        }
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        Direction dir = state.get(FACING);
        return switch (dir) {
            case SOUTH -> SOUTH_SHAPE;
            case EAST -> EAST_SHAPE;
            case WEST -> WEST_SHAPE;
            default -> NORTH_SHAPE;
        };
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState newState,
                                                WorldAccess world, BlockPos pos, BlockPos posFrom) {
        if (direction.getOpposite() == state.get(FACING) && !state.canPlaceAt(world, pos)) {
            return Blocks.AIR.getDefaultState();
        } else {
            return super.getStateForNeighborUpdate(state, direction, newState, world, pos, posFrom);
        }
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState().with(Properties.HORIZONTAL_FACING, ctx.getPlayerFacing().getOpposite());
    }

    @Override
    public BlockState rotate(BlockState state, BlockRotation rotation) {
        return state.with(FACING, rotation.rotate(state.get(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(FACING)));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.isOf(newState.getBlock())) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof Inventory) {
                ItemScatterer.spawn(world, pos, (Inventory) blockEntity);
                world.updateComparators(pos, this);
            }

            super.onStateReplaced(state, world, pos, newState, moved);
        }
    }

    static {
        FACING = HorizontalFacingBlock.FACING;
    }
}
