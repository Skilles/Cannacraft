package com.skilles.cannacraft.registry;

import com.skilles.cannacraft.blocks.seedChest.SeedChestEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.registry.Registry;

public class ModEntities {

    public static BlockEntityType<SeedChestEntity> SEED_CHEST_ENTITY;

    public static void registerEntities() {
        SEED_CHEST_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, "cannacraft:seed_chest", BlockEntityType.Builder.create(SeedChestEntity::new, ModBlocks.SEED_CHEST).build(null));
    }
}
