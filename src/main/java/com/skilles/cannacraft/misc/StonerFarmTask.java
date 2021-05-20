package com.skilles.cannacraft.misc;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.skilles.cannacraft.Cannacraft;
import com.skilles.cannacraft.blocks.weedCrop.WeedCrop;
import com.skilles.cannacraft.registry.ModBlocks;
import com.skilles.cannacraft.registry.ModItems;
import com.skilles.cannacraft.registry.ModMisc;
import net.minecraft.block.*;
import net.minecraft.entity.ai.brain.BlockPosLookTarget;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.WalkTarget;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.village.VillagerProfession;
import net.minecraft.world.GameRules;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class StonerFarmTask extends Task<VillagerEntity> {
    private static final int MAX_RUN_TIME = 200;
    public static final float WALK_SPEED = 0.5F;
    @Nullable
    private BlockPos currentTarget;
    private long nextResponseTime;
    private int ticksRan;
    private final List<BlockPos> targetPositions = Lists.newArrayList();

    public StonerFarmTask() {
        super(ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryModuleState.VALUE_ABSENT, MemoryModuleType.WALK_TARGET, MemoryModuleState.VALUE_ABSENT, MemoryModuleType.SECONDARY_JOB_SITE, MemoryModuleState.VALUE_PRESENT));
    }

    protected boolean shouldRun(ServerWorld serverWorld, VillagerEntity villagerEntity) {
        if (!serverWorld.getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING)) {
            return false;
        } else if (villagerEntity.getVillagerData().getProfession() != ModMisc.STONER) {
            return false;
        } else {
            BlockPos.Mutable mutable = villagerEntity.getBlockPos().mutableCopy();
            this.targetPositions.clear();

            for(int i = -1; i <= 1; ++i) {
                for(int j = -1; j <= 1; ++j) {
                    for(int k = -1; k <= 1; ++k) {
                        mutable.set(villagerEntity.getX() + (double)i, villagerEntity.getY() + (double)j, villagerEntity.getZ() + (double)k);
                        if (this.isSuitableTarget(mutable, serverWorld)) {
                            this.targetPositions.add(new BlockPos(mutable));
                            Cannacraft.log("Position found");
                        }
                    }
                }
            }

            this.currentTarget = this.chooseRandomTarget(serverWorld);
            return this.currentTarget != null;
        }
    }

    @Nullable
    private BlockPos chooseRandomTarget(ServerWorld world) {
        return this.targetPositions.isEmpty() ? null : (BlockPos)this.targetPositions.get(world.getRandom().nextInt(this.targetPositions.size()));
    }

    private boolean isSuitableTarget(BlockPos pos, ServerWorld world) {
        BlockState blockState = world.getBlockState(pos);
        Block block = blockState.getBlock();
        Block block2 = world.getBlockState(pos.down()).getBlock();
        return block instanceof WeedCrop && ((WeedCrop)block).isBloomed(blockState) && world.getBlockState(pos.down()).getBlock() instanceof FarmlandBlock || blockState.isAir() && block2 instanceof FarmlandBlock;
    }

    protected void run(ServerWorld serverWorld, VillagerEntity villagerEntity, long l) {
        if (l > this.nextResponseTime && this.currentTarget != null) {
            Cannacraft.log("Running stoner task");
            villagerEntity.getBrain().remember(MemoryModuleType.LOOK_TARGET, new BlockPosLookTarget(this.currentTarget));
            villagerEntity.getBrain().remember(MemoryModuleType.WALK_TARGET, new WalkTarget(new BlockPosLookTarget(this.currentTarget), 0.5F, 1));
        }
    }

    protected void finishRunning(ServerWorld serverWorld, VillagerEntity villagerEntity, long l) {
        villagerEntity.getBrain().forget(MemoryModuleType.LOOK_TARGET);
        villagerEntity.getBrain().forget(MemoryModuleType.WALK_TARGET);
        this.ticksRan = 0;
        this.nextResponseTime = l + 40L;
    }
    private boolean hasWeedSeed(VillagerEntity villagerEntity) {
        return villagerEntity.getInventory().containsAny(ImmutableSet.of(ModItems.WEED_SEED));
    }
    protected void keepRunning(ServerWorld serverWorld, VillagerEntity villagerEntity, long l) {
        if (this.currentTarget == null || this.currentTarget.isWithinDistance(villagerEntity.getPos(), 1.0D)) {
            if (this.currentTarget != null && l > this.nextResponseTime) {
                BlockState blockState = serverWorld.getBlockState(this.currentTarget);
                Block block = blockState.getBlock();
                Block block2 = serverWorld.getBlockState(this.currentTarget.down()).getBlock();
                if (block instanceof WeedCrop && ((WeedCrop)block).isBloomed(blockState)) {
                    serverWorld.breakBlock(this.currentTarget, true, villagerEntity);
                }

                if (blockState.isAir() && block2 instanceof FarmlandBlock && hasWeedSeed(villagerEntity)) {
                    SimpleInventory simpleInventory = villagerEntity.getInventory();

                    for(int i = 0; i < simpleInventory.size(); ++i) {
                        ItemStack itemStack = simpleInventory.getStack(i);
                        boolean bl = false;
                        if (!itemStack.isEmpty() && itemStack.isOf(ModItems.WEED_SEED)) {
                            serverWorld.setBlockState(this.currentTarget, ModBlocks.WEED_CROP.getDefaultState(), 3);
                            bl = true;

                        }

                        if (bl) {
                            serverWorld.playSound((PlayerEntity)null, (double)this.currentTarget.getX(), (double)this.currentTarget.getY(), (double)this.currentTarget.getZ(), SoundEvents.ITEM_CROP_PLANT, SoundCategory.BLOCKS, 1.0F, 1.0F);
                            itemStack.decrement(1);
                            if (itemStack.isEmpty()) {
                                simpleInventory.setStack(i, ItemStack.EMPTY);
                            }
                            break;
                        }
                    }
                }

                if (block instanceof WeedCrop && ((WeedCrop)block).isBloomed(blockState)) {
                    this.targetPositions.remove(this.currentTarget);
                    this.currentTarget = this.chooseRandomTarget(serverWorld);
                    if (this.currentTarget != null) {
                        this.nextResponseTime = l + 20L;
                        villagerEntity.getBrain().remember(MemoryModuleType.WALK_TARGET, new WalkTarget(new BlockPosLookTarget(this.currentTarget), 0.5F, 1));
                        villagerEntity.getBrain().remember(MemoryModuleType.LOOK_TARGET, new BlockPosLookTarget(this.currentTarget));
                    }
                }
            }

            ++this.ticksRan;
        }
    }

    protected boolean shouldKeepRunning(ServerWorld serverWorld, VillagerEntity villagerEntity, long l) {
        return this.ticksRan < 200;
    }
}