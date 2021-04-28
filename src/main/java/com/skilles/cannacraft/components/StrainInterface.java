package com.skilles.cannacraft.components;

import com.skilles.cannacraft.registry.ModMisc;
import com.skilles.cannacraft.strain.StrainMap;
import dev.onyxstudios.cca.api.v3.component.ComponentV3;
import net.minecraft.nbt.ListTag;

import java.util.List;

public interface StrainInterface extends ComponentV3 {

    static <T> StrainInterface get(T provider) {
        return ModMisc.STRAIN.get(provider);
    }

    String getStrain();

    void setStrain(int index); // setType not needed

    List<ListTag> getGenetics();

    void setGenetics(ListTag geneList);

    int getIndex();

    StrainMap.Type getType();

    int getThc();

    void setThc(int thc);

    boolean isMale();

    void setMale(boolean isMale);

    void identify();

    boolean identified();

    void copyFrom(ItemStrainComponent other);
}
