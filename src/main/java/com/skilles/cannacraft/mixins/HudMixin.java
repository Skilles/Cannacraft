package com.skilles.cannacraft.mixins;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.skilles.cannacraft.registry.ModMisc;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class HudMixin {

    @Shadow @Final
    private MinecraftClient client;

    @Shadow
    private int scaledHeight;

    @Shadow private int scaledWidth;

    @Shadow @Final private static Identifier VIGNETTE_TEXTURE;

    @Shadow protected abstract void renderPortalOverlay(float nauseaStrength);

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;lerp(FFF)F"))
    private void renderTint(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        ClientPlayerEntity player = this.client.player;
        if (player != null && this.client.interactionManager != null) {
            if (isHigh(player)) {
                this.renderHighTint(player);
            }
        }
    }

    private boolean isHigh(PlayerEntity player) {
        return player.hasStatusEffect(ModMisc.HIGH);
    }

    private void renderHighTint(ClientPlayerEntity player) {
        if (isHigh(player)) {
            float threshold = (float)(1 + player.getStatusEffect(ModMisc.HIGH).getAmplifier())/2;
            float f = threshold - threshold/2; //(threshold-1) / threshold + 1.0F / threshold * 2.0F;
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.ZERO, GlStateManager.DstFactor.ONE_MINUS_SRC_COLOR, GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ZERO);
            RenderSystem.setShaderColor(0.5F, f, f, 1.0F);

            this.client.getTextureManager().bindTexture(VIGNETTE_TEXTURE);
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferBuilder = tessellator.getBuffer();
            bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
            bufferBuilder.vertex(0.0D, this.scaledHeight, -90.0D).texture(0.0F, 1.0F).next();
            bufferBuilder.vertex(this.scaledWidth, this.scaledHeight, -90.0D).texture(1.0F, 1.0F).next();
            bufferBuilder.vertex(this.scaledWidth, 0.0D, -90.0D).texture(1.0F, 0.0F).next();
            bufferBuilder.vertex(0.0D, 0.0D, -90.0D).texture(0.0F, 0.0F).next();
            tessellator.draw();
            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
            RenderSystem.setShaderColor(0.1F, f, f, 1.0F);
            RenderSystem.defaultBlendFunc();
        }
    }
}
