package com.skilles.cannacraft.mixins;

import com.google.common.annotations.Beta;
import com.google.common.collect.Ordering;
import com.mojang.blaze3d.systems.RenderSystem;
import com.skilles.cannacraft.registry.ModMisc;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.StatusEffectSpriteManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.stream.Collectors;

/**
 * Minify status effect icons if high (god why)
 * TODO: implement only for effects given by high
 * TODO: control offset with variables
 */
@Beta
@Mixin(AbstractInventoryScreen.class)
public abstract class InventoryScreenMixin {
    @Unique
    StatusEffect backgroundType;
    @Unique
    StatusEffect spriteType;
    @Unique
    boolean isHigh;
    /**
     * Replaces HungerMixin
     **/
    @ModifyVariable(method = "drawStatusEffectDescriptions(Lnet/minecraft/client/util/math/MatrixStack;IILjava/lang/Iterable;)V", at = @At(value = "STORE"), ordinal = 0)
    private String munchies(String string) {
        if(isHigh && string.equalsIgnoreCase(I18n.translate("effect.minecraft.hunger"))) {
            return I18n.translate("effect.cannacraft.hunger");
        } else {
            return string;
        }
    }
    /**
     * Checks whether player is high
     */
    @Inject(method = "drawStatusEffects(Lnet/minecraft/client/util/math/MatrixStack;)V", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderColor(FFFF)V"), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void setHigh(MatrixStack matrices, CallbackInfo ci, int i, Collection<StatusEffectInstance> collection) {
        if(collection.stream().anyMatch(obj -> obj.getEffectType().equals(ModMisc.HIGH))) {
            isHigh = true;
        }
    }
    /**
     * Sets the spacing between the effect boxes
     */
    @ModifyVariable(method = "drawStatusEffects(Lnet/minecraft/client/util/math/MatrixStack;)V", at = @At("STORE"), ordinal = 1)
    private int setJ(int j) {
        return isHigh ? 26 : 33;
    }
    /**
     * These injects capture local variables for comparison in later method calls
     */
    @Inject(method = "drawStatusEffectBackgrounds(Lnet/minecraft/client/util/math/MatrixStack;IILjava/lang/Iterable;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/AbstractInventoryScreen;drawTexture(Lnet/minecraft/client/util/math/MatrixStack;IIIIII)V"), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void captureBackground(MatrixStack matrices, int i, int j, Iterable<StatusEffectInstance> iterable, CallbackInfo ci, int k, Iterator var6, StatusEffectInstance statusEffectInstance) {
        if(isHigh) {
            backgroundType = statusEffectInstance.getEffectType();
        }
    }
    @Inject(method = "drawStatusEffectSprites(Lnet/minecraft/client/util/math/MatrixStack;IILjava/lang/Iterable;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/AbstractInventoryScreen;drawSprite(Lnet/minecraft/client/util/math/MatrixStack;IIIIILnet/minecraft/client/texture/Sprite;)V"), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void captureSprite(MatrixStack matrices, int i, int j, Iterable<StatusEffectInstance> iterable, CallbackInfo ci, StatusEffectSpriteManager statusEffectSpriteManager, int k, Iterator var7, StatusEffectInstance statusEffectInstance, StatusEffect statusEffect, Sprite sprite) {
        if(isHigh) {
            spriteType = statusEffect;
        }
    }
    @Inject(method = "drawStatusEffectBackgrounds(Lnet/minecraft/client/util/math/MatrixStack;IILjava/lang/Iterable;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/AbstractInventoryScreen;drawTexture(Lnet/minecraft/client/util/math/MatrixStack;IIIIII)V"))
    private void drawNewTexture(CallbackInfo ci) {
        if(isHigh && !backgroundType.equals(ModMisc.HIGH)) RenderSystem.setShaderTexture(0, new Identifier("cannacraft","textures/gui/container/background.png"));
    }
    /**
     * These methods adjust the offset for the sprite, background, and texts
     */
    @ModifyArgs(method = "drawStatusEffectBackgrounds(Lnet/minecraft/client/util/math/MatrixStack;IILjava/lang/Iterable;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/AbstractInventoryScreen;drawTexture(Lnet/minecraft/client/util/math/MatrixStack;IIIIII)V"))
    private void background(Args args) {
        if (isHigh && !backgroundType.equals(ModMisc.HIGH)) {
            int k = args.get(2);
            args.set(2, k + 5);
            args.set(4, 0);
            args.set(5, 110);
            args.set(6, 26);
        }
    }
    @ModifyArgs(method = "drawStatusEffectSprites(Lnet/minecraft/client/util/math/MatrixStack;IILjava/lang/Iterable;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/AbstractInventoryScreen;drawSprite(Lnet/minecraft/client/util/math/MatrixStack;IIIIILnet/minecraft/client/texture/Sprite;)V"))
    private void sprite(Args args) {
        if(isHigh && !spriteType.equals(ModMisc.HIGH)) {
            int width = args.get(4);
            int height = args.get(5);
            int y = args.get(2);
            int x = args.get(1);
            args.set(4, width / 2);
            args.set(5, height / 2);
            args.set(2, y + 7);
            args.set(1, x + 2);
        }
    }
    /**
     * This offset modifier requires more logic in order to also offset the duration
     * Not sure if better than redirect
     */
    @Unique
    int runTime = 0; // run instance
    @Unique
    boolean contains = false;
    @ModifyArgs(method = "drawStatusEffectDescriptions(Lnet/minecraft/client/util/math/MatrixStack;IILjava/lang/Iterable;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/font/TextRenderer;drawWithShadow(Lnet/minecraft/client/util/math/MatrixStack;Ljava/lang/String;FFI)I"))
    private void text(Args args) {
        if(isHigh) {
            String name = args.get(1);
            String highName = I18n.translate("effect.cannacraft.high");
            float x = args.get(2);
            float y = args.get(3);
            if((int) args.get(4) == 8355711) {
                runTime = 2;
            } else {
                runTime = 1;
            }
            if(name.contains(highName)){
                contains = true;
            } else if(runTime == 1){
                contains = false;
            }
            if(!contains){
                args.set(2, x-5);
                args.set(3, y + 4);
                if(runTime == 2) {
                    args.set(2, x-5);
                    args.set(3, y + 4);
                    contains = false;
                }
            }
        }
    }
}
