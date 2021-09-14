package com.skilles.cannacraft.util;

import com.skilles.cannacraft.config.ModConfig;
import com.skilles.cannacraft.registry.ModItems;
import com.skilles.cannacraft.strain.Gene;
import com.skilles.cannacraft.strain.GeneTypes;
import com.skilles.cannacraft.strain.Strain;
import com.skilles.cannacraft.strain.StrainInfo;
import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WeedRegistry {

    // TODO:
    //  store all strain containing items in here and add common methods for retrieving strain info
    //  store all strains in player's registry to allow for progression
    //  start refactoring to remove unnecessary code

    private static ModConfig config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();



    // Anything that can have strain information
    public enum WeedTypes {
        BUNDLE(ModItems.WEED_BUNDLE),
        DISTILLATE(ModItems.WEED_DISTILLATE),
        BROWNIE(ModItems.WEED_BROWNIE),
        BROWNIE_MIX(ModItems.BROWNIE_MIX),
        JOINT(ModItems.WEED_JOINT),
        SEED(ModItems.WEED_SEED);

        private final Item weedItem;

        WeedTypes(Item item) {
            weedItem = item;
        }

        Item item() {
            return weedItem;
        }
        public static WeedTypes fromString(String string) {
            return Arrays.stream(WeedTypes.values()).filter(type -> string.equalsIgnoreCase(type.name())).findFirst().orElse(SEED);
        }
        public static WeedTypes fromStack(ItemStack stack) {
            return Arrays.stream(WeedTypes.values()).filter(type -> stack.getItem().equals(type.item())).findFirst().get();
        }
        public static boolean isOf(ItemStack stack) {
            return Arrays.stream(WeedTypes.values()).anyMatch(type -> stack.getItem().equals(type.item()));
        }
    }
    public enum StatusTypes {
        WET(1.0F),
        DRY(0.5F),
        GROUND(0.0F);

        private final float value;

        StatusTypes(float i) {
            value = i;
        }

        public float value() {
            return value;
        }
        public static StatusTypes byValue(float value) {
            return Arrays.stream(StatusTypes.values()).filter(type -> type.value == value).findFirst().get();
        }
    }
    // Check item
    public static boolean checkItem(Item item) {
        return Arrays.stream(WeedTypes.values()).anyMatch(x -> x.item().equals(item));
    }
    public static boolean checkItem(ItemStack itemStack) {
        return checkItem(itemStack.getItem());
    }

    // Get strain from item
    public static Strain getStrain(ItemStack itemStack) {
        NbtCompound strainTag = getStrainTag(itemStack);
        return StrainUtil.getStrain(strainTag.getInt("ID"), strainTag.getBoolean("Resource"));
    }

    // Get thc from item
    public static int getThc(ItemStack itemStack) {
        return getStrainTag(itemStack).getInt("THC");
    }

    // Check identified from item
    public static boolean isIdentified(ItemStack itemStack) { return getStrainTag(itemStack).getBoolean("Identified"); }

    // Get subtag from item
    private static NbtCompound getStrainTag(ItemStack itemStack) {
        assert checkItem(itemStack);
        return itemStack.getNbt().getCompound("cannacraft:strain");
    }

    // Get genes from item
    public static List<Gene> getGenes(ItemStack itemStack) {
        List<Gene> output = new ArrayList<>();
        NbtList nbtList = getStrainTag(itemStack).getList("Attributes", NbtType.COMPOUND);
        nbtList.stream().map(nbtElement -> (NbtCompound) nbtElement).forEachOrdered(compound -> {
            String name = compound.getString("Gene");
            int level = compound.getInt("Level");
            output.add(new Gene(GeneTypes.byName(name), level));
        });
        return output;
    }

    // Get item's result after processing
    public static ItemStack getOutput(ItemStack itemStack) {
        Strain stackStrain = getStrain(itemStack);
        return stackStrain.getItem().getDefaultStack();
    }

    public static StrainInfo getStrainInfo(ItemStack itemStack) {
        return new StrainInfo(getStrain(itemStack), getThc(itemStack), isIdentified(itemStack), getGenes(itemStack));
    }

    // Convert strain to item
    public static ItemStack strainToItem(@Nullable Strain strain, @Nullable StatusTypes status, @Nullable Integer thc, @Nullable NbtList genes, WeedTypes type, boolean identified) {
        if(strain == null) strain = randomStrain();

        ItemStack itemStack = type.item().getDefaultStack();
        NbtCompound strainTag = new NbtCompound();
        strainTag.putInt("ID", strain.id());
        strainTag.putInt("THC", thc != null ? thc : randomThc(strain));
        if(status != null) strainTag.putFloat("Status", status.value());
        strainTag.put("Attributes", genes != null ? genes : MiscUtil.toNbtList(MiscUtil.randGenes(strain)));
        strainTag.putBoolean("Identified", identified);
        itemStack.setSubNbt("cannacraft:strain", strainTag);
        return itemStack;
    }

    // Get status if bundle
    public static StatusTypes getStatus(ItemStack itemStack) {
        assert itemStack.getItem().equals(WeedTypes.BUNDLE.item());
        return StatusTypes.byValue(getStrainTag(itemStack).getFloat("Status"));
    }

    private static int randomThc(Strain strain) {
        return (int) (StrainUtil.normalDist(15, 5, 10) * strain.thcMultiplier());
    }

    public static Strain randomStrain() {
        List<Strain> strainList = StrainUtil.getStrainPool();
        // Compute the total weight of all items together.
        double totalWeight = 0.0;
        for (Strain strain : strainList) {
            totalWeight += MiscUtil.getWeight(strain);
        }
        // Now choose a random item.
        int idx = 0;
        for (double r = Math.random() * totalWeight; idx < strainList.size() - 1; ++idx) {
            r -= MiscUtil.getWeight(strainList.get(idx));
            if (r <= 0.0) break;
        }
        return strainList.get(idx);
    }

    public static ItemStack randomItem(WeedTypes type, boolean genes, boolean identified) {
        ItemStack itemStack = type.item().getDefaultStack();
        Strain strain = randomStrain();
        NbtCompound strainTag = new NbtCompound();
        strainTag.putInt("ID", strain.id());
        strainTag.putInt("THC", randomThc(strain));
        if(type.equals(WeedTypes.BUNDLE)) strainTag.putFloat("Status", 1.0F);
        strainTag.put("Attributes", genes ? MiscUtil.toNbtList(MiscUtil.randGenes(strain)) : new NbtList());
        strainTag.putBoolean("Identified", identified);
        itemStack.setSubNbt("cannacraft:strain", strainTag);
        return itemStack;
    }
}
