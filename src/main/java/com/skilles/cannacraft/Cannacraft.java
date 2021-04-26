package com.skilles.cannacraft;

import com.skilles.cannacraft.registry.*;
import com.skilles.cannacraft.strain.StrainMap;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public class Cannacraft implements ModInitializer {

    public static final String MOD_ID = "cannacraft";

    public static final ItemGroup ITEM_GROUP = FabricItemGroupBuilder.build(
            id("general"),
            () -> new ItemStack(ModItems.WEED_FRUIT));

    public static Identifier id(String path) {
        return new Identifier(MOD_ID, path);
    }


    @Override
    public void onInitialize() {

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

        ModMisc.registerEffects();
        System.out.println("Effects registered!");

        ModScreens.registerScreenHandlers();
        System.out.println("ScreenHandlers registered!");


    }
}
