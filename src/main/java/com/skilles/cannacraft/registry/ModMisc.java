package com.skilles.cannacraft.registry;

import com.skilles.cannacraft.components.EntityInterface;
import com.skilles.cannacraft.components.ItemStrainComponent;
import com.skilles.cannacraft.components.PlayerStrainComponent;
import com.skilles.cannacraft.components.StrainInterface;
import com.skilles.cannacraft.misc.HighEffect;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistryV3;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import dev.onyxstudios.cca.api.v3.entity.RespawnCopyStrategy;
import dev.onyxstudios.cca.api.v3.item.ItemComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.item.ItemComponentInitializer;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.util.registry.Registry;

import static com.skilles.cannacraft.Cannacraft.id;

public class ModMisc implements ItemComponentInitializer, EntityComponentInitializer {
    public static final ComponentKey<StrainInterface> STRAIN = ComponentRegistryV3.INSTANCE.getOrCreate(id("strain"), StrainInterface.class);
    public static final ComponentKey<EntityInterface> PLAYER = ComponentRegistryV3.INSTANCE.getOrCreate(id("player"), EntityInterface.class);

    @Override
    public void registerItemComponentFactories(ItemComponentFactoryRegistry registry) {
        registry.register(ModItems.WEED_SEED, STRAIN, ItemStrainComponent::new);
        registry.register(ModItems.WEED_FRUIT, STRAIN, ItemStrainComponent::new);
    }
    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.registerForPlayers(PLAYER, player -> new PlayerStrainComponent(), RespawnCopyStrategy.LOSSLESS_ONLY);
    }

    public static void registerRecipes() {
        //ANALYZE_RECIPE_SERIALIZER = AnalyzeRecipeSerializer.register("analyze_serializer", new AnalyzeRecipeSerializer(AnalyzeRecipe::new));
    }

    public static final StatusEffect HIGH = new HighEffect();
    public static void registerEffects() {
        Registry.register(Registry.STATUS_EFFECT, id("high"), HIGH);
    }


}
