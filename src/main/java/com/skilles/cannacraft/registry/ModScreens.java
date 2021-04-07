package com.skilles.cannacraft.registry;


import com.skilles.cannacraft.Cannacraft;
import com.skilles.cannacraft.blocks.seedChest.SeedChestScreen;
import com.skilles.cannacraft.blocks.seedChest.SeedChestScreenHandler;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;

public class ModScreens {

    public static ScreenHandlerType<SeedChestScreenHandler> SEED_CHEST_SCREEN_HANDLER;

    public static void registerScreenHandlers() {
        SEED_CHEST_SCREEN_HANDLER = ScreenHandlerRegistry.registerSimple(new Identifier(Cannacraft.MOD_ID, "seed_chest"), SeedChestScreenHandler::new);

    }
    public static void registerScreens() {
       ScreenRegistry.register(SEED_CHEST_SCREEN_HANDLER, SeedChestScreen::new);
    }
}
