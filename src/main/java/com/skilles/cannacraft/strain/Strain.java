package com.skilles.cannacraft.strain;


import com.skilles.cannacraft.registry.ModItems;
import com.skilles.cannacraft.util.StrainUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Rarity;
import org.jetbrains.annotations.ApiStatus;

import java.util.Objects;

import static com.skilles.cannacraft.strain.StrainMap.Type;

public class Strain {
    public static int CLASS_COUNT = 0;
    private String name;
    private Type type;
    private int id;
    private float thcMultiplier;
    private Rarity rarity;
    public StrainUtil.StrainItems strainItem;
    private final boolean resource;

    public Strain(String name, Type type, boolean resource, boolean register) {
        this.name = name;
        this.type = type;
        this.strainItem = StrainUtil.getStrainItem(this);
        this.rarity = Rarity.COMMON;
        this.thcMultiplier = 1.0F;
        this.resource = resource;
        this.id = StrainUtil.indexOf(this, register);
        CLASS_COUNT++;
    }
    public Strain(String name, Type type, Rarity rarity, boolean resource, boolean register) {
        this(name, type, resource, register);
        this.rarity = rarity;
        this.thcMultiplier = StrainUtil.getThcMultiplier(this);
    }
    public Strain(String name, Type type, boolean register) {
        this(name, type, false, register);
    }
    public Strain(String name, Type type , Rarity rarity, boolean register) {
        this(name, type, false, register);
        this.rarity = rarity;
        this.thcMultiplier = StrainUtil.getThcMultiplier(this);
    }
    @ApiStatus.Experimental
    public Strain withId(int id) {
        this.id = id;
        return this;
    }
    public int id() { return id; }

    public String name() {
        return name;
    }

    protected void setName(String name) { this.name = name; }

    public Type type() {
        return type;
    }

    public float thcMultiplier() { return thcMultiplier; }

    public Rarity getRarity() { return rarity; }

    protected void setType(Type type) {
        this.type = type;
    }

    public String toString() { return name + " | " + type + " | " + rarity; }

    public Item getItem() { return strainItem.item; }

    public boolean isResource() { return this.resource; }

    public ItemStack toSeedStack() {
        NbtCompound tag = new NbtCompound();
        tag.putInt("ID", id);
        ItemStack stack = ModItems.WEED_SEED.getDefaultStack();
        stack.setSubNbt("cannacraft:strain", tag);
        return stack;
    }

    public void init() {
        this.strainItem = StrainUtil.getStrainItem(this);
        this.rarity = Rarity.COMMON;
        this.thcMultiplier = StrainUtil.getThcMultiplier(this);
        StrainUtil.addStrain(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null /*|| getClass() != o.getClass()*/) return false;
        Strain strain = (Strain) o;
        return this.isResource() ? this.strainItem.equals(strain.strainItem) : name.equals(strain.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}