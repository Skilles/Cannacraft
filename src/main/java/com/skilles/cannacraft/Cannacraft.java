package com.skilles.cannacraft;

import com.skilles.cannacraft.blocks.strainAnalyzer.AnalyzeRecipe;
import com.skilles.cannacraft.registry.*;
import com.skilles.cannacraft.strain.StrainMap;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.Optional;

public class Cannacraft implements ModInitializer {

    public static final String MOD_ID = "cannacraft";

    public static final ItemGroup ITEM_GROUP = FabricItemGroupBuilder.build(
            id("general"),
            () -> new ItemStack(ModItems.WEED_FRUIT));
    public static final RecipeType<AnalyzeRecipe> ANALYZE_RECIPE = new RecipeType<AnalyzeRecipe>() {
        @Override
        public <C extends Inventory> Optional<AnalyzeRecipe> get(Recipe<C> recipe, World world, C inventory) {
            return recipe.matches(inventory, world) ? Optional.of((AnalyzeRecipe) recipe) : Optional.empty();
        }
    };
    public static final AnalyzeRecipe.AnalyzeRecipeSerializer ANALYZE_RECIPE_SERIALIZER = new AnalyzeRecipe.AnalyzeRecipeSerializer();
    public static Identifier id(String path) {
        return new Identifier(MOD_ID, path);
    }


    @Override
    public void onInitialize() {
        //Registry.register(Registry.RECIPE_TYPE, id("analyze"), ANALYZE_RECIPE);
        //Registry.register(Registry.RECIPE_SERIALIZER, id("analyze"), ANALYZE_RECIPE_SERIALIZER);

        StrainMap.registerStrains();

        ModCommands.registerCommands();
        System.out.println("Commands registered!");

        LootTable.registerLoot();
        System.out.println("LootTables registered!");

        ModItems.registerItems();
        System.out.println("Items registered!");

        ModEntities.registerEntities();
        System.out.println("Entities registered!");

        ModBlocks.registerBlocks();
        System.out.println("Blocks registered!");

        ModScreens.registerScreenHandlers();
        System.out.println("ScreenHandlers registered!");


    }
}
