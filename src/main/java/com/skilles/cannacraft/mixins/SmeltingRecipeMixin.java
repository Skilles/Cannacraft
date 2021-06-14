package com.skilles.cannacraft.mixins;

import com.skilles.cannacraft.registry.ModItems;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.AbstractCookingRecipe;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.skilles.cannacraft.Cannacraft.id;

@Mixin(AbstractCookingRecipe.class)
public abstract class SmeltingRecipeMixin {
    @Shadow public abstract ItemStack getOutput();

    @Shadow @Final protected Identifier id;
    @Inject(method = "matches", at = @At(value = "RETURN"), cancellable = true)
    public void matchInject(Inventory inventory, World world, CallbackInfoReturnable<Boolean> cir) {
        if(!inventory.getStack(0).hasTag()) cir.setReturnValue(false);
    }
    @Inject(method = "craft", at = @At(value = "RETURN"), cancellable = true)
    public void inject(Inventory inventory, CallbackInfoReturnable<ItemStack> cir) {
        if(this.id.equals(id("weed_brownie"))) {
            int slotId = 0;
            ItemStack input = inventory.getStack(slotId).copy();
            ItemStack output = this.getOutput().copy();
            if(input.hasTag()) {
                output.putSubTag("cannacraft:strain", input.getSubTag("cannacraft:strain"));
                cir.setReturnValue(output);
            }
        }
    }
}
