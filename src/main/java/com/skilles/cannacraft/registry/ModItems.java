package com.skilles.cannacraft.registry;

import com.skilles.cannacraft.Cannacraft;
import com.skilles.cannacraft.items.*;
import com.skilles.cannacraft.items.seedBag.SeedBag;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.BlockItem;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.util.Rarity;
import net.minecraft.util.registry.Registry;

import static com.skilles.cannacraft.Cannacraft.id;

public class ModItems {

    // Items
    public static final WeedSeed WEED_SEED = new WeedSeed(ModBlocks.WEED_CROP, new Item.Settings().group(Cannacraft.ITEM_GROUP));
    public static final WeedJoint WEED_JOINT = new WeedJoint(new Item.Settings().group(Cannacraft.ITEM_GROUP).maxDamage(5));
    public static final WeedBundle WEED_BUNDLE = new WeedBundle(new FabricItemSettings().group(Cannacraft.ITEM_GROUP).maxCount(128).rarity(Rarity.UNCOMMON));
    public static final SeedBag SEED_BAG = new SeedBag(new Item.Settings().group(Cannacraft.ITEM_GROUP).maxCount(1).fireproof());
    public static final WeedManual WEED_MANUAL = new WeedManual(new Item.Settings().group(Cannacraft.ITEM_GROUP).maxCount(1).fireproof());
    public static final WeedDistillate WEED_DISTILLATE = new WeedDistillate(new Item.Settings().group(Cannacraft.ITEM_GROUP));
    public static final WeedBrownie WEED_BROWNIE = new WeedBrownie(new Item.Settings().group(Cannacraft.ITEM_GROUP).food(new FoodComponent.Builder().hunger(4).alwaysEdible().saturationModifier(2.0F).statusEffect(new StatusEffectInstance(ModMisc.HIGH, 1000, 2), 1.0F).statusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 200, 1), 0.2F).build()));
    public static final WeedLighter WEED_LIGHTER = new WeedLighter(new Item.Settings().group(Cannacraft.ITEM_GROUP).maxCount(1).maxDamage(1000));
    public static final WeedGrinder WEED_GRINDER = new WeedGrinder(new Item.Settings().group(Cannacraft.ITEM_GROUP).maxCount(1));

    // Crafting items
    public static final BrownieMix BROWNIE_MIX = new BrownieMix(new Item.Settings().group(Cannacraft.ITEM_GROUP));

    // Crafting items

    // Block items
    public static final BlockItem STRAIN_ANALYZER = new BlockItem(ModBlocks.STRAIN_ANALYZER, new FabricItemSettings().group(Cannacraft.ITEM_GROUP));
    public static final BlockItem SEED_CHEST = new BlockItem(ModBlocks.SEED_CHEST, new Item.Settings().group(Cannacraft.ITEM_GROUP));
    public static final BlockItem SEED_CROSSER = new BlockItem(ModBlocks.SEED_CROSSER, new Item.Settings().group(Cannacraft.ITEM_GROUP));
    public static final BlockItem WEED_EXTRACTOR = new BlockItem(ModBlocks.WEED_EXTRACTOR, new Item.Settings().group(Cannacraft.ITEM_GROUP));
    public static final BlockItem WEED_RACK = new BlockItem(ModBlocks.WEED_RACK, new Item.Settings().group(Cannacraft.ITEM_GROUP));
    public static final BlockItem WEED_BONG = new BlockItem(ModBlocks.WEED_BONG, new Item.Settings().group(Cannacraft.ITEM_GROUP));
    public static final BlockItem GROW_LIGHT = new BlockItem(ModBlocks.GROW_LIGHT, new Item.Settings().group(Cannacraft.ITEM_GROUP));
    public static void registerItems () {
        Registry.register(Registry.ITEM, id( "weed_seed"), WEED_SEED);
        Registry.register(Registry.ITEM, id( "seed_bag"), SEED_BAG);
        Registry.register(Registry.ITEM, id("weed_fruit"), WEED_BUNDLE);
        Registry.register(Registry.ITEM, id("weed_joint"), WEED_JOINT);
        Registry.register(Registry.ITEM, id( "seed_chest"), SEED_CHEST);
        Registry.register(Registry.ITEM, id( "strain_analyzer"), STRAIN_ANALYZER);
        Registry.register(Registry.ITEM, id( "seed_crosser"), SEED_CROSSER);
        Registry.register(Registry.ITEM, id("weed_extractor"), WEED_EXTRACTOR);
        Registry.register(Registry.ITEM, id("manual"), WEED_MANUAL);
        Registry.register(Registry.ITEM, id("weed_distillate"), WEED_DISTILLATE); // TODO
        Registry.register(Registry.ITEM, id("weed_brownie"), WEED_BROWNIE); // TODO
        Registry.register(Registry.ITEM, id("lighter"), WEED_LIGHTER);
        Registry.register(Registry.ITEM, id("weed_grinder"), WEED_GRINDER); // TODO
        Registry.register(Registry.ITEM, id("weed_rack"), WEED_RACK);
        Registry.register(Registry.ITEM, id("weed_bong"), WEED_BONG);
        Registry.register(Registry.ITEM, id("grow_lamp"), GROW_LIGHT);
        Registry.register(Registry.ITEM, id("brownie_mix"), BROWNIE_MIX); // TODO
    }
}
