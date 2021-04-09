package com.skilles.cannacraft;

import net.minecraft.nbt.CompoundTag;

import java.util.HashMap;
import java.util.Map;

public class StrainType {
    int index = 0;
    String type;
    String strain;
    static int strainCount = 3; // Amount of strains
    Map<String, Integer> strainMap = new HashMap<>();
    public StrainType() {
        // Goes through and maps each strain string to index
        for (int i = 0; strainCount - 1 > i; i++) {
            updateStrain(i);
            strainMap.put(this.strain, index); // ("OG Kush", 0)
        }
    }
    // Assigns name and type based on index
    private void updateStrain(int index){
        switch (index) {
            case 0:
                strain = "OG Kush";
                type = "Hybrid";
            case 1:
                strain = "Purple Punch";
                type = "Indica";
            case 2:
                strain = "Chem Trix";
                type = "Sativa";
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
    public String getType() {
        return type;
    }
    public CompoundTag strainTag(CompoundTag tag) {
        tag.putString("Strain", getStrain());
        tag.putInt("THC", 0); // TODO: add custom THC
        tag.putString("Type", getType());
        return tag;
    }
}
