package com.skilles.cannacraft.items;

import dev.onyxstudios.cca.api.v3.item.ItemComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

// local variables are dependant on if server or client are setting them
// NBT tags are two-way

public class ItemStrainComponent extends ItemComponent implements StrainInterface {
    private final ItemStack stack;
    private NbtCompound tag;
    private boolean identified = false;
    int UNKNOWN_INDEX = 3;
    int index = UNKNOWN_INDEX; // default index (unknown)

    String type;
    String strain;
    int thc = 0;
    public static int strainCount = 3; // Amount of strains
    Map<String, Integer> strainMap = new HashMap<>();

    public ItemStrainComponent(ItemStack stack) {
        super(stack);
        // Goes through and maps each strain string to index
        for (int i = 0; strainCount - 1 > i; i++) {
            updateStrain(i);
            strainMap.put(this.strain, i); // ("OG Kush", 0)
        }
        // Sets local strain and type according to index
        updateStrain(index);

        // Inits stack and tag
        this.stack = stack;
        this.tag = stack.getOrCreateTag();

        // setTag() is not called here so that blank items do not have an "ID" tag
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

    // Identifies item and reveals NBT tags. If index is unknown, then random index is assigned
    @Override
    public void identify() {
        if(!identified && index == UNKNOWN_INDEX) {
            Random newIndex = new Random();
            this.index = newIndex.nextInt(strainCount);
        }
        this.identified = true;
        tag.putBoolean("Identified", true);
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
        readNbt();

        //ModComponents.STRAIN.sync(this.stack); // optional?
    }


    // Set strain using strain name
    @Override
    public void setStrain(String strainName) {
        setIndex(strainMap.get(strainName));
        // sync called in setIndex
    }

    // Sets index of itemstack
    @Override
    public void setIndex(int index) {
        this.index = index;
        setTag();
        sync();
    }
    // Sets type of itemstack
    @Override
    public void setType(String type) {
        this.type = type;
    }
    // Sets thc of itemstack
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
    public void setTag() {
        tag.putInt("ID", index); // Always sets tag "ID" to index
            if (!identified) {
                tag.putString("Strain", "Unidentified");
                tag.putInt("THC", 2);
                tag.putString("Type", "Unknown");
                tag.putBoolean("Identified", false);
            } else {
                tag.putString("Strain", getStrain());
                tag.putInt("THC", getTHC()); // TODO: add custom THC
                tag.putString("Type", getType());
                tag.putBoolean("Identified", true);
        }
    }
    // Sets local variables according to NBT tags. Strain, type, and thc are assigned only when identified
    @Override
    public void readNbt() {
    NbtCompound tag = stack.getOrCreateTag();
        this.identified = tag.getBoolean("Identified");
        this.index = tag.getInt("ID");
        if(identified) {
            this.strain = tag.getString("Strain");
            this.type = tag.getString("Type");
            this.thc = tag.getInt("THC");
        }
    }

    // Checks what is synced, returns unsycned items and their values
    @Override
    public String syncTest() {
        boolean strainSync = false;
        boolean typeSync = false;
        boolean identifiedSync = false;
        boolean indexSync = false;
        boolean tagAssigned;

        String strain = tag.getString("Strain");
        String type = tag.getString("Type");
        boolean identified = tag.getBoolean("Identified");
        int index = tag.getInt("ID");
        String syncedItems;
        String tags = "";
        String data = "";

        if(this.strain.equals(strain)) {
            strainSync = true;
        } else {
            tags += strain+", ";
            data += this.strain+", ";
        }

        if(this.type.equals(type)) {
            typeSync = true;
        } else {
            tags += type+", ";
            data += this.type+", ";
        }

        if(this.identified == identified) {
            identifiedSync = true;
        } else {
            tags += identified+", ";
            data += this.identified+", ";
        }

        if(tag.contains("ID")){
            if(this.index == index) {
                indexSync= true;
            } else {
                tags += index;
                data += this.index;
            }
            tagAssigned = true;
        } else {
            tagAssigned = false;
        }


        if(tagAssigned) {
            syncedItems = "Strain: " + strainSync + " | Type: " + typeSync + " | Identified: " + identifiedSync + " | Index: " + indexSync;
        } else {
            syncedItems = "Item has not been assigned tags!";
            tags = "";
            data = "";
        }

        return syncedItems+"\n"+tags+"\n"+data;
    }
}
