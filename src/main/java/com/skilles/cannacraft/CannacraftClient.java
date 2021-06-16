package com.skilles.cannacraft;

import com.skilles.cannacraft.config.ModConfig;
import com.skilles.cannacraft.registry.ModBlocks;
import com.skilles.cannacraft.registry.ModScreens;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.RenderLayer;

@Environment(EnvType.CLIENT)

public class CannacraftClient implements ClientModInitializer, ModMenuApi  {
    public static ModConfig config;
    public static Screen currentScreen = MinecraftClient.getInstance().currentScreen; // workaround for lambda 'private' access error (ModMenu likely needs J16 update)
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> AutoConfig.getConfigScreen(ModConfig.class, currentScreen).get();
    }
    @Override
    public void onInitializeClient() {
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.WEED_CROP, RenderLayer.getCutout());
        ModScreens.registerScreens();
        AutoConfig.register(ModConfig.class, GsonConfigSerializer::new);
        config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
    }
}
