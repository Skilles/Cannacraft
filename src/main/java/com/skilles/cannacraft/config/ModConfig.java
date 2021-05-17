package com.skilles.cannacraft.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = "cannacraft")
public class ModConfig implements ConfigData {

    @ConfigEntry.Gui.CollapsibleObject
    CropSection crop = new CropSection();

    @ConfigEntry.Gui.CollapsibleObject
    MiscSection misc = new MiscSection();


    @ConfigEntry.Gui.CollapsibleObject
    DebugSection debug = new DebugSection();

    @Override
    public void validatePostLoad() throws ValidationException {
        if(crop.speed > 4.0F) {
            crop.speed = 4.0F;
        } else if(crop.speed < 0) {
            crop.speed = 0;
        }
        ConfigData.super.validatePostLoad();
    }
    public CropSection getCrop() { return crop; }
    public DebugSection getDebug() { return debug; }
    public MiscSection getMisc() { return misc; }
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
}
