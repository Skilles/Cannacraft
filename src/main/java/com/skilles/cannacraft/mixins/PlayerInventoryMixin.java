package com.skilles.cannacraft.mixins;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import org.spongepowered.asm.mixin.Mixin;

// hopefully doesn't break anything
@Mixin(PlayerInventory.class)
public abstract class PlayerInventoryMixin implements Inventory {
    public int getMaxCountPerStack() {
        return 128;
    }
}
