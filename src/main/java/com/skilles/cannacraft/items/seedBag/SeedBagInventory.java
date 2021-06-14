package com.skilles.cannacraft.items.seedBag;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.skilles.cannacraft.items.seedBag.SeedBag.IContents;
import com.skilles.cannacraft.strain.Strain;
import com.skilles.cannacraft.util.MiscUtil;
import com.skilles.cannacraft.util.StrainUtil;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.collection.DefaultedList;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class SeedBagInventory implements IContents {
    private static final SeedBagInventory INSTANCE = new SeedBagInventory();

    public static SeedBagInventory getInstance() {
        return INSTANCE;
    }

    private Strain strain;

    private final List<Entry> contents;
    private int count;

    private final Map<Integer, Comparator<Entry>> sorters;
    private int sorterIndex;
    private Comparator<Entry> subSorter;

    private ItemStack firstStack;
    private ItemStack lastStack;

    SeedBagInventory() {
        this.strain = StrainUtil.getStrain(0);
        this.contents = Lists.newArrayList();
        this.count = 0;
        this.sorters = Maps.newHashMap();
        this.setSorterIndex(0);
        this.firstStack = ItemStack.EMPTY;
        this.lastStack = ItemStack.EMPTY;
    }
    @Override
    public Strain getStrain() {
        return this.strain;
    }

    @Override
    public int getCount() {
        return this.count;
    }

    protected void sort() {
        if(this.contents.size() > 0) {
            this.contents.sort(this.subSorter);
            this.firstStack = this.contents.get(0).initializeStack();
            this.firstStack = this.contents.get(this.contents.size() - 1).initializeStack();
        }
    }

    @Override
    public SeedBag.ISorter getSorter() {
        return SeedBag.getSorter(this.sorterIndex);
    }

    @Override
    public int getSorterIndex() {
        return this.sorterIndex;
    }

    @Override
    public void setSorterIndex(int index) {
        this.sorterIndex = index;
        this.subSorter = this.sorters.computeIfAbsent(index, value -> (a, b) -> this.getSorter().compare(a.getStrain(), b.getStrain()));
        this.sort();
    }

    @Override
    public DefaultedList<ItemStack> getItems() {
        return DefaultedList.<ItemStack>copyOf(ItemStack.EMPTY, this.contents.stream().map(entry -> (ItemStack) entry.initializeStack()).collect(Collectors.toList()).toArray(new ItemStack[this.size()]));
    }

    @Override
    public ItemStack extractFirstSeed(int amount, boolean simulate) {
        if(this.firstStack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack out = this.firstStack.copy();
        if (amount >= this.contents.get(0).getAmount()) {
            // More seeds were requested than there actually are
            Entry entry = simulate ? this.contents.get(0) : this.contents.remove(0);
            out.setCount(entry.getAmount());
            if (!simulate) {
                this.count -= out.getCount();
                if (this.contents.size() > 0) {
                    this.firstStack = this.contents.get(0).initializeStack();
                } else {
                    this.firstStack = ItemStack.EMPTY;
                    this.lastStack = ItemStack.EMPTY;
                    this.strain = StrainUtil.getStrain(0);
                }
            }
        } else {
            out.setCount(amount);
            if (!simulate) {
                this.contents.get(0).extract(amount);
                this.count -= out.getCount();
            }
        }
        return out;
    }

    @Override
    public int size() {
        return 27;
    }

    @Override
    public ItemStack getStack(int slot) {
        if (slot == 0) {
            return this.firstStack;
        }
        if (slot == 1) {
            return this.lastStack;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack extractLastSeed(int amount, boolean simulate) {
        if(this.lastStack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack out = this.lastStack.copy();
        if (amount >= this.contents.get(this.contents.size() - 1).getAmount()) {
            // More seeds were requested than there actually are
            Entry entry = simulate ? this.contents.get(this.contents.size() - 1) : this.contents.remove(this.contents.size() - 1);
            out.setCount(entry.getAmount());
            if (!simulate) {
                this.count -= out.getCount();
                if (this.contents.size() > 0) {
                    this.lastStack = this.contents.get(this.contents.size() - 1).initializeStack();
                } else {
                    this.firstStack = ItemStack.EMPTY;
                    this.lastStack = ItemStack.EMPTY;
                    this.strain = StrainUtil.getStrain(0);
                }
            }
        } else {
            out.setCount(amount);
            if (!simulate) {
                this.contents.get(this.contents.size() - 1).extract(amount);
                this.count -= out.getCount();
            }
        }
        return out;
    }
    private static class Entry {
        private final Strain strain;
        private int amount;

        protected Entry(Strain strain, int amount) {
            this.strain = strain;
            this.amount = amount;
        }

        public int getAmount() {
            return this.amount;
        }

        public Strain getStrain() {
            return this.strain;
        }

        public ItemStack initializeStack() {
            return this.strain.toSeedStack();
        }

        public void add(int amount) {
            this.amount += amount;
        }

        public void extract(int amount) {
            this.amount -= amount;
            this.amount = Math.max(this.amount, 0);
        }

        public boolean matches(Strain strain) {
            return this.getStrain().equals(strain);
        }

        public NbtCompound writeToTag() {
            NbtCompound tag = new NbtCompound();
            NbtCompound strainTag = new NbtCompound();
            strainTag.putInt("ID", this.strain.id());
            //this.getStrain().writeToNBT(genomeTag);
            tag.put("cannacraft:strain", strainTag);
            //tag.putInt(AgriNBT.ENTRIES, this.getAmount());
            return tag;
        }

        public static Optional<Entry> readFromTag(NbtCompound tag) {
            if(!tag.contains("ID")) {
                return Optional.empty();
            }
            Strain strain = StrainUtil.getStrain(0);
            int count = tag.getInt("THC");
            return Optional.of(new Entry(strain, count));
        }
    }
}
