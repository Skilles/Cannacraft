package com.skilles.cannacraft.registry;


import com.skilles.cannacraft.blocks.machines.seedCrosser.SeedCrosserScreen;
import com.skilles.cannacraft.blocks.machines.seedCrosser.SeedCrosserScreenHandler;
import com.skilles.cannacraft.blocks.machines.strainAnalyzer.StrainAnalyzerScreen;
import com.skilles.cannacraft.blocks.machines.strainAnalyzer.StrainAnalyzerScreenHandler;
import com.skilles.cannacraft.blocks.machines.weedExtractor.WeedExtractorScreen;
import com.skilles.cannacraft.blocks.machines.weedExtractor.WeedExtractorScreenHandler;
import com.skilles.cannacraft.blocks.seedChest.SeedChestScreen;
import com.skilles.cannacraft.blocks.seedChest.SeedChestScreenHandler;
import net.minecraft.screen.ScreenHandlerType;

import static com.skilles.cannacraft.Cannacraft.id;
import static net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry.register;
import static net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry.registerSimple;

public class ModScreens {

    public static ScreenHandlerType<SeedChestScreenHandler> SEED_CHEST_SCREEN_HANDLER;
    public static ScreenHandlerType<StrainAnalyzerScreenHandler> STRAIN_ANALYZER_SCREEN_HANDLER;
    public static ScreenHandlerType<SeedCrosserScreenHandler> SEED_CROSSER_SCREEN_HANDLER;
    public static ScreenHandlerType<WeedExtractorScreenHandler> WEED_EXTRACTOR_SCREEN_HANDLER;

    public static void registerScreenHandlers() {
        SEED_CHEST_SCREEN_HANDLER = registerSimple(id("seed_chest"), SeedChestScreenHandler::new);
        STRAIN_ANALYZER_SCREEN_HANDLER = registerSimple(id("strain_analyzer"), StrainAnalyzerScreenHandler::new);
        SEED_CROSSER_SCREEN_HANDLER = registerSimple(id("seed_crosser"), SeedCrosserScreenHandler::new);
        WEED_EXTRACTOR_SCREEN_HANDLER = registerSimple(id("weed_extractor"), WeedExtractorScreenHandler::new);
    }
    public static void registerScreens() {
       register(SEED_CHEST_SCREEN_HANDLER, SeedChestScreen::new);
       register(STRAIN_ANALYZER_SCREEN_HANDLER, StrainAnalyzerScreen::new);
       register(SEED_CROSSER_SCREEN_HANDLER, SeedCrosserScreen::new);
       register(WEED_EXTRACTOR_SCREEN_HANDLER, WeedExtractorScreen::new);
    }
}
