package com.skilles.cannacraft.items;

import com.skilles.cannacraft.registry.ModComponents;
import dev.onyxstudios.cca.api.v3.component.ComponentV3;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;


public abstract class BaseStrainComponent implements StrainInterface, ComponentV3 {
    protected int index;
    protected String strain;
    protected String type;
    protected int thc;
    protected boolean identified;
    private final Object provider;
    protected NbtCompound tag;

    public BaseStrainComponent(Object provider) {
        this.provider = provider;
        // Goes through and maps each strain string to index
        for (int i = 0; strainCount - 1 > i; i++) {
            updateStrain(i);
            strainMap.put(this.strain, i); // ("OG Kush", 0)
        }
        // Sets local strain and type according to index
        updateStrain(index);
        tag = ((BlockEntity)provider).toInitialChunkDataNbt();
    }


    int UNKNOWN_INDEX = 3;
    public static int strainCount = 3; // Amount of strains
    Map<String, Integer> strainMap = new HashMap<>();



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

    // Identifies item and reveals NBT tags. If index is unknown, then random index is assigned
    @Override
    public void identify() {
        if(!identified && index == UNKNOWN_INDEX) {
            Random newIndex = new Random();
            this.index = newIndex.nextInt(strainCount);
        }
        this.identified = true;
        //tag.putBoolean("Identified", true);
        setTag();
        sync();
    }
    // Checks if item is identified
    @Override
    public boolean identified() {
        return identified;
    }
    // Calls updateStrain and readNbt to sync local variables with client
    @Override
    public void sync() {
        updateStrain(index);
        ModComponents.STRAIN.sync(provider); // optional?
    }

    @Override
    public void readNbt() {
        readFromNbt(tag);
    }


    // Set strain using strain name
    @Override
    public void setStrain(String strainName) {
        setIndex(strainMap.get(strainName));
        // sync called in setIndex
    }

    // Sets index
    @Override
    public void setIndex(int index) {
        this.index = index;
        setTag();
        sync();
    }
    // Sets type
    @Override
    public void setType(String type) {
        this.type = type;
    }
    // Sets thc
    @Override
    public void setThc(int thc) {
        this.thc = thc;
    }
    // Gets local strain
    @Override
    public String getStrain() {
        return strain;
    }
    // Gets NBT tag "Strain"
    @Override
    public String getStrainNBT() {
        return tag.getString("Strain");
    }

    @Override
    public void setTag() {
        writeToNbt(tag);
    }

    // Gets local index
    @Override
    public int getIndex() {
        return index;
    }
    // Gets local type
    @Override
    public String getType() {
        return type;
    }
    // Gets local thc
    @Override
    public int getTHC() {
        return thc;
    }
    // Assign NBT tags according to local variables


    @Override
    public void readFromNbt(NbtCompound compoundTag) {
        this.strain = compoundTag.getString("Strain");
        this.type = compoundTag.getString("Type");
        this.thc = compoundTag.getInt("THC");
        this.identified = compoundTag.getBoolean("Identified");
        this.index = compoundTag.getInt("ID");
    }

    @Override
    public void writeToNbt(NbtCompound compoundTag) {
        compoundTag.putString("Strain", this.strain);
        compoundTag.putString("Type", this.type);
        compoundTag.putInt("THC", this.thc);
        compoundTag.putBoolean("Identified", this.identified);
        compoundTag.putInt("ID", this.index);
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
