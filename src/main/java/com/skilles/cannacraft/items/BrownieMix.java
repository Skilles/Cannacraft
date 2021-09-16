package com.skilles.cannacraft.items;

import com.skilles.cannacraft.Cannacraft;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

public class BrownieMix extends StrainItem {
    public BrownieMix() {
        super(new Item.Settings().group(Cannacraft.ITEM_GROUP));
    }

    @Override
    public Text getName(ItemStack stack) {
        return new TranslatableText("item.cannacraft.brownie_mix");
    }
}
