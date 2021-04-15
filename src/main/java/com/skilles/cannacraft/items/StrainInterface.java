package com.skilles.cannacraft.items;

import com.skilles.cannacraft.registry.ModComponents;
import dev.onyxstudios.cca.api.v3.component.ComponentV3;

public interface StrainInterface extends ComponentV3 {

    static <T> StrainInterface get(T provider) {
        return ModComponents.STRAIN.get(provider);
    }

    void setStrain(String strain);
    void setIndex(int index);
    void setType(String type);
    void setThc(int thc);

    String getStrainNBT();

    void setTag();
    void sync();
    void readNbt();

    String getStrain();
    int getIndex();
    String getType();
    int getTHC();

    void identify();
    boolean identified();

    @Deprecated
    default String syncTest() {
        return null;
    }

    @Deprecated
    default void transferTo(StrainInterface dest, int amount) {
        int strainIndex = this.getIndex();
        int actualAmount = Math.min(strainIndex, amount);
        this.setIndex(strainIndex - actualAmount);
        dest.setIndex(dest.getIndex() + actualAmount);
    }
}
