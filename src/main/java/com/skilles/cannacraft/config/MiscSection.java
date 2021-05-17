package com.skilles.cannacraft.config;

import me.shedaniel.autoconfig.annotation.ConfigEntry;

public class MiscSection {
    @ConfigEntry.Gui.Tooltip
    public boolean smoke = false;
    @ConfigEntry.Gui.Tooltip
    public boolean australia = false;
}
