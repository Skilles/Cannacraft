package com.skilles.cannacraft.blocks.weedBong;

import com.skilles.cannacraft.items.WeedJoint;
import com.skilles.cannacraft.registry.BlockEntities;
import com.skilles.cannacraft.registry.ModContent;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FlintAndSteelItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import static com.skilles.cannacraft.Cannacraft.log;
import static com.skilles.cannacraft.util.WeedRegistry.*;

public class WeedBong extends BlockWithEntity {

    public static final DirectionProperty FACING;


    public WeedBong() {
        super(FabricBlockSettings.copyOf(Blocks.GLASS).nonOpaque());
        this.setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.NORTH));
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {

        WeedBongEntity bongEntity = (WeedBongEntity) world.getBlockEntity(pos);
        ItemStack handStack = hand == Hand.MAIN_HAND ? player.getMainHandStack() : player.getOffHandStack();
        assert bongEntity != null;
        if (bongEntity.cooldown) return ActionResult.FAIL;
        if (bongEntity.packed) {
            if (handStack.isEmpty()) {
                if (bongEntity.lit) {
                    if (bongEntity.startedHitting) {
                        bongEntity.clicks++;
                        bongEntity.hitTimer = 0;
                        if (bongEntity.clicks > 20) {
                            log("Hitted the bong!");
                            bongEntity.cooldown = true;
                            bongEntity.clicks = 0;
                            bongEntity.startedHitting = false;
                            bongEntity.timer /= 2;
                            if (++bongEntity.hits == 5) {
                                bongEntity.packed = false;
                                bongEntity.lit = false;
                                bongEntity.cooldown = false;
                                bongEntity.timer = 0;
                                bongEntity.hits = 0;
                            }
                            // HighUtil.applyHigh(bongEntity.packedInfo, player);
                            if (world.isClient) {
                                for (int i = 0; i < 10; i++) {
                                    WeedJoint.spawnSmoke(player);
                                }
                            }
                        }
                    } else {
                        bongEntity.startHitting();
                        log("Started hitting");
                    }
                    return ActionResult.SUCCESS;
                }
            } else if (handStack.getItem() instanceof FlintAndSteelItem) {
                bongEntity.lit = true;
                log("Lit the bong");
                return ActionResult.SUCCESS;
            }
        } else if (handStack.isOf(ModContent.WEED_BUNDLE) && getStatus(handStack).equals(StatusTypes.GROUND)) {
            bongEntity.packedInfo = getStrainInfo(handStack);
            bongEntity.packed = true;
            handStack.decrement(1);
            log("Packed the bong");
            return ActionResult.SUCCESS;
        }


        return ActionResult.FAIL;
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return Block.createCuboidShape(5.0D, 0.0D, 5.0D, 10.0D, 16.0D, 10.0D);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        Direction direction = ctx.getPlayerFacing().getOpposite();
        return this.getDefaultState().with(FACING, direction);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> stateManager) {
        stateManager.add(FACING);
    }

    static {
        FACING = HorizontalFacingBlock.FACING;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return checkType(type, BlockEntities.BONG, WeedBongEntity::tick);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new WeedBongEntity(pos, state);
    }
}
