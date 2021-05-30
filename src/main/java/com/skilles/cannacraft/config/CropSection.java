package com.skilles.cannacraft.config;

import me.shedaniel.autoconfig.annotation.ConfigEntry;

public class CropSection {
    @ConfigEntry.Gui.Tooltip
    public float speed = 1.0F;
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(max = 3)
    public int yield = 0;
    @ConfigEntry.Gui.Tooltip
    public boolean resource = true;
    @ConfigEntry.Gui.Tooltip
    public boolean spread = true;
    @ConfigEntry.BoundedDiscrete(max = 100)
    public int spreadChance = 75;
    @ConfigEntry.Gui.Tooltip(count = 2)
    public boolean randomBreed;
}
