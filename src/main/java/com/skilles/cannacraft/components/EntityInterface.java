package com.skilles.cannacraft.components;

import com.skilles.cannacraft.registry.ModMisc;
import com.skilles.cannacraft.strain.Strain;
import com.skilles.cannacraft.strain.cannadex.Cannadex;
import dev.onyxstudios.cca.api.v3.component.ComponentV3;
import net.minecraft.client.MinecraftClient;

public interface EntityInterface extends ComponentV3 {
    static <T> StrainInterface get(T provider) {
        return ModMisc.STRAIN.get(provider);
    }

    void setStrain(int index);

    Strain getStrain();

    Cannadex getCannadex();

    void discoverStrain(Strain strain, MinecraftClient client);

    boolean isDiscovered(Strain strain);

}
