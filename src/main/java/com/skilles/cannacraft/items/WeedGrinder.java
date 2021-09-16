package com.skilles.cannacraft.items;

import com.skilles.cannacraft.Cannacraft;
import net.minecraft.item.Item;

public class WeedGrinder extends Item {
    public WeedGrinder() {
        super(new Item.Settings().group(Cannacraft.ITEM_GROUP).maxCount(1));
    }
}
