package com.skilles.cannacraft.items;

import com.skilles.cannacraft.registry.ModComponents;
import com.skilles.cannacraft.strain.StrainMap;
import dev.onyxstudios.cca.api.v3.component.ComponentV3;
import net.minecraft.nbt.NbtList;

import java.util.List;

public interface StrainInterface extends ComponentV3 {

    static <T> StrainInterface get(T provider) {
        return ModComponents.STRAIN.get(provider);
    }

    void setStrain(int index); // setType not needed
    void setGenetics(NbtList geneList);
    void setThc(int thc);
    void setMale(boolean isMale);


    String getStrain();
    List getGenetics();
    int getIndex();
    StrainMap.Type getType();
    int getThc();
    boolean isMale();


    void identify();
    boolean identified();

    void copyFrom(ItemStrainComponent other);
}
