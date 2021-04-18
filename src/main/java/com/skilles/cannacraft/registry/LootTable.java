package com.skilles.cannacraft.registry;

import com.google.common.collect.Lists;
import com.skilles.cannacraft.items.ItemStrainComponent;
import net.fabricmc.fabric.api.loot.v1.FabricLootPoolBuilder;
import net.fabricmc.fabric.api.loot.v1.FabricLootSupplierBuilder;
import net.fabricmc.fabric.api.loot.v1.event.LootTableLoadingCallback;
import net.minecraft.loot.condition.RandomChanceLootCondition;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.function.SetNbtLootFunction;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.apache.commons.lang3.ArrayUtils;

import java.util.List;

public class LootTable {
    private static final Identifier GRASS_LOOT_TABLE_ID = new Identifier("minecraft", "blocks/grass");
    private static final Identifier TALL_LOOT_TABLE_ID = new Identifier("minecraft", "blocks/tall_grass");
    private static final Identifier FERN_LOOT_TABLE_ID = new Identifier("minecraft", "blocks/fern");
    private static final Identifier LARGE_FERN_LOOT_TABLE_ID = new Identifier("minecraft", "blocks/large_fern");

    static boolean enable = true;
    static float chestLootChance = .04f;
    static float dropLootChance = 0.5f;

    private static final List<LootTableInsert> INSERTS = Lists.newArrayList();
    static FabricLootPoolBuilder lootPool() {
        FabricLootPoolBuilder poolBuilder = FabricLootPoolBuilder.builder()
                .rolls(ConstantLootNumberProvider.create(1))
                .withCondition(RandomChanceLootCondition.builder(dropLootChance).build());
        int i;
        for(i = 0; i < ItemStrainComponent.STRAIN_COUNT; i++) {
            int finalI = i;
            FabricLootPoolBuilder newPoolBuilder = poolBuilder
                    .withEntry(ItemEntry.builder(ModItems.SEED).apply(SetNbtLootFunction.builder(Util.make(new NbtCompound(), (nbtCompound) -> {
                        NbtCompound strainNbt = new NbtCompound();
                        strainNbt.putInt("ID", finalI);
                        nbtCompound.put("cannacraft:strain", strainNbt);
                    }))).build()); // dynamically add new strains to loot table
            poolBuilder = newPoolBuilder;
        }
        return poolBuilder;
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

    public static class LootTableInsert
    {
        public final Identifier[] tables;
        public final FabricLootPoolBuilder lootPool;
        public LootTableInsert(FabricLootPoolBuilder lootPool, Identifier... tables)
        {
            this.tables = tables;
            this.lootPool = lootPool;
        }

        public void insert(FabricLootSupplierBuilder supplier)
        {
            supplier.pool(lootPool);
        }
    }
}
