package com.skilles.cannacraft.blocks.machines.seedCrosser;

import com.mojang.blaze3d.systems.RenderSystem;
import com.skilles.cannacraft.Cannacraft;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class SeedCrosserScreen extends HandledScreen<SeedCrosserScreenHandler> {

    private static final Identifier TEXTURE = Cannacraft.id("textures/gui/container/seed_crosser.png");

    public SeedCrosserScreen(SeedCrosserScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Override
    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        int i = this.x;
        int j = this.y;
        this.drawTexture(matrices, i, j, 0, 0, this.backgroundWidth, this.backgroundHeight);
        int l = this.handler.getArrowWidth(); // stops at 175
        int energy = this.handler.powerStored() / 161; // 62 max
        this.drawTexture(matrices, i + 78, j + 36, 177, 14, l, 16); // arrow

        // power bar
        this.drawTexture(matrices, i + 150, j + 9,177, 33, 18, 62);
        this.drawTexture(matrices, i + 151, j + 9, 151, 9, 16, 62-energy);

    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        int i = this.x;
        int j = this.y;
        if (mouseX >= i + 150 && mouseX < i + 168 && mouseY >= j + 8 && mouseY < j + 70) { // if hovering over power bar
            renderTooltip(matrices, new LiteralText("Energy: "+this.handler.powerStored()).formatted(Formatting.GOLD), i + 168, j + 20);
        }
        this.renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(matrices, mouseX, mouseY);
    }
    @Override
    protected void init() {
        super.init();
        titleX = (backgroundWidth - textRenderer.getWidth(title)) / 2;
    }
}
