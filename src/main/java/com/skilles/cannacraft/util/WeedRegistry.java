package com.skilles.cannacraft.util;

import com.skilles.cannacraft.blocks.weedCrop.WeedCropEntity;
import com.skilles.cannacraft.config.ModConfig;
import com.skilles.cannacraft.dna.genome.Genome;
import com.skilles.cannacraft.dna.genome.gene.TraitGene;
import com.skilles.cannacraft.registry.ModContent;
import com.skilles.cannacraft.strain.Strain;
import com.skilles.cannacraft.strain.StrainInfo;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WeedRegistry {

    private static ModConfig config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();

    // Anything that can have strain information
    public enum WeedTypes {
        BUNDLE(ModContent.WEED_BUNDLE),
        DISTILLATE(ModContent.DISTILLATE),
        BROWNIE(ModContent.BROWNIE),
        BROWNIE_MIX(ModContent.BROWNIE_MIX),
        JOINT(ModContent.JOINT),
        SEED(ModContent.WEED_SEED);

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
    @Deprecated
    public static Strain getStrain(ItemStack itemStack) {
        NbtCompound strainTag = getStrainTag(itemStack);
        return StrainUtil.getStrain(strainTag.getInt("ID"), strainTag.getBoolean("Resource"));
    }

    // Get thc from item
    @Deprecated
    public static int getThc(ItemStack itemStack) {
        return getStrainTag(itemStack).getInt("THC");
    }

    // Check identified from item
    public static boolean isIdentified(ItemStack itemStack) { return getStrainTag(itemStack).getBoolean("Identified"); }

    public static boolean isIdentified(WeedCropEntity entity) { return getStrainTag(entity).getBoolean("Identified"); }

    @Deprecated
    public static boolean isMale(ItemStack itemStack) { return getStrainTag(itemStack).getBoolean("Male"); }

    // Get subtag from item
    public static NbtCompound getStrainTag(ItemStack itemStack) {
        assert checkItem(itemStack);
        return itemStack.getNbt().getCompound("cannacraft:strain");
    }
    public static NbtCompound getStrainTag(WeedCropEntity entity) {
        return entity.writeNbt(new NbtCompound()).getCompound("cannacraft:strain");
    }

    // Get genes from item
    public static List<TraitGene> getGenes(ItemStack itemStack) {
        return new ArrayList<>(DnaUtil.getGenome(itemStack).traitMap.values());
    }

    // Get item's result after processing
    public static ItemStack getOutput(ItemStack itemStack) {
        Strain stackStrain = getStrain(itemStack);
        return stackStrain.getItem().getDefaultStack();
    }

    public static StrainInfo getStrainInfo(ItemStack itemStack) {
        Genome genome = DnaUtil.getGenome(itemStack);
        return DnaUtil.convertStrain(genome, isIdentified(itemStack));
    }

    public static StrainInfo getStrainInfo(WeedCropEntity entity) {
        Genome genome = DnaUtil.getGenome(entity);
        return DnaUtil.convertStrain(genome, isIdentified(entity));
    }

    // Convert strain to item
    public static ItemStack strainToItem(StrainInfo info, WeedTypes type, StatusTypes status) {
        return strainToItem(info.strain(), info.thc(), info.identified(), info.geneList(), type, status);
    }

    // Convert strain to item
    public static ItemStack strainToItem(@Nullable Strain strain, @Nullable Integer thc, boolean identified, @Nullable List<TraitGene> genes, WeedTypes type, @Nullable StatusTypes status) {
        return strainToItem(strain, thc, identified, genes, type, status, false);
    }

    // Convert strain to item
    public static ItemStack strainToItem(@Nullable Strain strain, @Nullable Integer thc, boolean identified, @Nullable List<TraitGene> genes, WeedTypes type, @Nullable StatusTypes status, boolean male) {
        if (strain == null) strain = randomStrain();
        if (thc == null) thc = randomThc(strain);

        Genome genome = DnaUtil.convertGenome(strain.id(), thc, strain.isResource(), male, genes);

        ItemStack itemStack = type.item().getDefaultStack();
        itemStack.setSubNbt("cannacraft:strain", DnaUtil.generateNbt(genome, identified, status));

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

    public static StrainInfo randomStrainInfo(boolean identified, boolean male) {
        Strain randStrain = randomStrain();
        return new StrainInfo(randStrain, randomThc(randStrain), identified, male, new ArrayList<>());
    }
}
