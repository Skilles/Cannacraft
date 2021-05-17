package com.skilles.cannacraft.registry;

import com.skilles.cannacraft.Cannacraft;
import com.skilles.cannacraft.CannacraftClient;
import com.skilles.cannacraft.items.WeedBundle;
import com.skilles.cannacraft.items.WeedJoint;
import com.skilles.cannacraft.items.WeedSeed;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.util.Rarity;
import net.minecraft.util.registry.Registry;

import static com.skilles.cannacraft.Cannacraft.id;

public class ModItems {

    // Items
    public static final WeedSeed WEED_SEED = new WeedSeed(ModBlocks.WEED_CROP, new Item.Settings().group(Cannacraft.ITEM_GROUP));
    public static final WeedJoint WEED_JOINT = new WeedJoint(new Item.Settings().group(Cannacraft.ITEM_GROUP).maxDamage(5));
    public static final WeedBundle WEED_BUNDLE = new WeedBundle(new FabricItemSettings().group(Cannacraft.ITEM_GROUP).maxCount(128).rarity(Rarity.UNCOMMON));
    // Block items
    public static final BlockItem STRAIN_ANALYZER = new BlockItem(ModBlocks.STRAIN_ANALYZER, new FabricItemSettings().group(Cannacraft.ITEM_GROUP));
    public static final BlockItem SEED_CHEST = new BlockItem(ModBlocks.SEED_CHEST, new Item.Settings().group(Cannacraft.ITEM_GROUP));
    public static final BlockItem SEED_CROSSER = new BlockItem(ModBlocks.SEED_CROSSER, new Item.Settings().group(Cannacraft.ITEM_GROUP));
    public static final BlockItem WEED_EXTRACTOR = new BlockItem(ModBlocks.WEED_EXTRACTOR, new Item.Settings().group(Cannacraft.ITEM_GROUP));
    public static void registerItems () {
        Registry.register(Registry.ITEM, id( "weed_seed"), WEED_SEED);
        Registry.register(Registry.ITEM, id("weed_fruit"), WEED_BUNDLE);
        Registry.register(Registry.ITEM, id("weed_joint"), WEED_JOINT);
        Registry.register(Registry.ITEM, id( "seed_chest"), SEED_CHEST);
        Registry.register(Registry.ITEM, id( "strain_analyzer"), STRAIN_ANALYZER);
        Registry.register(Registry.ITEM, id( "seed_crosser"), SEED_CROSSER);
        Registry.register(Registry.ITEM, id("weed_extractor"), WEED_EXTRACTOR);
    }
}
