package com.skilles.cannacraft.registry;

import com.skilles.cannacraft.Cannacraft;
import com.skilles.cannacraft.items.WeedFruit;
import com.skilles.cannacraft.items.WeedSeed;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.BlockItem;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.util.registry.Registry;

import static com.skilles.cannacraft.Cannacraft.id;

public class ModItems {

    // Items
    public static final WeedSeed WEED_SEED = new WeedSeed(ModBlocks.WEED_CROP, new Item.Settings().group(Cannacraft.ITEM_GROUP));
    public static final WeedFruit WEED_FRUIT = new WeedFruit(new FabricItemSettings().group(Cannacraft.ITEM_GROUP).food(new FoodComponent.Builder().alwaysEdible().snack().build()));
    // Block items
    public static final BlockItem STRAIN_ANALYZER = new BlockItem(ModBlocks.STRAIN_ANALYZER, new FabricItemSettings().group(Cannacraft.ITEM_GROUP));
    public static final BlockItem SEED_CHEST = new BlockItem(ModBlocks.SEED_CHEST, new Item.Settings().group(Cannacraft.ITEM_GROUP));
    public static final BlockItem SEED_CROSSER = new BlockItem(ModBlocks.SEED_CROSSER, new Item.Settings().group(Cannacraft.ITEM_GROUP));

    public static void registerItems() {
        Registry.register(Registry.ITEM, id("weed_seed"), WEED_SEED);
        Registry.register(Registry.ITEM, id("weed_fruit"), WEED_FRUIT);
        Registry.register(Registry.ITEM, id("seed_chest"), SEED_CHEST);
        Registry.register(Registry.ITEM, id("strain_analyzer"), STRAIN_ANALYZER);
        Registry.register(Registry.ITEM, id("seed_crosser"), SEED_CROSSER);
    }
}
