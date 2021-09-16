package com.skilles.cannacraft.strain;

import net.minecraft.util.Rarity;

public class StrainBuilder {

    private String name;
    private StrainMap.Type type = StrainMap.Type.UNKNOWN;
    private boolean resource = false;
    private boolean register = false;
    private Rarity rarity = Rarity.COMMON;

    public StrainBuilder name(String name) {
        this.name = name;
        return this;
    }

    public StrainBuilder type(StrainMap.Type type) {
        this.type = type;
        return this;
    }

    public StrainBuilder resource() {
        this.resource = true;
        return this;
    }

    public StrainBuilder register() {
        this.register = true;
        return this;
    }

    public StrainBuilder rarity(Rarity rarity) {
        this.rarity = rarity;
        return this;
    }

    public Strain build() {
        return new Strain(name, type, rarity, resource, register);
    }

}