package com.skilles.cannacraft.mixins;

import com.skilles.cannacraft.registry.ModItems;
import com.skilles.cannacraft.util.StrainUtil;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.skilles.cannacraft.Cannacraft.log;

/**
 * Catches dropped items and assigns random thc and ID if needed.
 * In the future, can do some custom behavior with dropped items...
 */
// TODO: add different drops for different players...
@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin {

    @Shadow public abstract ItemStack getStack();

    @Inject(at = @At("TAIL"), method = "<init>(Lnet/minecraft/world/World;DDDLnet/minecraft/item/ItemStack;)V")
    public void ItemEntity(World world, double x, double y, double z, ItemStack stack, CallbackInfo ci) {
        if(stack.getItem().equals(ModItems.WEED_SEED) || stack.getItem().equals(ModItems.WEED_BUNDLE)) {
            NbtCompound tag = stack.getSubNbt("cannacraft:strain");
            if(tag != null && tag.contains("ID")) {
                if(tag.getInt("ID") == 0) {
                    log("Changing tag: " + tag);
                    StrainUtil.randomStrain(tag);
                }
            }
        }
    }
}

