package com.skilles.cannacraft.mixins;

import com.skilles.cannacraft.registry.ModItems;
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

/**
 * Copies strain NBT in crafting recipes
 */
@Mixin(ShapelessRecipe.class)
public abstract class ShapelessRecipeMixin {

    @Shadow public abstract ItemStack getOutput();

    @Shadow @Final private Identifier id;

    @Inject(method = "craft", at = @At(value = "RETURN"), cancellable = true)
    public void inject(CraftingInventory craftingInventory, CallbackInfoReturnable<ItemStack> cir) {
        if(this.id.equals(new Identifier("cannacraft:weed_joint"))) {
            int slotId = 0;
            for(int i = 0; i < craftingInventory.size(); i++) {
                if(craftingInventory.getStack(i).isOf(ModItems.WEED_FRUIT)) slotId = i;
            }
            ItemStack input = craftingInventory.getStack(slotId).copy();
            ItemStack output = this.getOutput().copy();
            if(input.hasTag()) {
                output.putSubTag("cannacraft:strain", input.getSubTag("cannacraft:strain"));
                cir.setReturnValue(output);
            }
        }
    }
}
