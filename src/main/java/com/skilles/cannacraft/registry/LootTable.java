package com.skilles.cannacraft.registry;

import com.google.common.collect.Lists;
import net.fabricmc.fabric.api.loot.v1.FabricLootPoolBuilder;
import net.fabricmc.fabric.api.loot.v1.FabricLootSupplierBuilder;
import net.fabricmc.fabric.api.loot.v1.event.LootTableLoadingCallback;
import net.minecraft.loot.condition.RandomChanceLootCondition;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.function.SetNbtLootFunction;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.ArrayUtils;

import java.util.List;

public class LootTable {
    private static final Identifier GRASS_LOOT_TABLE_ID = new Identifier("minecraft", "blocks/grass");
    private static final Identifier TALL_LOOT_TABLE_ID = new Identifier("minecraft", "blocks/tall_grass");
    private static final Identifier FERN_LOOT_TABLE_ID = new Identifier("minecraft", "blocks/fern");
    private static final Identifier LARGE_FERN_LOOT_TABLE_ID = new Identifier("minecraft", "blocks/large_fern");

    private static final boolean enable = true;
    private static final float chestLootChance = .04f;
    private static final float dropLootChance = 0.5f;

    private static final List<LootTableInsert> INSERTS = Lists.newArrayList();
    static FabricLootPoolBuilder lootPool() {
        NbtCompound subTag = new NbtCompound();
        NbtCompound baseTag = new NbtCompound();
        subTag.putInt("ID", 0);
        baseTag.put("cannacraft:strain", subTag);
        return FabricLootPoolBuilder.builder()
                .rolls(ConstantLootNumberProvider.create(1))
                .withCondition(RandomChanceLootCondition.builder(dropLootChance).build()).withEntry(ItemEntry.builder(ModItems.WEED_SEED).apply(SetNbtLootFunction.builder(baseTag)).build());
    }
    private static final FabricLootPoolBuilder lootPool = lootPool();
    public static void registerLoot()
    {
        if(enable)
        {
            INSERTS.add(new LootTableInsert(lootPool,
                    new Identifier("minecraft", "chests/desert_pyramid"),
                    new Identifier("minecraft", "chests/jungle_temple"),
                    new Identifier("minecraft", "chests/igloo_chest"),
                    new Identifier("minecraft", "chests/pillager_outpost"),
                    GRASS_LOOT_TABLE_ID, TALL_LOOT_TABLE_ID, FERN_LOOT_TABLE_ID, LARGE_FERN_LOOT_TABLE_ID
            ));

            LootTableLoadingCallback.EVENT.register(((resourceManager, lootManager, identifier, supplier, lootTableSetter) -> {
                INSERTS.forEach(i->{
                    if(ArrayUtils.contains(i.tables, identifier))
                    {
                        i.insert(supplier);
                    }
                });
            }));
        }
    }

    public record LootTableInsert(FabricLootPoolBuilder lootPool,
                                  Identifier... tables) {

        public void insert(FabricLootSupplierBuilder supplier) {
            supplier.pool(lootPool);
        }
    }
}
