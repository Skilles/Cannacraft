package com.skilles.cannacraft.util;

import com.skilles.cannacraft.registry.ModMisc;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;

public class HighUtil {
    /**
     * @param duration the duration of the "high" effect
     * @return an integer from 0 to 3 representing the amplifier for the effect
     */
    public static int durationToAmplifier(int duration) {
        if(duration <= 1200) {
            return 0;
        } else if(duration <= 1800) {
            return 1;
        } else if (duration <= 2400) {
            return 2;
        } else {
            return 3;
        }
    }
    /**
     * Sends a player a message according to how high they are
     * @param player the player to send the message to
     */
    private static void sendHighMessage(PlayerEntity player) {
        StatusEffectInstance currentEffect = player.getStatusEffect(ModMisc.HIGH);
        int amplifier = currentEffect.getAmplifier();
        switch(amplifier) {
            case 0:
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, currentEffect.getDuration(), 0, true,false), null);
                player.sendMessage(new LiteralText("The buzz has made you resistant to fire").formatted(Formatting.GREEN), true);
                break;
            case 1:
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE, currentEffect.getDuration(), 0, true, false), null);
                player.sendMessage(new LiteralText("Why are your hands shaking").formatted(Formatting.GREEN), true);
                break;
            case 2:
                if (MiscUtil.random().nextInt(2) == 0) {
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, currentEffect.getDuration(), 0, true,false), null);
                    player.sendMessage(new LiteralText("You feel stronger for some reason").formatted(Formatting.GREEN), true);
                } else {
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, currentEffect.getDuration(), 0, true, false), null);
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, currentEffect.getDuration(), 0, true, false), null);
                    player.sendMessage(new LiteralText("You feel like you're floating").formatted(Formatting.GREEN), true);
                }
                break;
            case 3:
                if (MiscUtil.random().nextInt(2) == 0) {
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, currentEffect.getDuration(), 0, true, false), null);
                    player.sendMessage(new LiteralText("Sonic").formatted(Formatting.GREEN), true);
                } else {
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, currentEffect.getDuration(), 0, true, false), null);
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.HUNGER, currentEffect.getDuration(), 0, true, false), null);
                    player.sendMessage(new LiteralText("You could really use some oreos").formatted(Formatting.GREEN), true);
                }
                break;
            default:
                break;
        }
    }

    /**
     * @param user entity to
     */
    public static void applyHigh(LivingEntity user) {
        int duration;
        int amplifier;
        int switchNum = 0;
        assert user.getActiveItem().hasNbt();
        int index = user.getActiveItem().getSubNbt("cannacraft:strain").getInt("ID");
        int thc = user.getActiveItem().getSubNbt("cannacraft:strain").getInt("THC");
        ModMisc.PLAYER.get(user).setStrain(index);
        if(thc <= 18) switchNum = 1;
        if(19 <= thc && thc <= 25) switchNum = 2;
        if(26 <= thc) switchNum = 3;
        duration = switch (switchNum) {
            case 1 -> 1200;
            case 2 -> 1800;
            case 3 -> 2400;
            default -> 0;
        };
        if(user.hasStatusEffect(ModMisc.HIGH)) {
            StatusEffectInstance currentEffect = user.getStatusEffect(ModMisc.HIGH);
            duration = switch (switchNum) {
                case 1 -> currentEffect.getDuration() + 600;
                case 2 -> currentEffect.getDuration() + 1200;
                case 3 -> currentEffect.getDuration() + 1800;
                default -> 0;
            };
        }
        amplifier = durationToAmplifier(duration);
        if(amplifier >  2 && !user.world.isDay()) {
            BlockPos pos = user.getBlockPos();
            //BlockState blockState = Blocks.GREEN_BED.getDefaultState();
            //user.world.setBlockState(pos, blockState.with(BedBlock.OCCUPIED, true), 4);
            user.setSleepingPosition(pos);
            //((PlayerEntityAccessor)user).setSleepTimer(100);
            ((ServerWorld) user.world).setTimeOfDay(6000);
            user.sendSystemMessage(Text.of("You pass out and awake the next day"), Util.NIL_UUID);
        }
        user.addStatusEffect(new StatusEffectInstance(ModMisc.HIGH, duration, amplifier, true, false));
        sendHighMessage((PlayerEntity) user);
    }
}
