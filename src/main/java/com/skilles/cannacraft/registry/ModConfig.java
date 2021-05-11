package com.skilles.cannacraft.registry;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;

@Config(name = "cannacraft")
public class ModConfig implements ConfigData {
    public static Screen currentScreen = MinecraftClient.getInstance().currentScreen;

    boolean australia = false;
    boolean spread = false;
    boolean debug = false;

    @ConfigEntry.Gui.CollapsibleObject
    InnerStuff crop = new InnerStuff();

    static class InnerStuff {
        @ConfigEntry.Gui.Tooltip
        float speed = 1.0F;
        @ConfigEntry.Gui.Tooltip
        int yield = 1;
        @ConfigEntry.Gui.Tooltip
        boolean resource = true;
    }

    @Override
    public void validatePostLoad() throws ValidationException {
        if(crop.speed > 4.0F) {
            crop.speed = 4.0F;
        } else if(crop.speed < 0) {
            crop.speed = 0;
        }
        ConfigData.super.validatePostLoad();
    }
    public float getSpeed() {
        return crop.speed;
    }
    public boolean resourceCrops() { return crop.resource; }
    /*public static Screen configScreen() {
            ConfigBuilder builder= ConfigBuilder.create()
                    .setParentScreen(ModConfig.currentScreen)
                    .setTitle(new TranslatableText("title.cannacraft.config"));
            ConfigCategory general = builder.getOrCreateCategory(new TranslatableText("category.cannacraft.general"));
            ConfigEntryBuilder entryBuilder = builder.entryBuilder();
            general.addEntry(entryBuilder.startStrField(new TranslatableText("option.cannacraft.optionA"), "test")
                    .setDefaultValue("This is the default value") // Recommended: Used when user click "Reset"
                    .setTooltip(new TranslatableText("This option is awesome!")) // Optional: Shown when the user hover over this option
                    //.setSaveConsumer(newValue -> currentValue = newValue) // Recommended: Called when user save the config
                    .build()); // Builds the option entry for cloth config
            return builder.setFallbackCategory(general).build();
        }*/
    public static void registerConfig() {
        AutoConfig.register(ModConfig.class, GsonConfigSerializer::new);
    }
}
