package com.skilles.cannacraft.items;

import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

public class BrownieMix extends StrainItem {
    public BrownieMix(Settings settings) {
        super(settings);
    }

    @Override
    public Text getName(ItemStack stack) {
        return new TranslatableText("item.cannacraft.brownie_mix");
    }
}
