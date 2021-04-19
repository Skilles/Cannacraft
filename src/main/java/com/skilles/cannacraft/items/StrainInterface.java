package com.skilles.cannacraft.items;

import com.skilles.cannacraft.registry.ModComponents;
import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.ComponentV3;

public interface StrainInterface extends ComponentV3 {

    static <T> StrainInterface get(T provider) {
        return ModComponents.STRAIN.get(provider);
    }

    void setStrain(int index); // setType not needed
    void setIndex(int index);
    void setThc(int thc);


    String getStrain();
    int getIndex();
    int getIndex(String strain);
    String getType();
    int getThc();


    void identify();
    boolean identified();

    void copyFrom(Component other);
}
