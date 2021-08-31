package com.skilles.cannacraft;

import com.skilles.cannacraft.registry.*;
import com.skilles.cannacraft.strain.StrainMap;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Cannacraft implements ModInitializer {

    public static final String MOD_ID = "cannacraft";

    public static final ItemGroup ITEM_GROUP = FabricItemGroupBuilder.build(
            id("general"),
            () -> new ItemStack(ModItems.WEED_JOINT));

    public static Identifier id(String path) {
        return new Identifier(MOD_ID, path);
    }
    public static Logger LOGGER = LogManager.getLogger();

    public static void log(Level level, String message) {
        LOGGER.log(level, "[Cannacraft] "+message);
    }
    public static void log(String message) {
        log(Level.INFO, message);
    }
    public static void log(Object message) {
        log(String.valueOf(message));
    }
    @Override
    public void onInitialize() {
        ModItems.registerItems();
        ModEntities.registerEntities();
        ModBlocks.registerBlocks();
        StrainMap.registerStrains();
        ModCommands.registerCommands();
        LootTable.registerLoot();
        ModMisc.registerMisc();
        ModScreens.registerScreenHandlers();
    }
}
