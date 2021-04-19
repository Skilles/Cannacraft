package com.skilles.cannacraft.mixins;

import com.skilles.cannacraft.items.ItemStrainComponent;
import com.skilles.cannacraft.registry.ModItems;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Catches dropped items and assigns random thc if needed.
 * In the future, can do some custom behavior with dropped items...
 */

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin {

    @Shadow public abstract ItemStack getStack();

    @Inject(at = @At("TAIL"), method = "<init>(Lnet/minecraft/world/World;DDDLnet/minecraft/item/ItemStack;)V")
    public void ItemEntity(World world, double x, double y, double z, ItemStack stack, CallbackInfo ci) {
        if(stack.getItem().equals(ModItems.WEED_SEED)) {
            NbtCompound tag = stack.getSubTag("cannacraft:strain");
            if(tag != null && !tag.contains("THC")) {
                tag.putInt("THC", ItemStrainComponent.normalDist(15, 5, 13));
                this.getStack().putSubTag("cannacraft:strain", tag);
            }
        }
    }
}

