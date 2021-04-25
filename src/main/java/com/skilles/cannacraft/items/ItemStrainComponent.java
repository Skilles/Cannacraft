package com.skilles.cannacraft.items;

import com.skilles.cannacraft.registry.ModItems;
import com.skilles.cannacraft.strain.GeneticsManager;
import com.skilles.cannacraft.strain.StrainMap;
import dev.onyxstudios.cca.api.v3.component.CopyableComponent;
import dev.onyxstudios.cca.api.v3.item.CcaNbtType;
import dev.onyxstudios.cca.api.v3.item.ItemComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtList;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.skilles.cannacraft.strain.StrainMap.normalDist;

// separate component for ItemStacks

public class ItemStrainComponent extends ItemComponent implements StrainInterface, CopyableComponent<ItemStrainComponent> {

    public static final int UNKNOWN_ID = 0; // null
    ItemStack stack;
    public ItemStrainComponent(ItemStack stack) {
        super(stack);
        this.stack = stack;
    }

    @Override
    public int getIndex() {
        if(!this.hasTag("ID")) this.setStrain(UNKNOWN_ID);

        return this.getInt("ID");
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
        //this.putString("Strain", StrainMap.getStrain(index).name());
        //this.putString("Type", WordUtils.capitalizeFully(StrainMap.getStrain(index).type().toString()));
        this.putInt("THC", getThc());
        if(stack.getItem().equals(ModItems.WEED_SEED)) this.putBoolean("Male", isMale());
    }
    @Override
    public String getStrain() {
        if(!this.hasTag("ID")) setStrain(getIndex());
        return StrainMap.getStrain(this.getInt("ID")).name();
    }
    @Override
    public StrainMap.Type getType() {
        if(!this.hasTag("Type")) setStrain(getIndex());
        return StrainMap.getStrain(this.getInt("ID")).type();
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
    public boolean isMale() {
        if(!this.hasTag("Male")) this.putBoolean("Male", false);
        return this.getBoolean("Male");
    }
    @Override
    public void setMale(boolean isMale) {
        this.getOrCreateRootTag().putBoolean("Male", isMale);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return obj != null && obj.getClass() == this.getClass();
    }

    @Override
    public void copyFrom(ItemStrainComponent other) {
        stack.setTag(other.stack.getTag());
        //setStrain(other.getIndex());
    }
}