package com.skilles.cannacraft.mixins;

import com.skilles.cannacraft.items.StrainItem;
import com.skilles.cannacraft.strain.StrainInfo;
import com.skilles.cannacraft.util.DnaUtil;
import com.skilles.cannacraft.util.WeedRegistry;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
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
        if (stack.getItem() instanceof StrainItem) {
            StrainInfo info = WeedRegistry.getStrainInfo(stack);
            if (stack.hasNbt() && info.strain().id() == 0) {
                log("Changing tag: " + stack.getNbt());
                WeedRegistry.StatusTypes status = null;
                if (WeedRegistry.WeedTypes.fromStack(stack).equals(WeedRegistry.WeedTypes.BUNDLE)) {
                    status = WeedRegistry.StatusTypes.WET;
                }
                stack.setNbt(DnaUtil.generateNbt(DnaUtil.randomGenome(), false, status));
                // stack.setNbt(WeedRegistry.randomItem(WeedRegistry.WeedTypes.fromStack(stack), false, false).getNbt());
            }
        }
    }
}

