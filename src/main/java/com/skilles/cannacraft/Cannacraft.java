package com.skilles.cannacraft;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.skilles.cannacraft.blocks.strainAnalyzer.AnalyzeRecipe;
import com.skilles.cannacraft.registry.*;
import com.skilles.cannacraft.strain.StrainMap;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

import java.util.Optional;

import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

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

    public static int setStrain(CommandContext<ServerCommandSource> ctx, int strain) throws CommandSyntaxException {
        final ServerPlayerEntity self = ctx.getSource().getPlayer();
        ItemStack itemStack = self.getMainHandStack();
        if(itemStack.getItem().equals(ModItems.WEED_SEED) || itemStack.getItem().equals(ModItems.WEED_FRUIT)){
            //NbtCompound tag = itemStack.getOrCreateSubTag("cannacraft:strain");
            ModComponents.STRAIN.get(itemStack).setStrain(strain); // BUG: index NBT is null when set to 0
            //itemStack.putSubTag("cannacraft:strain", tag);
        }
        return 1;
    }
    public static int identify(CommandContext<ServerCommandSource> ctx, int flag) throws CommandSyntaxException {
        final ServerPlayerEntity self = ctx.getSource().getPlayer();
        ItemStack handStack = self.getMainHandStack();
        if(handStack.getItem().equals(ModItems.WEED_SEED) || handStack.getItem().equals(ModItems.WEED_FRUIT)) {
            if(flag == 0) {
                ModComponents.STRAIN.get(handStack).identify();
                System.out.println("Seed identified!");
                return 1;
            } else {
                int i;
                int j = 0;
                for(i = 0; self.getInventory().size() > i; i++) {
                    ItemStack itemStack = self.getInventory().getStack(i);
                    if (itemStack != null && itemStack.getItem().equals(ModItems.WEED_SEED) && !ModComponents.STRAIN.get(itemStack).identified()) {
                        ModComponents.STRAIN.get(itemStack).identify();
                        j++;
                    }
                }
                System.out.println(j+" seeds identified!");
            }
        }
        return 1;
    }
    public static int addStrain(CommandContext<ServerCommandSource> ctx, String name, String type) throws CommandSyntaxException {
        final ServerPlayerEntity self = ctx.getSource().getPlayer();
        StrainMap.addStrain(name, StrainMap.Type.valueOf(type.toUpperCase()));
        self.sendSystemMessage(Text.of("Strain added: "+StrainMap.toStrain(name)), Util.NIL_UUID);
        return 1;
    }
    public static int listStrain(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        final ServerPlayerEntity self = ctx.getSource().getPlayer();
        self.sendSystemMessage(Text.of(StrainMap.getStrains().toString()), Util.NIL_UUID);
        return 1;
    }
    @Override
    public void onInitialize() {
        Registry.register(Registry.RECIPE_TYPE, id("analyze"), ANALYZE_RECIPE);
        Registry.register(Registry.RECIPE_SERIALIZER, id("analyze"), ANALYZE_RECIPE_SERIALIZER);

        LootTable.registerLoot();
        System.out.println("LootTables registered!");


        StrainMap.registerStrains();

        //System.out.println("Strains initialized = "+StrainMap.getStrainCount());

        ModItems.registerItems();
        System.out.println("Items registered!");

        ModEntities.registerEntities();
        System.out.println("Entities registered!");

        ModBlocks.registerBlocks();
        System.out.println("Blocks registered!");

        ModScreens.registerScreenHandlers();
        System.out.println("ScreenHandlers registered!");

        CommandRegistrationCallback.EVENT.register((dispatcher, integrated) -> {
            dispatcher.register(literal("weed")
                    .then(literal("identify")
                            .executes(ctx -> {
                                identify(ctx, 0);
                                return 1;
                                }).then(literal("all")
                                    .executes(ctx -> {
                                    identify(ctx, 1);
                                    return 1;
                                    })))
                    .then(literal("strain")
                        .then(literal("set")
                            .then(argument("index", IntegerArgumentType.integer(0, StrainMap.getStrainCount()))
                                .executes(ctx -> {
                                    System.out.println("Seed strain set!");
                                    setStrain(ctx, getInteger(ctx, "index"));
                                    return 1;
            })))
                            .then(literal("add")
                                    .then(argument("name", StringArgumentType.string())
                                            .then(argument("type", StringArgumentType.string())
                                .executes(ctx -> {
                                    addStrain(ctx, getString(ctx, "name"), getString(ctx, "type"));
                                    return 1;
                                })
            ))))
                            .then(literal("list")
                                    .executes(ctx -> {
                                        listStrain(ctx);
                                    return 1;
                    }))
            );
        });
    }
}
