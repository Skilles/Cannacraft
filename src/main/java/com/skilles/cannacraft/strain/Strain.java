package com.skilles.cannacraft.strain;


import com.skilles.cannacraft.util.StrainUtil;
import net.minecraft.item.Item;

import java.util.Objects;

public final class Strain {
    public static int CLASS_COUNT = 0;
    private String name;
    private StrainMap.Type type;
    private final int id;
    private StrainUtil.StrainItems strainItem;

    public Strain(String name, StrainMap.Type type) {
        this.name = name;
        this.type = type;
        this.strainItem = StrainUtil.getStrainItems(this);
        this.id = StrainMap.strainArray.size();
        CLASS_COUNT++;
    }
    public int id() { return id; }

    public String name() {
        return name;
    }

    protected void setName(String name) { this.name = name; }

    public StrainMap.Type type() {
        return type;
    }

    protected void setType(StrainMap.Type type) {
        this.type = type;
    }

    public String toString() {
        return name + " | " + type;
    }

    public Item getItem() {
        if(strainItem == null) return null;
        return strainItem.item;
    }

    void init() {
        this.strainItem = StrainUtil.getStrainItems(this);
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