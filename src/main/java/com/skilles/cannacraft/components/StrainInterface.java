package com.skilles.cannacraft.components;

import com.skilles.cannacraft.registry.ModMisc;
import com.skilles.cannacraft.strain.StrainMap;
import dev.onyxstudios.cca.api.v3.component.ComponentV3;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;

import java.util.List;

public interface StrainInterface extends ComponentV3 {

    static <T> StrainInterface get(T provider) {
        return ModMisc.STRAIN.get(provider);
    }

    void setStrain(int index); // setType not needed
    void setGenetics(NbtList geneList);
    void setThc(int thc);
    void setMale(boolean isMale);

    void addGene(String name, int level);
    boolean hasGenes();

    String getStrain();
    List<NbtCompound> getGenetics();
    int getIndex();
    StrainMap.Type getType();
    int getThc();
    boolean isMale();
    boolean isResource();

    TriState getStatus();
    void setStatus(TriState status);

    void identify();
    boolean identified();

    void copyFrom(ItemStrainComponent other);
}
