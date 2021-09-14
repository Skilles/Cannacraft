package com.skilles.cannacraft.registry;

import com.skilles.cannacraft.blocks.GrowLight;
import com.skilles.cannacraft.blocks.machines.seedCrosser.SeedCrosser;
import com.skilles.cannacraft.blocks.machines.strainAnalyzer.StrainAnalyzer;
import com.skilles.cannacraft.blocks.machines.weedExtractor.WeedExtractor;
import com.skilles.cannacraft.blocks.seedChest.SeedChest;
import com.skilles.cannacraft.blocks.weedBong.WeedBong;
import com.skilles.cannacraft.blocks.weedCrop.WeedCrop;
import com.skilles.cannacraft.blocks.weedRack.WeedRack;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.Material;
import net.minecraft.block.PlantBlock;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.registry.Registry;

import static com.skilles.cannacraft.Cannacraft.id;

public class ModBlocks {

    public static final Block SEED_CHEST = new SeedChest(FabricBlockSettings
            .copyOf(Blocks.CHEST));

    public static final PlantBlock WEED_CROP = new WeedCrop(FabricBlockSettings.of(Material.PLANT).noCollision().ticksRandomly().breakInstantly().sounds(BlockSoundGroup.GRASS));

    public static final Block STRAIN_ANALYZER = new StrainAnalyzer(FabricBlockSettings
            .copyOf(Blocks.COAL_BLOCK));
    public static final Block SEED_CROSSER = new SeedCrosser(FabricBlockSettings
            .copyOf(Blocks.COAL_BLOCK));
    public static final Block WEED_EXTRACTOR = new WeedExtractor(FabricBlockSettings
            .copyOf(Blocks.COAL_BLOCK));
    public static final Block WEED_BONG = new WeedBong(FabricBlockSettings.copyOf(Blocks.GLASS));
    public static final Block WEED_RACK = new WeedRack(FabricBlockSettings.copyOf(Blocks.OAK_PLANKS));
    public static final Block GROW_LIGHT = new GrowLight(FabricBlockSettings.copyOf(Blocks.REDSTONE_LAMP));

    public static void registerBlocks() {
        Registry.register(Registry.BLOCK, id( "seed_chest"), SEED_CHEST);
        Registry.register(Registry.BLOCK, id( "weed_crop"), WEED_CROP);
        FlammableBlockRegistry.getDefaultInstance().add(WEED_CROP, 60, 60);
        Registry.register(Registry.BLOCK, id( "strain_analyzer"), STRAIN_ANALYZER);
        Registry.register(Registry.BLOCK, id( "weed_extractor"), WEED_EXTRACTOR);
        Registry.register(Registry.BLOCK, id( "seed_crosser"), SEED_CROSSER);
        Registry.register(Registry.BLOCK, id( "weed_bong"), WEED_BONG);
        Registry.register(Registry.BLOCK, id( "weed_rack"), WEED_RACK);
        Registry.register(Registry.BLOCK, id( "grow_lamp"), GROW_LIGHT);
    }
}
