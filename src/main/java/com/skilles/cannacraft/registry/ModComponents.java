package com.skilles.cannacraft.registry;

import com.skilles.cannacraft.Cannacraft;
import com.skilles.cannacraft.items.StrainComponent;
import com.skilles.cannacraft.items.StrainInterface;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistryV3;
import dev.onyxstudios.cca.api.v3.item.ItemComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.item.ItemComponentInitializer;

public final class ModComponents implements ItemComponentInitializer {
    public static final ComponentKey<StrainInterface> STRAIN = ComponentRegistryV3.INSTANCE.getOrCreate(Cannacraft.id("straincomponent"), StrainInterface.class);



    @Override
    public void registerItemComponentFactories(ItemComponentFactoryRegistry registry) {

        registry.register(ModItems.SEED, STRAIN, StrainComponent::new);
    }
}
