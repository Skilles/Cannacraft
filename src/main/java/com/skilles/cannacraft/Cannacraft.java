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
            () -> new ItemStack(ModContent.JOINT));

    public static Identifier id(String path) {
        return new Identifier(MOD_ID, path);
    }

    private static final Logger LOGGER = LogManager.getLogger(Cannacraft.class);

    public static void log(Level level, String message) {
        LOGGER.log(level, "[Cannacraft] " + message);
    }

    public static void log(String message) {
        LOGGER.info(message);
    }

    public static void log(Object message) {
        log(String.valueOf(message));
    }

    @Override
    public void onInitialize() {
        ModRegistry.registerAll();

        StrainMap.registerStrains();
        ModCommands.registerCommands();
        LootTable.registerLoot();
        ModMisc.registerMisc();
        ModScreens.registerScreenHandlers();
    }
}
