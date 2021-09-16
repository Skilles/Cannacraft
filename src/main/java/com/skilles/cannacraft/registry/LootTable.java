package com.skilles.cannacraft.registry;

import com.google.common.collect.Lists;
import net.fabricmc.fabric.api.loot.v1.FabricLootPoolBuilder;
import net.fabricmc.fabric.api.loot.v1.FabricLootSupplierBuilder;
import net.fabricmc.fabric.api.loot.v1.event.LootTableLoadingCallback;
import net.minecraft.loot.condition.RandomChanceLootCondition;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.ArrayUtils;

import java.util.List;

public class LootTable {

    private static final boolean enable = true;

    private static final float CHEST_CHANCE = .04f;

    private static final float DROP_CHANCE = 0.5f;


    private static final List<LootTableInsert> INSERTS = Lists.newArrayList();

    private static FabricLootPoolBuilder lootPool() {
        /*NbtCompound subTag = new NbtCompound();
        NbtCompound baseTag = new NbtCompound();
        subTag.putString("DNA", "");
        baseTag.put("cannacraft:strain", subTag);*/
        return FabricLootPoolBuilder.builder()
                .rolls(ConstantLootNumberProvider.create(1))
                .withCondition(RandomChanceLootCondition.builder(DROP_CHANCE).build()).withEntry(ItemEntry.builder(ModContent.WEED_SEED).build()); //.apply(SetNbtLootFunction.builder(baseTag)).build());
    }

    private static final FabricLootPoolBuilder lootPool = lootPool();

    public static void registerLoot() {
        if (enable) {
            INSERTS.add(new LootTableInsert(lootPool,
                    new Identifier("minecraft", "chests/desert_pyramid"),
                    new Identifier("minecraft", "chests/jungle_temple"),
                    new Identifier("minecraft", "chests/igloo_chest"),
                    new Identifier("minecraft", "chests/pillager_outpost"),
                    new Identifier("minecraft", "blocks/grass"),
                    new Identifier("minecraft", "blocks/tall_grass"),
                    new Identifier("minecraft", "blocks/fern"),
                    new Identifier("minecraft", "blocks/large_fern")
            ));

            LootTableLoadingCallback.EVENT.register((resourceManager, lootManager, identifier, supplier, lootTableSetter)
                    -> INSERTS.forEach(i -> {
                        if (ArrayUtils.contains(i.tables, identifier)) {
                            i.insert(supplier);
                        }
                    }));
        }
    }

    private record LootTableInsert(FabricLootPoolBuilder lootPool,
                                  Identifier... tables) {

        private void insert(FabricLootSupplierBuilder supplier) {
            supplier.pool(lootPool);
        }
    }
}
