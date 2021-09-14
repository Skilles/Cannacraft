package com.skilles.cannacraft.misc;

import com.mojang.blaze3d.systems.RenderSystem;
import com.skilles.cannacraft.Cannacraft;
import com.skilles.cannacraft.strain.Strain;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class WeedToast implements Toast {

    private final Type type;
    private final Text title;
    private final Text description;
    private Visibility visibility;
    private long lastTime;
    private float lastProgress;
    private float progress;
    private final boolean hasProgressBar;

    public WeedToast(Type type, Strain strain) {
        this.visibility = Visibility.SHOW;
        this.type = type;
        this.title = type.title();
        this.description = new LiteralText(strain.name()).formatted(strain.getRarity().formatting);
        this.hasProgressBar = true;
    }

    public Visibility draw(MatrixStack matrices, ToastManager manager, long startTime) {
        RenderSystem.setShaderTexture(0, Cannacraft.id("textures/gui/toasts.png"));
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        manager.drawTexture(matrices, 0, 0, 0, 96, this.getWidth(), this.getHeight());
        this.type.drawIcon(matrices, manager, 6, 6);
        if (this.description == null) {
            manager.getGame().textRenderer.draw(matrices, this.title, 30.0F, 12.0F, -11534256);
        } else {
            manager.getGame().textRenderer.draw(matrices, this.title, 30.0F, 7.0F, -11534256);
            manager.getGame().textRenderer.draw(matrices, this.description, 30.0F, 18.0F, -16777216);
        }

        if (this.hasProgressBar) {
            DrawableHelper.fill(matrices, 3, 28, 157, 29, -1);
            float f = MathHelper.clampedLerp(this.lastProgress, this.progress, (float)(startTime - this.lastTime) / 100.0F);
            int j;
            if (this.progress >= this.lastProgress) {
                j = -16755456;
            } else {
                j = -11206656;
            }

            DrawableHelper.fill(matrices, 3, 28, (int)(3.0F + 154.0F * f), 29, j);
            this.lastProgress = f;
            this.lastTime = startTime;
        }

        this.progress += 0.002F;
        if (this.progress >= 1) {
            this.hide();
        }

        return this.visibility;
    }

    public void hide() {
        this.visibility = Visibility.HIDE;
    }

    public void setProgress(float progress) {
        this.progress = progress;
    }

    @Environment(EnvType.CLIENT)
    public static enum Type {
//        NULL1(0, 0),
//        NULL2(1, 0),
//        NULL3(2, 0),
        DISCOVER(0, 1, "New Strain Discovered"),
        CREATE(1, 1, "New Strain Created");
//        NULL4(2, 1),
//        NULL5(3, 1);

        private final int textureSlotX;

        private final int textureSlotY;

        private final String title;

        private Type(int textureSlotX, int textureSlotY, String title) {
            this.textureSlotX = textureSlotX;
            this.textureSlotY = textureSlotY;
            this.title = title;
        }

        public void drawIcon(MatrixStack matrices, DrawableHelper helper, int x, int y) {
            RenderSystem.enableBlend();
            helper.drawTexture(matrices, x, y, 176 + this.textureSlotX * 20, this.textureSlotY * 20, 20, 20);
            RenderSystem.enableBlend();
        }

        public Text title() {
            return new LiteralText(this.title);
        }
    }
}
