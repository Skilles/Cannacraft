package com.skilles.cannacraft.registry;

import com.skilles.cannacraft.Cannacraft;
import com.skilles.cannacraft.items.Seed;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class ModItems {

    // Items
    public static final Seed SEED = new Seed(ModBlocks.WEED_CROP, new Item.Settings().group(Cannacraft.ITEM_GROUP));
    //public static final Seed SEED1 = Registry.register(Registry.ITEM, new Identifier(Cannacraft.MOD_ID, "seed_chest"), SEED);

    // Block items
    public static final BlockItem SEED_CHEST = new BlockItem(ModBlocks.SEED_CHEST, new Item.Settings().group(Cannacraft.ITEM_GROUP));
    public static void registerItems () {
        //Registry.register(Registry.ITEM, new Identifier(Cannacraft.MOD_ID, "seed"), SEED);
        Registry.register(Registry.ITEM, Cannacraft.id( "seed"), ModItems.SEED);
        Registry.register(Registry.ITEM, new Identifier(Cannacraft.MOD_ID, "seed_chest"), SEED_CHEST);
    }
}
