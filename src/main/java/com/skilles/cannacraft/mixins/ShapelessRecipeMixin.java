package com.skilles.cannacraft.mixins;

import com.skilles.cannacraft.registry.ModItems;
import com.skilles.cannacraft.util.WeedRegistry;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.ShapelessRecipe;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.skilles.cannacraft.Cannacraft.id;

/**
 * Copies strain NBT in crafting recipes
 */
@Mixin(ShapelessRecipe.class)
public abstract class ShapelessRecipeMixin {

    @Shadow public abstract ItemStack getOutput();

    @Shadow @Final private Identifier id;

    @Inject(method = "craft", at = @At(value = "RETURN"), cancellable = true)
    public void inject(CraftingInventory craftingInventory, CallbackInfoReturnable<ItemStack> cir) {
        if(WeedRegistry.WeedTypes.isOf(this.getOutput())) {
            int slotId = 0;
            for(int i = 0; i < craftingInventory.size(); i++) {
                if(WeedRegistry.WeedTypes.isOf(craftingInventory.getStack(i))) slotId = i;
            }
            ItemStack input = craftingInventory.getStack(slotId).copy();
            ItemStack output = this.getOutput().copy();
            if(input.hasNbt()) {
                output.setSubNbt("cannacraft:strain", input.getSubNbt("cannacraft:strain"));
                if(output.getItem() == ModItems.WEED_BUNDLE) {
                    if(this.id.equals(id("weed_bundle_ground"))) {
                        if(WeedRegistry.getStatus(input) == WeedRegistry.StatusTypes.DRY) {
                            output.getSubNbt("cannacraft:strain").putFloat("Status", 0.0F);
                            cir.setReturnValue(output);
                        } else {
                            cir.setReturnValue(ItemStack.EMPTY);
                        }
                    }
                } else if(this.id.equals(id("weed_joint"))) {
                    cir.setReturnValue(WeedRegistry.getStatus(input) == WeedRegistry.StatusTypes.GROUND ? output : ItemStack.EMPTY);
                } else {
                    cir.setReturnValue(output);
                }
            }
        }
    }
}
