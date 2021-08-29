package com.skilles.cannacraft.mixins;

import com.skilles.cannacraft.registry.ModItems;
import com.skilles.cannacraft.registry.ModMisc;
import net.fabricmc.fabric.api.util.TriState;
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
        if(this.id.equals(id("weed_joint"))) {
            int slotId = 0;
            for(int i = 0; i < craftingInventory.size(); i++) {
                if(craftingInventory.getStack(i).isOf(ModItems.WEED_BUNDLE)) slotId = i;
            }
            ItemStack input = craftingInventory.getStack(slotId).copy();
            ItemStack output = this.getOutput().copy();
            if(input.hasNbt()) {
                output.setSubNbt("cannacraft:strain", input.getSubNbt("cannacraft:strain"));
                cir.setReturnValue(output);
            }
        } else if(this.id.equals(id("weed_bundle_ground"))) {
            int slotId = 0;
            for(int i = 0; i < craftingInventory.size(); i++) {
                if(craftingInventory.getStack(i).isOf(ModItems.WEED_BUNDLE)) slotId = i;
            }
            ItemStack input = craftingInventory.getStack(slotId).copy();
            ItemStack output = this.getOutput().copy();
            if(input.hasNbt() && input.getSubNbt("cannacraft:strain").getFloat("Status") == 0.5F) {
                output.setSubNbt("cannacraft:strain", input.getSubNbt("cannacraft:strain"));
                ModMisc.STRAIN.get(output).setStatus(TriState.DEFAULT);
                cir.setReturnValue(output);
            }
        }
    }
}
