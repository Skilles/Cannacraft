package com.skilles.cannacraft.components;

import com.skilles.cannacraft.registry.ModItems;
import com.skilles.cannacraft.strain.Gene;
import com.skilles.cannacraft.strain.GeneTypes;
import com.skilles.cannacraft.strain.StrainMap;
import com.skilles.cannacraft.util.MiscUtil;
import com.skilles.cannacraft.util.StrainUtil;
import dev.onyxstudios.cca.api.v3.component.CopyableComponent;
import dev.onyxstudios.cca.api.v3.item.CcaNbtType;
import dev.onyxstudios.cca.api.v3.item.ItemComponent;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.skilles.cannacraft.components.ItemStrainComponent.StrainType.FRUIT;
import static com.skilles.cannacraft.components.ItemStrainComponent.StrainType.SEED;
import static com.skilles.cannacraft.util.StrainUtil.normalDist;


public class ItemStrainComponent extends ItemComponent implements StrainInterface, CopyableComponent<ItemStrainComponent> {

    public static final int UNKNOWN_ID = 0; // null
    final ItemStack stack;
    enum StrainType {
        SEED,
        FRUIT
    }
    StrainType type;
    public ItemStrainComponent(ItemStack stack) {
        super(stack);
        this.stack = stack;
        if(stack.isOf(ModItems.WEED_SEED)) {
            this.type = SEED;
        } else if(stack.isOf(ModItems.WEED_BUNDLE)) {
            this.type = FRUIT;
        }
    }
    private boolean seed() {
        return this.type.equals(SEED);
    }
    private boolean fruit() {
        return this.type.equals(FRUIT);
    }
    @Override
    public int getIndex() {
        if(!this.hasTag("ID")) this.setStrain(UNKNOWN_ID);
        return this.getInt("ID");
    }
    @Override
    public void setGenetics(NbtList geneList) {
        putList("Attributes", geneList);
    }
    @Override
    public List<NbtCompound> getGenetics() {
        if(!hasTag("Attributes")) return null; //putList("Attributes", GeneticsManager.toNbtList(GeneticsManager.getTestArray()));
        return getList("Attributes", CcaNbtType.COMPOUND);
    }
    private NbtList getGeneticsNbt() {
        if(!hasTag("Attributes")) return null;
        return getList("Attributes", NbtType.COMPOUND);
    }
    @Override
    public boolean hasGenes() {
        return getGenetics() != null;
    }
    @Override
    public void addGene(String name, int level) {
        ArrayList<Gene> currentList = new ArrayList<>();
        if(hasGenes()){
            currentList = MiscUtil.fromNbtList(getGenetics());
            for(int i = 0; i < currentList.size(); i++) {
                Gene entry = currentList.get(i);
                if(entry.name().equalsIgnoreCase(name)) {
                    currentList.remove(entry);
                }
            }
        }
        currentList.add(new Gene(GeneTypes.byName(name), level));
        setGenetics(MiscUtil.toNbtList(currentList));
    }
    @Override
    public void setStrain(int index) { // sets strain and type NBT based on index
        this.getOrCreateRootTag().putInt("ID", index);
        this.putInt("THC", getThc());
        if(seed()) this.putBoolean("Male", isMale());
    }
    @Override
    public String getStrain() {
        if(!this.hasTag("ID")) setStrain(getIndex());
        return StrainUtil.getStrain(this.getInt("ID")).name();
    }
    @Override
    public StrainMap.Type getType() {
        if(!this.hasTag("Type")) setStrain(getIndex());
        return StrainUtil.getStrain(this.getInt("ID")).type();
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
        if(seed()) {
            if (!this.hasTag("Male")) this.putBoolean("Male", false);
            return this.getBoolean("Male");
        } else {
            return false;
        }
    }
    @Override
    public void setMale(boolean isMale) {
        if(seed()) {
            this.getOrCreateRootTag().putBoolean("Male", isMale);
        }
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