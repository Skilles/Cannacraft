package com.skilles.cannacraft.components;

import com.skilles.cannacraft.registry.ModMisc;
import com.skilles.cannacraft.strain.Strain;
import dev.onyxstudios.cca.api.v3.component.ComponentV3;

public interface EntityInterface extends ComponentV3 {
    static <T> StrainInterface get(T provider) {
        return ModMisc.STRAIN.get(provider);
    }

    Strain getStrain();

    void setStrain(int index);

}
