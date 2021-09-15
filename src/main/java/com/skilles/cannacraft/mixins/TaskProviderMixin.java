package com.skilles.cannacraft.mixins;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import com.skilles.cannacraft.misc.StonerFarmTask;
import com.skilles.cannacraft.registry.ModMisc;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.*;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.village.VillagerProfession;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

/**
 * Adds the Stoner farming task to the villager task list
 */
@Mixin(VillagerTaskListProvider.class)
public abstract class TaskProviderMixin {
    @Shadow
    private static Pair<Integer, Task<LivingEntity>> createBusyFollowTask() {
        return null;
    }
    @Inject(at = @At("TAIL"), method = "createWorkTasks(Lnet/minecraft/village/VillagerProfession;F)Lcom/google/common/collect/ImmutableList;", locals = LocalCapture.CAPTURE_FAILEXCEPTION, cancellable = true)
    private static void injectWorkTasks(VillagerProfession profession, float speed, CallbackInfoReturnable<ImmutableList<Pair<Integer, ? extends Task<? super VillagerEntity>>>> cir) {
        if (profession == ModMisc.STONER) cir.setReturnValue(ImmutableList.of(createBusyFollowTask(), Pair.of(5, new RandomTask<>(ImmutableList.of(Pair.of(new FarmerWorkTask(), 7), Pair.of(new GoToIfNearbyTask(MemoryModuleType.JOB_SITE, 0.5F, 4), 2), Pair.of(new GoToNearbyPositionTask(MemoryModuleType.JOB_SITE, 0.5F, 1, 10), 5), Pair.of(new GoToSecondaryPositionTask(MemoryModuleType.SECONDARY_JOB_SITE, speed, 1, 6, MemoryModuleType.JOB_SITE), 5), Pair.of(new StonerFarmTask(), 2), Pair.of(new BoneMealTask(), 4)))), Pair.of(10, new HoldTradeOffersTask(400, 1600)), Pair.of(10, new FindInteractionTargetTask(EntityType.PLAYER, 4)), Pair.of(2, new VillagerWalkTowardsTask(MemoryModuleType.JOB_SITE, speed, 9, 100, 1200)), Pair.of(3, new GiveGiftsToHeroTask(100)), Pair.of(99, new ScheduleActivityTask())));
    }
}
