package com.skilles.cannacraft.items;

import com.skilles.cannacraft.Cannacraft;
import com.skilles.cannacraft.registry.ModMisc;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;

public class WeedBrownie extends StrainItem {
    public WeedBrownie() {
        super(new Item.Settings().group(Cannacraft.ITEM_GROUP).food(new FoodComponent.Builder().hunger(4).alwaysEdible().saturationModifier(2.0F).statusEffect(new StatusEffectInstance(ModMisc.HIGH, 1000, 2), 1.0F).statusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 200, 1), 0.2F).build()));
    }

}
