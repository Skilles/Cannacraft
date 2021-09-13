package com.skilles.cannacraft.strain;

import com.skilles.cannacraft.dna.genome.gene.TraitGene;

import java.util.List;

// TODO: make this a normal class with genome as constructor parameter
public record StrainInfo(Strain strain, int thc, boolean identified, boolean male, List<TraitGene> geneList) {


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StrainInfo other = (StrainInfo) o;
        return (this.strain == other.strain);
    }

    public StrainInfo asIdentified() {
        return new StrainInfo(this.strain, this.thc, true, this.male, this.geneList);
    }

    public StrainInfo withGenes(List<TraitGene> geneList) {
        return new StrainInfo(this.strain, this.thc, this.identified, this.male, geneList);
    }

}
