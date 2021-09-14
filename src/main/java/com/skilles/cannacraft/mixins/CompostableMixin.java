package com.skilles.cannacraft.mixins;

import com.skilles.cannacraft.registry.ModItems;
import net.minecraft.block.ComposterBlock;
import net.minecraft.item.ItemConvertible;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Allows seed and bundle to be compostable
 */
@Mixin(ComposterBlock.class)
public abstract class CompostableMixin {
    @Shadow
    private static void registerCompostableItem(float levelIncreaseChance, ItemConvertible item) {
    }

    @Inject(method = "registerDefaultCompostableItems()V", at = @At(value = "TAIL"))
    private static void injectCompostableRegistry(CallbackInfo ci) {
        registerCompostableItem(0.5f, ModItems.WEED_SEED);
        registerCompostableItem(0.65F, ModItems.WEED_BUNDLE);
    }
}
