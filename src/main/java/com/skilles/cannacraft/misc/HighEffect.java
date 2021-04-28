package com.skilles.cannacraft.misc;

import com.skilles.cannacraft.mixins.StatusEffectAccessor;
import com.skilles.cannacraft.registry.ModMisc;
import com.skilles.cannacraft.strain.GeneticsManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffectType;
import net.minecraft.entity.player.PlayerEntity;

public class HighEffect extends StatusEffect {

    public HighEffect() {
        super(StatusEffectType.BENEFICIAL, 0x98D982);
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return true;
    }

    @Override
    public void applyUpdateEffect(LivingEntity entity, int amplifier) {
        if (entity instanceof PlayerEntity) {
            StatusEffectInstance currentEffect = entity.getStatusEffect(this);
            if (currentEffect.getDuration() % 600 == 0) {
                ((StatusEffectAccessor) currentEffect).setAmplifier(GeneticsManager.durationToAmplifier(currentEffect.getDuration()));
            }
        }
    }

    @Override
    public void onRemoved(LivingEntity entity, AttributeContainer attributes, int amplifier) {
        entity.setGlowing(false);
    }


    @Override
    public void onApplied(LivingEntity entity, AttributeContainer attributes, int amplifier) {
        if (entity instanceof PlayerEntity) {
            int id = ModMisc.PLAYER.get(entity).getStrain().id();
            entity.setGlowing(true);
            System.out.println(id);
            switch (id) {
                case 0:
                    entity.kill();
                default:
                    System.out.println(ModMisc.PLAYER.get(entity).getStrain());
                    break;
            }
        }
    }
}

