package com.skilles.cannacraft.strain;


import com.skilles.cannacraft.registry.ModItems;
import com.skilles.cannacraft.util.StrainUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Rarity;

import java.util.Objects;

public final class Strain {
    public static int CLASS_COUNT = 0;
    private String name;
    private StrainMap.Type type;
    private final int id;
    private float thcMultiplier;
    private Rarity rarity = Rarity.COMMON;
    private StrainUtil.StrainItems strainItem;

    public Strain(String name, StrainMap.Type type) {
        this.name = name;
        this.type = type;
        this.strainItem = StrainUtil.getStrainItem(this);
        this.id = StrainMap.strainArray.size();
        CLASS_COUNT++;
    }
    public Strain(String name, StrainMap.Type type, Rarity rarity) {
        this(name, type);
        this.rarity = rarity;
        this.thcMultiplier = StrainUtil.getThcMultiplier(this);
    }
    public int id() { return id; }

    public String name() {
        return name;
    }

    protected void setName(String name) { this.name = name; }

    public StrainMap.Type type() {
        return type;
    }

    public float thcMultiplier() { return thcMultiplier; }

    public Rarity getRarity() { return rarity; }

    protected void setType(StrainMap.Type type) {
        this.type = type;
    }

    public String toString() { return name + " | " + type + " | " + rarity; }

    public Item getItem() { return strainItem.item; }

    public ItemStack toSeedStack() {
        NbtCompound tag = new NbtCompound();
        tag.putInt("ID", id);
        ItemStack stack = ModItems.WEED_SEED.getDefaultStack();
        stack.putSubTag("cannacraft:strain", tag);
        return stack;
    }

    public void init() {
        this.strainItem = StrainUtil.getStrainItem(this);
        this.rarity = Rarity.COMMON;
        this.thcMultiplier = StrainUtil.getThcMultiplier(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Strain strain = (Strain) o;
        return name.equals(strain.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}