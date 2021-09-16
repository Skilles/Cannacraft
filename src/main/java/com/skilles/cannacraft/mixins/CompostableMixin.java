package com.skilles.cannacraft.mixins;

import net.minecraft.block.ComposterBlock;
import net.minecraft.item.ItemConvertible;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * Allows registry to add items to the composter block's list of compostable items.
 */
@Mixin(ComposterBlock.class)
public interface CompostableMixin {

    @Invoker("registerCompostableItem")
    static void registerCompostableItem(float levelIncreaseChance, ItemConvertible item) {
        throw new AssertionError();
    }
}
