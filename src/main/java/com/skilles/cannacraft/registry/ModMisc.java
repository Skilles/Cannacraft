package com.skilles.cannacraft.registry;

import com.skilles.cannacraft.blocks.weedCrop.WeedCrop;
import com.skilles.cannacraft.components.EntityInterface;
import com.skilles.cannacraft.components.ItemStrainComponent;
import com.skilles.cannacraft.components.PlayerStrainComponent;
import com.skilles.cannacraft.components.StrainInterface;
import com.skilles.cannacraft.misc.HighEffect;
import com.skilles.cannacraft.util.BundleUtil;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistryV3;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import dev.onyxstudios.cca.api.v3.entity.RespawnCopyStrategy;
import dev.onyxstudios.cca.api.v3.item.ItemComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.item.ItemComponentInitializer;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.object.builder.v1.client.model.FabricModelPredicateProviderRegistry;
import net.fabricmc.fabric.api.object.builder.v1.villager.VillagerProfessionBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DataPool;
import net.minecraft.util.registry.BuiltinRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.village.VillagerProfession;
import net.minecraft.world.Heightmap;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.decorator.Decorator;
import net.minecraft.world.gen.decorator.HeightmapDecoratorConfig;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.RandomPatchFeatureConfig;
import net.minecraft.world.gen.placer.SimpleBlockPlacer;
import net.minecraft.world.gen.stateprovider.SimpleBlockStateProvider;
import net.minecraft.world.poi.PointOfInterestType;

import static com.skilles.cannacraft.Cannacraft.id;

public class ModMisc implements ItemComponentInitializer, EntityComponentInitializer {
    public static final ComponentKey<StrainInterface> STRAIN = ComponentRegistryV3.INSTANCE.getOrCreate(id("strain"), StrainInterface.class);
    public static final ComponentKey<EntityInterface> PLAYER = ComponentRegistryV3.INSTANCE.getOrCreate(id("player"), EntityInterface.class);
    //public static final ConfiguredFeature<?, ?> WEED_CROP_FEATURE_CONFIGURED = WeedCropFeature.RANDOM_PATCH.configure((new net.minecraft.world.gen.feature.RandomPatchFeatureConfig.Builder(new SimpleBlockStateProvider(ModBlocks.WEED_CROP.getDefaultState()), new DoublePlantPlacer())).tries(64).cannotProject().build()).decorate(Decorator.SPREAD_32_ABOVE.configure(DecoratorConfig.DEFAULT));
    @Override
    public void registerItemComponentFactories(ItemComponentFactoryRegistry registry) {
        registry.register(ModItems.WEED_SEED, STRAIN, ItemStrainComponent::new);
        registry.register(ModItems.WEED_BUNDLE, STRAIN, ItemStrainComponent::new);
    }
    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.registerForPlayers(PLAYER, player -> new PlayerStrainComponent(), RespawnCopyStrategy.LOSSLESS_ONLY);
    }

    private static void registerRecipes() {
        //ANALYZE_RECIPE_SERIALIZER = AnalyzeRecipeSerializer.register("analyze_serializer", new AnalyzeRecipeSerializer(AnalyzeRecipe::new));
    }

    public static final StatusEffect HIGH = new HighEffect();
    private static void registerEffects() {
        Registry.register(Registry.STATUS_EFFECT, id("high"), HIGH);
    }

    private static final DataPool.Builder<BlockState> WEED_CROP_POOL;
    private static final RandomPatchFeatureConfig WEED_CROP_CONFIG;
    public static final ConfiguredFeature<?, ?> WEED_CROP_FEATURE;
    private static final BlockState WEED_CROP_STATE = ModBlocks.WEED_CROP.getDefaultState();

    static {
        WEED_CROP_POOL = new DataPool.Builder<BlockState>().add(WEED_CROP_STATE.with(WeedCrop.AGE, 1), 1).add(WEED_CROP_STATE.with(WeedCrop.AGE, 3), 2).add(WEED_CROP_STATE.with(WeedCrop.AGE, 5), 2).add(WEED_CROP_STATE.with(WeedCrop.AGE, 7), 1);
        //WEED_CROP_CONFIG = (new RandomPatchFeatureConfig.Builder(new WeightedBlockStateProvider(WEED_CROP_POOL), SimpleBlockPlacer.INSTANCE)).tries(64).needsWater().build();
        WEED_CROP_CONFIG = (new net.minecraft.world.gen.feature.RandomPatchFeatureConfig.Builder(new SimpleBlockStateProvider(ModBlocks.STRAIN_ANALYZER.getDefaultState()), SimpleBlockPlacer.INSTANCE)).tries(32).build();
        WEED_CROP_FEATURE = Feature.RANDOM_PATCH.configure(WEED_CROP_CONFIG).decorate(Decorator.HEIGHTMAP_SPREAD_DOUBLE.configure(new HeightmapDecoratorConfig(Heightmap.Type.MOTION_BLOCKING)).spreadHorizontally().repeat(5));
    }

    private static void registerConfiguredFeature() {
        Registry.register(BuiltinRegistries.CONFIGURED_FEATURE, id("weed_crop_feature"), WEED_CROP_FEATURE);
    }

    private static void registerBiomeModifications() {
        BuiltinRegistries.CONFIGURED_FEATURE.getKey(WEED_CROP_FEATURE)
                .ifPresent(key -> BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(),
                        GenerationStep.Feature.VEGETAL_DECORATION, key));
    }
    private static void registerModelPredicates() {
        FabricModelPredicateProviderRegistry.register(ModItems.WEED_JOINT, new Identifier("lit"), (itemStack, clientWorld, livingEntity, seed) -> itemStack.hasTag() ? itemStack.getTag().getBoolean("Lit") ? 1.0F : 0.0F : 0.0F);
        FabricModelPredicateProviderRegistry.register(ModItems.WEED_BUNDLE, new Identifier("count"), (itemStack, clientWorld, livingEntity, seed) -> BundleUtil.getTexture(itemStack));
    }
    public static final VillagerProfession STONER = VillagerProfessionBuilder.create().id(new Identifier("stoner")).workstation(PointOfInterestType.FARMER).harvestableItems(ModItems.WEED_SEED).secondaryJobSites(Blocks.FARMLAND).workSound(SoundEvents.ENTITY_VILLAGER_WORK_FARMER).build();
    private static void registerVillagers() {
        Registry.register(Registry.VILLAGER_PROFESSION, id("stoner"), STONER);
    }
    public static void registerMisc() {
        registerConfiguredFeature();
        registerBiomeModifications();
        registerEffects();
        registerModelPredicates();
        registerVillagers();
    }
}
