package com.skilles.cannacraft.components;

import com.skilles.cannacraft.dna.genome.Genome;
import com.skilles.cannacraft.dna.genome.gene.TraitGene;
import com.skilles.cannacraft.registry.ModMisc;
import com.skilles.cannacraft.strain.StrainInfo;
import dev.onyxstudios.cca.api.v3.component.ComponentV3;

import java.util.List;

import static com.skilles.cannacraft.util.WeedRegistry.StatusTypes;

public interface StrainInterface extends ComponentV3 {

    static <T> StrainInterface get(T provider) {
        return ModMisc.STRAIN.get(provider);
    }

    void setStrain(int index); // setType not needed

    void setTraits(List<TraitGene> geneList);

    void setThc(int thc);

    void setMale(boolean isMale);

    void addGene(TraitGene gene);

    boolean hasGenes();

    StrainInfo getStrainInfo();

    List<TraitGene> getTraits();

    List<TraitGene> getExpressed();

    int getThc();

    boolean isMale();

    StatusTypes getStatus();

    void setStatus(StatusTypes status);

    void identify();

    boolean identified();

    Genome getGenome();

    void copyFrom(ItemStrainComponent other);

}
