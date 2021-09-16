package com.skilles.cannacraft.registry;

import com.skilles.cannacraft.Cannacraft;
import com.skilles.cannacraft.blocks.machines.generator.GeneratorEntity;
import com.skilles.cannacraft.blocks.machines.seedCrosser.SeedCrosserEntity;
import com.skilles.cannacraft.blocks.machines.strainAnalyzer.StrainAnalyzerEntity;
import com.skilles.cannacraft.blocks.machines.weedExtractor.WeedExtractorEntity;
import com.skilles.cannacraft.blocks.seedChest.SeedChestEntity;
import com.skilles.cannacraft.blocks.weedBong.WeedBongEntity;
import com.skilles.cannacraft.blocks.weedCrop.WeedCropEntity;
import com.skilles.cannacraft.blocks.weedRack.WeedRackEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.ItemConvertible;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import org.apache.commons.lang3.Validate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;

public class BlockEntities {

    private static final List<BlockEntityType<?>> TYPES = new ArrayList<>();

    public static final BlockEntityType<WeedRackEntity> RACK = register(WeedRackEntity::new, "weed_rack", ModContent.RACK);
    public static final BlockEntityType<WeedCropEntity> CROP = register(WeedCropEntity::new, "weed_crop", ModContent.WEED_CROP);
    public static final BlockEntityType<StrainAnalyzerEntity> ANALYZER = register(StrainAnalyzerEntity::new, "strain_analyzer", ModContent.Machine.STRAIN_ANALYZER);
    public static final BlockEntityType<SeedCrosserEntity> CROSSER = register(SeedCrosserEntity::new, "seed_crosser", ModContent.Machine.SEED_CROSSER);
    public static final BlockEntityType<WeedExtractorEntity> EXTRACTOR = register(WeedExtractorEntity::new, "weed_extractor", ModContent.Machine.WEED_EXTRACTOR);
    public static final BlockEntityType<WeedBongEntity> BONG = register(WeedBongEntity::new, "weed_bong", ModContent.BONG);
    public static final BlockEntityType<GeneratorEntity> GENERATOR = register(GeneratorEntity::new, "generator", ModContent.Machine.GENERATOR);
    public static final BlockEntityType<SeedChestEntity> SEED_CHEST = register(SeedChestEntity::new, "seed_chest", ModContent.SEED_CHEST);

    public static <T extends BlockEntity> BlockEntityType<T> register(BiFunction<BlockPos, BlockState, T> supplier, String name, ItemConvertible... items) {
        return register(supplier, name, Arrays.stream(items).map(itemConvertible -> Block.getBlockFromItem(itemConvertible.asItem())).toArray(Block[]::new));
    }

    public static <T extends BlockEntity> BlockEntityType<T> register(BiFunction<BlockPos, BlockState, T> supplier, String name, Block... blocks) {
        Validate.isTrue(blocks.length > 0, "no blocks for blockEntity entity type!");
        return register(Cannacraft.id(name).toString(), FabricBlockEntityTypeBuilder.create(supplier::apply, blocks));
    }

    public static <T extends BlockEntity> BlockEntityType<T> register(String id, FabricBlockEntityTypeBuilder<T> builder) {
        BlockEntityType<T> blockEntityType = builder.build(null);
        Registry.register(Registry.BLOCK_ENTITY_TYPE, new Identifier(id), blockEntityType);
        BlockEntities.TYPES.add(blockEntityType);
        return blockEntityType;
    }


}
