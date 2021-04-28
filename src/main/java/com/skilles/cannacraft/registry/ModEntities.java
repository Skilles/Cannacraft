package com.skilles.cannacraft.registry;

import com.skilles.cannacraft.blocks.machines.seedCrosser.SeedCrosserEntity;
import com.skilles.cannacraft.blocks.machines.strainAnalyzer.StrainAnalyzerEntity;
import com.skilles.cannacraft.blocks.seedChest.SeedChestEntity;
import com.skilles.cannacraft.blocks.weedCrop.WeedCropEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.registry.Registry;

public class ModEntities {

    public static BlockEntityType<SeedChestEntity> SEED_CHEST_ENTITY;
    public static BlockEntityType<StrainAnalyzerEntity> STRAIN_ANALYZER_ENTITY;
    public static BlockEntityType<WeedCropEntity> WEED_CROP_ENTITY;
    public static BlockEntityType<SeedCrosserEntity> SEED_CROSSER_ENTITY;

    public static void registerEntities() {
        SEED_CHEST_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, "cannacraft:seed_chest", BlockEntityType.Builder.create(SeedChestEntity::new, ModBlocks.SEED_CHEST).build(null));
        WEED_CROP_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, "cannacraft:weed_crop", BlockEntityType.Builder.create(WeedCropEntity::new, ModBlocks.WEED_CROP).build(null));
        STRAIN_ANALYZER_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, "cannacraft:strain_analyzer", BlockEntityType.Builder.create(StrainAnalyzerEntity::new, ModBlocks.STRAIN_ANALYZER).build(null));
        SEED_CROSSER_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, "cannacraft:seed_crosser", BlockEntityType.Builder.create(SeedCrosserEntity::new, ModBlocks.SEED_CROSSER).build(null));
    }
}
