package com.skilles.cannacraft;

import com.skilles.cannacraft.registry.ModScreens;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)

public class CannacraftClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ModScreens.registerScreens();
        System.out.println("Screens registered!");
    }
}
