package com.skilles.cannacraft.items.weedManual_;

import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.apache.logging.log4j.util.TriConsumer;

public class GuiButton extends ButtonWidget {
    private TriConsumer<GuiButton, Double, Double> clickHandler;

    public GuiButton(int x, int y, Text buttonText, ButtonWidget.PressAction pressAction) {
        super(x, y, 20, 200, buttonText, pressAction);
    }

    public GuiButton(int x, int y, int widthIn, int heightIn, Text buttonText, ButtonWidget.PressAction pressAction) {
        super(x, y, widthIn, heightIn, buttonText, pressAction);
    }

    public GuiButton clickHandler(TriConsumer<GuiButton, Double, Double> consumer) {
        clickHandler = consumer;
        return this;
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        if (clickHandler != null) {
            clickHandler.accept(this, mouseX, mouseY);
        }
        super.onClick(mouseY, mouseY);
    }
}
