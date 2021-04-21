package com.skilles.cannacraft.items;

import com.skilles.cannacraft.strain.GeneticsManager;
import com.skilles.cannacraft.strain.StrainMap;
import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.item.CcaNbtType;
import dev.onyxstudios.cca.api.v3.item.ItemComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtList;
import org.apache.commons.lang3.text.WordUtils;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.skilles.cannacraft.strain.StrainMap.normalDist;

// separate component for ItemStacks

public class ItemStrainComponent extends ItemComponent implements StrainInterface {

    public static final int UNKNOWN_ID = 0; // null
    public ItemStrainComponent(ItemStack stack) {
        super(stack);
    }

    @Override
    public int getIndex() {
        if(!this.hasTag("ID")) this.setStrain(UNKNOWN_ID);

        return this.getInt("ID");
    }
    @Override
    public int getIndex(String name) {
        return StrainMap.indexOf(name);
    }
    @Override
    public void setGenetics(NbtList geneList) {
        putList("Genes", geneList);
    }
    @Override
    public List getGenetics() {
        if(!hasTag("Genes")) putList("Genes", GeneticsManager.toNbtList(GeneticsManager.getTestArray()));
        return getList("Genes", CcaNbtType.LIST);
    }
    @Override
    public void setStrain(int index) { // sets strain and type NBT based on index
        this.getOrCreateRootTag().putInt("ID", index);
        this.putString("Strain", StrainMap.getStrain(index).name());
        this.putString("Type", WordUtils.capitalizeFully(StrainMap.getStrain(index).type().toString()));
        this.putInt("THC", getThc());
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
        if(this.hasTag("ID") && this.getInt("ID") == UNKNOWN_ID) this.putBoolean("Identified", true);
        if(!this.hasTag("Identified")) this.putBoolean("Identified", false);
        return this.getBoolean("Identified");
    }
    @Override
    public void identify() {
        if(!this.hasTag("ID")) this.setStrain(UNKNOWN_ID);
        this.putBoolean("Identified", true);
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