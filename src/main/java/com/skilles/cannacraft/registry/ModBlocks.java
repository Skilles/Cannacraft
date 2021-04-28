package com.skilles.cannacraft.registry;

import com.skilles.cannacraft.Cannacraft;
import com.skilles.cannacraft.blocks.machines.seedCrosser.SeedCrosser;
import com.skilles.cannacraft.blocks.machines.strainAnalyzer.StrainAnalyzer;
import com.skilles.cannacraft.blocks.seedChest.SeedChest;
import com.skilles.cannacraft.blocks.weedCrop.WeedCrop;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.Material;
import net.minecraft.block.PlantBlock;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class ModBlocks {

    public static final Block SEED_CHEST = new SeedChest(FabricBlockSettings
            .copyOf(Blocks.CHEST));

    public static final PlantBlock WEED_CROP = new WeedCrop(FabricBlockSettings.of(Material.PLANT).noCollision().ticksRandomly().breakInstantly().sounds(BlockSoundGroup.GRASS));

    public static final Block STRAIN_ANALYZER = new StrainAnalyzer(FabricBlockSettings
            .copyOf(Blocks.COAL_BLOCK));
    public static final Block SEED_CROSSER = new SeedCrosser(FabricBlockSettings
            .copyOf(Blocks.COAL_BLOCK));

    public static void registerBlocks() {
        Registry.register(Registry.BLOCK, new Identifier(Cannacraft.MOD_ID, "seed_chest"), SEED_CHEST);
        Registry.register(Registry.BLOCK, new Identifier(Cannacraft.MOD_ID, "weed_crop"), WEED_CROP);
        Registry.register(Registry.BLOCK, new Identifier(Cannacraft.MOD_ID, "strain_analyzer"), STRAIN_ANALYZER);
        Registry.register(Registry.BLOCK, new Identifier(Cannacraft.MOD_ID, "seed_crosser"), SEED_CROSSER);
    }
}
