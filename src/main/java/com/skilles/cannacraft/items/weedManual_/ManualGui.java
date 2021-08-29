package com.skilles.cannacraft.items.weedManual_;

import com.skilles.cannacraft.Cannacraft;
import net.minecraft.client.gui.screen.ConfirmChatLinkScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

public class ManualGui extends Screen {
    WeedManual manual;
    PlayerEntity player;

    private static final Identifier texture = Cannacraft.id("textures/gui/manual.png");
    int guiWidth = 207;
    int guiHeight = 195;
    private static final Text text1 = new TranslatableText("cannacraft.manual.wiki");
    private static final Text text2 = new TranslatableText("cannacraft.manual.discord");

    public ManualGui(PlayerEntity player) {
        super(new LiteralText("gui.manual"));
        this.player = player;
    }

    @Override
    public void init() {
        int y = (height / 2) - guiHeight / 2;
        y += 40;
        addSelectableChild(new GuiButton((width / 2 - 30), y + 10, 60, 20, new TranslatableText("cannacraft.manual.wikibtn"), var1 -> client.setScreen(new ConfirmChatLinkScreen(t -> {
            if (t) {
                Util.getOperatingSystem().open("http://wiki.techreborn.ovh");
            }
            this.client.setScreen(this);
        }, "http://wiki.techreborn.ovh", false))));
        addSelectableChild(new GuiButton((width / 2 - 30), y + 60, 60, 20, new TranslatableText("cannacraft.manual.discordbtn"), var1 -> client.setScreen(new ConfirmChatLinkScreen(t -> {
            if (t) {
                Util.getOperatingSystem().open("https://discord.gg/teamreborn");
            }
            this.client.setScreen(this);
        }, "https://discord.gg/teamreborn", false))));
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        renderBackground(matrixStack);
        client.getTextureManager().bindTexture(ManualGui.texture);
        int centerX = (width / 2) - guiWidth / 2;
        int centerY = (height / 2) - guiHeight / 2;
        drawTexture(matrixStack, centerX, centerY, 0, 0, guiWidth, guiHeight);
        textRenderer.draw(matrixStack, text1, ((width / 2) - textRenderer.getWidth(text1) / 2), centerY + 40, 4210752);
        textRenderer.draw(matrixStack, text2, ((width / 2) - textRenderer.getWidth(text2) / 2), centerY + 90, 4210752);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }
}
