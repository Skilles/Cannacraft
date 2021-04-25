package com.skilles.cannacraft.registry;

import com.skilles.cannacraft.blocks.machines.seedCrosser.SeedCrosserEntity;
import com.skilles.cannacraft.blocks.machines.strainAnalyzer.StrainAnalyzerEntity;
import com.skilles.cannacraft.blocks.seedChest.SeedChestEntity;
import com.skilles.cannacraft.blocks.weedCrop.WeedCropEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.registry.Registry;

public class ModEntities {

    public static BlockEntityType<SeedChestEntity> SEED_CHEST_ENTITY;
    public static BlockEntityType<StrainAnalyzerEntity> STRAIN_ANALYZER_ENTITY;
    public static BlockEntityType<WeedCropEntity> WEED_CROP_ENTITY;
    public static BlockEntityType<SeedCrosserEntity> SEED_CROSSER_ENTITY;

    public static void registerEntities() {
        SEED_CHEST_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, "cannacraft:seed_chest", FabricBlockEntityTypeBuilder.create(SeedChestEntity::new, ModBlocks.SEED_CHEST).build());
        WEED_CROP_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, "cannacraft:weed_crop", FabricBlockEntityTypeBuilder.create(WeedCropEntity::new, ModBlocks.WEED_CROP).build());
        STRAIN_ANALYZER_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, "cannacraft:strain_analyzer", FabricBlockEntityTypeBuilder.create(StrainAnalyzerEntity::new, ModBlocks.STRAIN_ANALYZER).build());
        SEED_CROSSER_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, "cannacraft:seed_crosser", FabricBlockEntityTypeBuilder.create(SeedCrosserEntity::new, ModBlocks.SEED_CROSSER).build());
    }
}
