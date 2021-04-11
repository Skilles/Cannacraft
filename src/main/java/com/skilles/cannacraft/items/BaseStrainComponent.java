package com.skilles.cannacraft.items;

import dev.onyxstudios.cca.api.v3.component.ComponentV3;
import net.minecraft.nbt.CompoundTag;

// Base to allow future separate block/item components
@Deprecated
public class BaseStrainComponent implements StrainInterface, ComponentV3 {
    protected int index;
    protected String strain;
    protected String type;
    protected int thc;

    @Override
    public void setStrain(String strain) {
        this.strain = strain;
    }

    @Override
    public void setIndex(int index) {
        this.index = index;
    }

    @Override
    public void setType(String type) {
        this.type = type;
    }

    @Override
    public void setThc(int thc) {
        this.thc = thc;
    }

    @Override
    public String getStrain2() {
        return null;
    }

    @Override
    public void setTag() {

    }

    @Override
    public void sync() {

    }

    @Override
    public void readNbt() {

    }

    @Override
    public String getStrain() {
        return strain;
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public int getTHC() {
        return thc;
    }


    @Override
    public void readFromNbt(CompoundTag compoundTag) {
        this.strain = compoundTag.getString("Strain");
        this.type = compoundTag.getString("Type");
        this.thc = compoundTag.getInt("THC");
    }

    @Override
    public void writeToNbt(CompoundTag compoundTag) {
        compoundTag.putString("Strain", this.strain);
        compoundTag.putString("Type", this.type);
        compoundTag.putInt("THC", this.thc);
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StrainInterface)) return false;
        return this.strain == ((StrainInterface) o).getStrain();
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(this.index);
    }
}
