package com.skilles.cannacraft.items;

import com.skilles.cannacraft.registry.ModComponents;
import dev.onyxstudios.cca.api.v3.item.ItemComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

import java.util.HashMap;
import java.util.Map;

public class StrainComponent extends ItemComponent implements StrainInterface {
    private final ItemStack stack;
    int index = 3; // default index (unknown)
    String type;
    String strain;
    int thc = 0;
    public static int strainCount = 3; // Amount of strains
    Map<String, Integer> strainMap = new HashMap<>();

    public StrainComponent(ItemStack stack) {
        super(stack);
        // Goes through and maps each strain string to index
        for (int i = 0; strainCount - 1 > i; i++) {
            updateStrain(i);
            strainMap.put(this.strain, i); // ("OG Kush", 0)
        }
        updateStrain(index);
        this.stack = stack;
    }


    // Assigns name and type based on index
    private void updateStrain(int index) {
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
    @Override
    public void sync() {
        updateStrain(index);
        setTag();
        ModComponents.STRAIN.sync(this.stack);
    }



    // Set strain using strain name
    @Override
    public void setStrain(String strainName) {
        setIndex(strainMap.get(strainName));
        // sync called in setIndex
    }


    @Override
    public void setIndex(int index) {
        this.index = index;
        sync();
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
    public String getStrain() {
        return strain;
    }
    @Override
    public String getStrain2() {
        return getString("Strain");
    }

    public Tag getStrainTag() {
        return StringTag.of(getStrain());
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
    public void setTag() {
            putString("Strain", getStrain());
            putInt("THC", getTHC()); // TODO: add custom THC
            putString("Type", getType());
    }
    @Override
    public void readNbt() {
        this.strain = getString("Strain");
        this.type = getString("Type");
        this.thc = getInt("THC");
    }
}
