package com.skilles.cannacraft;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

import java.util.HashMap;
import java.util.Map;

public class StrainType {
    int index = 0;
    String type;
    String strain;
    int thc = 0;
    public static int strainCount = 3; // Amount of strains
    Map<String, Integer> strainMap = new HashMap<>();
    public StrainType() {
        // Goes through and maps each strain string to index
        for (int i = 0; strainCount - 1 > i; i++, updateStrain(i)) {
            strainMap.put(this.strain, index); // ("OG Kush", 0)
        }
    }


    // Assigns name and type based on index
    private void updateStrain(int index){
        switch (index) {
            case 0:
                strain = "OG Kush";
                type = "Hybrid";
                break;
            case 1:
                strain = "Purple Punch";
                type = "Indica";
                break;
            case 2:
                strain = "Chem Trix";
                type = "Sativa";
                break;
            default:
                strain = "Unknown";
                type = "Unknown";
        }

    }
    // Set strain using index
    public void setStrain(int index) {
        updateStrain(index);
    }
    // Set strain using strain name
    public void setStrain(String strainName) {
        updateStrain(strainMap.get(strainName));
    }

    public String getStrain() {
        return strain;
    }
    public Tag getStrainTag() {

        return StringTag.of(getStrain());
    }
    public int getIndex() { return index; }
    public String getType() {
        return type;
    }
    public int getTHC() {
        return thc;
    }
    public String getName(int index) {
        setStrain(index);
        return getStrain();
    }
    public CompoundTag strainTag(CompoundTag tag) {
        tag.putString("Strain", getStrain());
        tag.putInt("THC", getTHC()); // TODO: add custom THC
        tag.putString("Type", getType());
        return tag;
    }
}
