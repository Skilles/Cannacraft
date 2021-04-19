package com.skilles.cannacraft.items;

import com.skilles.cannacraft.registry.ModComponents;
import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.item.ItemComponent;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

// separate component for ItemStacks

public class ItemStrainComponent extends ItemComponent implements StrainInterface {

    public static final int UNKNOWN_ID = 3; // null
    public static int STRAIN_COUNT = 3;
    private static Map<String, Integer> strainMap = new HashMap<>();
    public ItemStrainComponent(ItemStack stack) {
        super(stack);
    }

    @Override
    public int getIndex() {
        if(!this.hasTag("ID")) {
            if(!this.hasTag("Strain")) this.putInt("ID", UNKNOWN_ID); // default is unknown
            if(this.hasTag("Strain")) getIndex(this.getString("Strain")); // index 0 = null bug workaround
        }
        return this.getInt("ID");
    }
    @Override
    public void setIndex(int index) {
    this.putInt("ID", index); // BUG: index 0 is null
        this.getInt("ID");
    setStrain(index);
    }
    @Override
    public int getIndex(String strain) {// for index 0 = null bug workaround
        String[] fullStrain;
    for (int i = 0; STRAIN_COUNT - 1 > i; i++) {
            fullStrain = getFullStrain(i);
            strainMap.put(fullStrain[0], i); // ("OG Kush", 0)
            }
    return strainMap.get(strain);
    }
    public static String[] getFullStrain(int index) {
        String strain;
        String type;
        String[] fullStrain = new String[2];
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
            case UNKNOWN_ID:
                strain = "Unknown";
                type = "Unknown";
                break;
            default:
                throw new IllegalStateException("Unexpected index value: " + index);
        }
        fullStrain[0] = strain;
        fullStrain[1] = type;
        return fullStrain;
    }
    @Override
    public void setStrain(int index) { // sets strain and type NBT based on index
        String[] fullStrain = getFullStrain(index);
        this.putString("Strain", fullStrain[0]);
        this.putString("Type", fullStrain[1]);
    }
    @Override
    public String getStrain() {
        if(!this.hasTag("Strain")) setStrain(getIndex());
        return this.getString("Strain");
    }
    @Override
    public String getType() {
        if(!this.hasTag("Type")) setStrain(getIndex());
        return this.getString("Type");
    }
    @Override
    public int getThc() {
        if(!this.hasTag("THC")) this.putInt("THC", normalDist(15, 5, 13)); // lazy init
        return this.getInt("THC");
    }
    @Override
    public void setThc(int thc) {
        this.putInt("THC", thc);
    }
    @Override
    public boolean identified() {
        if(!this.hasTag("Identified")) this.putBoolean("Identified", false);
        return this.getBoolean("Identified");
    }
    @Override
    public void identify() {
        this.putBoolean("Identified", true);
    }

    public static int normalDist(int mean, int std, int min) {
        Random random = new Random();
        int newThc = (int) Math.round(random.nextGaussian()*std+mean);
        if(newThc < min) {
            newThc = min;
        }
        return newThc;
    }

    @Override
    public void copyFrom(Component other) {
        if(other != null && other.getClass() == this.getClass()) {
            other.writeToNbt(getRootTag());
        }
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return obj != null && obj.getClass() == this.getClass();
    }
}