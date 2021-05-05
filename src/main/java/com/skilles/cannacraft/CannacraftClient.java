package com.skilles.cannacraft;

import com.skilles.cannacraft.registry.ModBlocks;
import com.skilles.cannacraft.registry.ModConfig;
import com.skilles.cannacraft.registry.ModScreens;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.render.RenderLayer;

@Environment(EnvType.CLIENT)

public class CannacraftClient implements ClientModInitializer, ModMenuApi  {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> AutoConfig.getConfigScreen(ModConfig.class, ModConfig.currentScreen).get();
    }
    @Override
    public void onInitializeClient() {
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.WEED_CROP, RenderLayer.getCutout());
        ModScreens.registerScreens();
        ModConfig.registerConfig();
        System.out.println("Screens registered!");
    }
}
