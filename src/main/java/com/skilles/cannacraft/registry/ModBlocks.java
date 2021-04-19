package com.skilles.cannacraft.registry;

import com.skilles.cannacraft.Cannacraft;
import com.skilles.cannacraft.blocks.seedChest.SeedChest;
import com.skilles.cannacraft.blocks.strainAnalyzer.StrainAnalyzer;
import com.skilles.cannacraft.blocks.weedCrop.WeedCrop;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.CropBlock;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class ModBlocks {

    public static final Block SEED_CHEST = new SeedChest(FabricBlockSettings
            .copyOf(Blocks.CHEST));

    public static final CropBlock WEED_CROP = new WeedCrop(FabricBlockSettings
            .copyOf(Blocks.WHEAT));

    public static final Block STRAIN_ANALYZER = new StrainAnalyzer(FabricBlockSettings
            .copyOf(Blocks.COAL_BLOCK));

    public static void registerBlocks() {
        Registry.register(Registry.BLOCK, new Identifier(Cannacraft.MOD_ID, "seed_chest"), SEED_CHEST);
        Registry.register(Registry.BLOCK, new Identifier(Cannacraft.MOD_ID, "weed_crop"), WEED_CROP);
        Registry.register(Registry.BLOCK, new Identifier(Cannacraft.MOD_ID, "strain_analyzer"), STRAIN_ANALYZER);
    }
}
