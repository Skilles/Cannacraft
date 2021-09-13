package com.skilles.cannacraft.mixins;

import com.skilles.cannacraft.registry.ModMisc;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.*;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;

import static com.skilles.cannacraft.Cannacraft.id;
import static com.skilles.cannacraft.util.WeedRegistry.StatusTypes;

@Mixin({SmeltingRecipe.class, CampfireCookingRecipe.class})
public abstract class MiscRecipeMixin extends AbstractCookingRecipe {
    public MiscRecipeMixin(RecipeType<?> type, Identifier id, String group, Ingredient input, ItemStack output, float experience, int cookTime) {
        super(type, id, group, input, output, experience, cookTime);
    }

    @Override
    public ItemStack craft(Inventory inventory) {
        if(this.id.equals(id("weed_brownie"))) {
            int slotId = 0;
            ItemStack input = inventory.getStack(slotId).copy();
            ItemStack output = this.getOutput().copy();
            if(input.hasNbt()) {
                output.setSubNbt("cannacraft:strain", input.getSubNbt("cannacraft:strain"));
                return output;
            }
        } else if(this.id.equals(id("weed_bundle_dry"))) {
            int slotId = 0;
            ItemStack input = inventory.getStack(slotId).copy();
            ItemStack output = this.getOutput().copy();
            if(input.hasNbt() && ModMisc.STRAIN.get(output).getStatus() == StatusTypes.DRY) {
                output.setSubNbt("cannacraft:strain", input.getSubNbt("cannacraft:strain"));
                ModMisc.STRAIN.get(output).setStatus(StatusTypes.DRY);
                return output;
            }
        }
        return super.craft(inventory);
    }
}
