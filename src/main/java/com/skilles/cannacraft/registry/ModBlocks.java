package com.skilles.cannacraft.registry;

import com.skilles.cannacraft.Cannacraft;
import com.skilles.cannacraft.blocks.seedChest.SeedChest;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class ModBlocks {

    public static final Block SEED_CHEST = new SeedChest(FabricBlockSettings
            .copyOf(Blocks.CHEST));



    public static void registerBlocks() {
        Registry.register(Registry.BLOCK, new Identifier(Cannacraft.MOD_ID, "seed_chest"), SEED_CHEST);
    }
    public static void applyBlockRender() {
      //  BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.SEED_CHEST, RenderLayer.getCutout());
    }
}
