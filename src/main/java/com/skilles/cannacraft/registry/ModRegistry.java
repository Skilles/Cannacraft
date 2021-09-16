package com.skilles.cannacraft.registry;

import com.skilles.cannacraft.Cannacraft;
import com.skilles.cannacraft.blocks.GrowLight;
import com.skilles.cannacraft.blocks.seedChest.SeedChest;
import com.skilles.cannacraft.blocks.weedBong.WeedBong;
import com.skilles.cannacraft.blocks.weedCrop.WeedCrop;
import com.skilles.cannacraft.blocks.weedRack.WeedRack;
import com.skilles.cannacraft.items.*;
import com.skilles.cannacraft.items.seedBag.SeedBag;
import net.minecraft.item.Item;

import java.util.Arrays;

import static com.skilles.cannacraft.registry.RegistryManager.*;

public class ModRegistry {

    public static void registerAll() {
        registerBlocks();
        registerItems();
    }

    public static void registerBlocks() {
        Item.Settings itemGroup = new Item.Settings().group(Cannacraft.ITEM_GROUP);
        Arrays.stream(ModContent.Machine.values()).forEach(v -> registerBlock(v.block, itemGroup));
        registerBlockNoItem(ModContent.WEED_CROP = setup(new WeedCrop(), "weed_crop"));
        registerBlock(ModContent.SEED_CHEST = setup(new SeedChest(), "seed_chest"), itemGroup);
        registerBlock(ModContent.RACK = setup(new WeedRack(), "weed_rack"), itemGroup);
        registerBlock(ModContent.BONG = setup(new WeedBong(), "weed_bong"), itemGroup);
        registerBlock(ModContent.GROW_LIGHT = setup(new GrowLight(), "grow_lamp"), itemGroup);
    }

    public static void registerItems() {
        // Items
        registerItem(ModContent.WEED_SEED = setup(new WeedSeed(ModContent.WEED_CROP), "weed_seed"));
        registerItem(ModContent.JOINT = setup(new WeedJoint(), "weed_joint"));
        registerItem(ModContent.WEED_BUNDLE = setup(new WeedBundle(), "weed_fruit"));
        registerItem(ModContent.SEED_BAG = setup(new SeedBag(), "seed_bag"));
        registerItem(ModContent.MANUAL = setup(new WeedManual(), "manual"));
        registerItem(ModContent.DISTILLATE = setup(new WeedDistillate(), "weed_distillate"));
        registerItem(ModContent.BROWNIE = setup(new WeedBrownie(), "weed_brownie"));
        registerItem(ModContent.LIGHTER = setup(new WeedLighter(), "lighter"));
        registerItem(ModContent.GRINDER = setup(new WeedGrinder(), "weed_grinder"));

        // Crafting Items
        registerItem(ModContent.BROWNIE_MIX = setup(new BrownieMix(), "brownie_mix"));

        // Compostable
        registerCompostable(0.5f, ModContent.WEED_SEED);
        registerCompostable(0.65F, ModContent.WEED_BUNDLE);
    }

}
