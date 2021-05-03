package com.skilles.cannacraft.mixins;

import net.minecraft.entity.effect.StatusEffect;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Hunger -> Munchies
 */
@Deprecated
@Mixin(StatusEffect.class)
public abstract class HungerMixin {
    @Shadow
    @Nullable
    private String translationKey;

    @Inject(at = @At("RETURN"), method = "loadTranslationKey")
    protected void loadTranslationKey(CallbackInfoReturnable<String> cir) {
        if (this.translationKey.equals("effect.minecraft.hunger")) {
            this.translationKey = "effect.cannacraft.hunger";
        }
    }
}
