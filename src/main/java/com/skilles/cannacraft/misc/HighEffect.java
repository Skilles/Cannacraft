package com.skilles.cannacraft.misc;

import com.skilles.cannacraft.mixins.StatusEffectAccessor;
import com.skilles.cannacraft.registry.ModMisc;
import com.skilles.cannacraft.util.HighUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;

import static com.skilles.cannacraft.Cannacraft.log;

public class HighEffect extends StatusEffect {

    public HighEffect() {
        super(StatusEffectCategory.BENEFICIAL, 0x98D982);
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return true;
    }

    @Override
    public void applyUpdateEffect(LivingEntity entity, int amplifier) {
        if (entity instanceof PlayerEntity player) {
            StatusEffectInstance currentEffect = entity.getStatusEffect(this);
            if (currentEffect.getDuration() % 600 == 0) {
                ((StatusEffectAccessor) currentEffect).setAmplifier(HighUtil.durationToAmplifier(currentEffect.getDuration()));
            } else if (currentEffect.getAmplifier() >= 1) {
                /*for (int i = 0; i < MiscUtil.random().nextInt(7) * currentEffect.getAmplifier(); i++) {
                    player.updateTrackedPositionAndAngles(player.getX(), player.getY(), player.getZ(), player.bodyYaw + (MiscUtil.random().nextInt(20) - 10) * currentEffect.getAmplifier(), player.getPitch() + MiscUtil.random().nextInt(5) * currentEffect.getAmplifier(), 100, true);
                }*/
            }
        }
    }

    @Override
    public void onRemoved(LivingEntity entity, AttributeContainer attributes, int amplifier) {
    }
    
    @Override
    public void onApplied(LivingEntity entity, AttributeContainer attributes, int amplifier) {
        if (entity instanceof PlayerEntity) {
            int id = ModMisc.PLAYER.get(entity).getStrain().id();
            log(id);
            switch (id) {
                case 0:
                    entity.kill();
                default:
                    log(ModMisc.PLAYER.get(entity).getStrain());
                    break;
            }
        }
    }
}

