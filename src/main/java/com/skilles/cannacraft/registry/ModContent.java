package com.skilles.cannacraft.registry;

import com.skilles.cannacraft.blocks.machines.MachineBlock;
import com.skilles.cannacraft.blocks.machines.generator.GeneratorEntity;
import com.skilles.cannacraft.blocks.machines.seedCrosser.SeedCrosserEntity;
import com.skilles.cannacraft.blocks.machines.strainAnalyzer.StrainAnalyzerEntity;
import com.skilles.cannacraft.blocks.machines.weedExtractor.WeedExtractorEntity;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;

public class ModContent {

    // Items
    public static Item WEED_SEED;
    public static Item JOINT;
    public static Item WEED_BUNDLE;
    public static Item SEED_BAG;
    public static Item MANUAL;
    public static Item DISTILLATE;
    public static Item BROWNIE;
    public static Item LIGHTER;
    public static Item GRINDER;

    public static Item BROWNIE_MIX;

    // Blocks
    public static Block SEED_CHEST;
    public static Block RACK;
    public static Block BONG;
    public static Block GROW_LIGHT;
    public static Block WEED_CROP;

    public enum Machine implements ItemConvertible {
        STRAIN_ANALYZER(new MachineBlock(StrainAnalyzerEntity::new)),
        SEED_CROSSER(new MachineBlock(SeedCrosserEntity::new)),
        WEED_EXTRACTOR(new MachineBlock(WeedExtractorEntity::new)),
        GENERATOR(new MachineBlock(GeneratorEntity::new));

        public final String name;

        public final Block block;

        Machine(MachineBlock machineBlock) {
            this.name = this.toString().toLowerCase();
            this.block = machineBlock;
            RegistryManager.setup(block, name);
        }


        @Override
        public Item asItem() {
            return block.asItem();
        }
    }
}
