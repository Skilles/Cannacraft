package com.skilles.cannacraft.items;

import com.skilles.cannacraft.Cannacraft;
import net.minecraft.item.FlintAndSteelItem;
import net.minecraft.item.Item;

public class WeedLighter extends FlintAndSteelItem {
    public WeedLighter() {
        super(new Item.Settings().group(Cannacraft.ITEM_GROUP).maxCount(1).maxDamage(1000));
    }

}
