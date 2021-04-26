package com.skilles.cannacraft.mixins;

import net.minecraft.entity.effect.StatusEffectInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(StatusEffectInstance.class)
public interface StatusEffectAccessor {
    @Accessor("amplifier")
    void setAmplifier(int amplifier);

    @Accessor("duration")
    void setDuration(int duration);
}
