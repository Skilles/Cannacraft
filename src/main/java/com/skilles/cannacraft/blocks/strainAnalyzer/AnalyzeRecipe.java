package com.skilles.cannacraft.blocks.strainAnalyzer;

import com.google.gson.JsonObject;
import com.skilles.cannacraft.Cannacraft;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

public class AnalyzeRecipe implements Recipe <StrainAnalyzerEntity>{
    private final Identifier id;
    private final Ingredient ingredient;
    private final ItemStack output;

    public AnalyzeRecipe(Identifier id, Ingredient ingredient, ItemStack output) {
        this.id = id;
        this.ingredient = ingredient;
        this.output = output;
    }

    @Override
    public boolean matches(StrainAnalyzerEntity inv, World world) {
        return ingredient.test(inv.getStack(1));
    }

    @Override
    public ItemStack craft(StrainAnalyzerEntity inv) {
        if (inv.getStack(1).getCount() >= 1) {
            NbtCompound tag = output.getOrCreateSubTag("cannacraft:strain").copyFrom(inv.getStack(1).getOrCreateSubTag("cannacraft:strain"));
            output.setTag(tag);
            return output;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean fits(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getOutput() {
        return output;
    }

    @Override
    public Identifier getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Cannacraft.ANALYZE_RECIPE_SERIALIZER;
    }

    @Override
    public RecipeType<?> getType() {
        return Cannacraft.ANALYZE_RECIPE;
    }


    public static class AnalyzeRecipeSerializer implements RecipeSerializer<AnalyzeRecipe> {
        @Override
        public AnalyzeRecipe read(Identifier id, JsonObject json) {
            Ingredient ingredient = Ingredient.fromJson(JsonHelper.getObject(json, "ingredient"));
            String outputName = JsonHelper.getString(json, "result");
            ItemStack output = new ItemStack(Registry.ITEM.getOrEmpty(new Identifier(outputName)).orElseThrow(() ->
                    new IllegalStateException("Item: " + outputName + " does not exist")
            ));
            return new AnalyzeRecipe(id, ingredient, output);
        }

        @Override
        public AnalyzeRecipe read(Identifier id, PacketByteBuf buf) {
            Ingredient ingredient = Ingredient.fromPacket(buf);
            ItemStack output = buf.readItemStack();

            return new AnalyzeRecipe(id, ingredient, output);
        }

        @Override
        public void write(PacketByteBuf buf, AnalyzeRecipe recipe) {
            recipe.ingredient.write(buf);
            buf.writeItemStack(recipe.output);
        }
    }
}
