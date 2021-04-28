package com.skilles.cannacraft.registry;


import com.skilles.cannacraft.Cannacraft;
import com.skilles.cannacraft.blocks.machines.seedCrosser.SeedCrosserScreen;
import com.skilles.cannacraft.blocks.machines.seedCrosser.SeedCrosserScreenHandler;
import com.skilles.cannacraft.blocks.machines.strainAnalyzer.StrainAnalyzerScreen;
import com.skilles.cannacraft.blocks.machines.strainAnalyzer.StrainAnalyzerScreenHandler;
import com.skilles.cannacraft.blocks.seedChest.SeedChestScreen;
import com.skilles.cannacraft.blocks.seedChest.SeedChestScreenHandler;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.screen.ScreenHandlerType;

public class ModScreens {

    public static ScreenHandlerType<SeedChestScreenHandler> SEED_CHEST_SCREEN_HANDLER;
    public static ScreenHandlerType<StrainAnalyzerScreenHandler> STRAIN_ANALYZER_SCREEN_HANDLER;
    public static ScreenHandlerType<SeedCrosserScreenHandler> SEED_CROSSER_SCREEN_HANDLER;

    public static void registerScreenHandlers() {
        SEED_CHEST_SCREEN_HANDLER = ScreenHandlerRegistry.registerSimple(Cannacraft.id("seed_chest"), SeedChestScreenHandler::new);
        STRAIN_ANALYZER_SCREEN_HANDLER = ScreenHandlerRegistry.registerSimple(Cannacraft.id("strain_analyzer"), StrainAnalyzerScreenHandler::new);
        SEED_CROSSER_SCREEN_HANDLER = ScreenHandlerRegistry.registerSimple(Cannacraft.id("seed_crosser"), SeedCrosserScreenHandler::new);
    }

    public static void registerScreens() {
        ScreenRegistry.register(SEED_CHEST_SCREEN_HANDLER, SeedChestScreen::new);
        ScreenRegistry.register(STRAIN_ANALYZER_SCREEN_HANDLER, StrainAnalyzerScreen::new);
        ScreenRegistry.register(SEED_CROSSER_SCREEN_HANDLER, SeedCrosserScreen::new);
    }
}
