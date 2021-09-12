package com.skilles.cannacraft.dna.chromosome;

import com.skilles.cannacraft.dna.genome.gene.BaseGene;
import com.skilles.cannacraft.dna.genome.gene.InfoGene;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

import static com.skilles.cannacraft.dna.genome.Enums.ChromoType;
import static com.skilles.cannacraft.dna.genome.Enums.InfoType;
import static com.skilles.cannacraft.util.DnaUtil.STOP_CODON;

// TODO: probably delete, make genetype in as optional parameter,

public class InfoChromosome extends BaseChromosome {

    public Map<InfoType, InfoGene> infoMap = new EnumMap<>(InfoType.class);

    public InfoChromosome(String sequence) {
        super(sequence, ChromoType.INFO);
        for (BaseGene gene : this.geneMap.values()) {
            this.infoMap.put(((InfoGene) gene).iType, (InfoGene) gene);
        }
        if (!this.sequence.endsWith(STOP_CODON)) {
            System.err.println("NO STOP CODON! " + this + "  " + this.sequence);
        }
    }

    public InfoChromosome() {
        super(ChromoType.INFO);
        for (BaseGene gene : this.geneMap.values()) {
            this.infoMap.put(((InfoGene) gene).iType, (InfoGene) gene);
        }
    }

    public void updateGene(InfoGene gene) {
        // THROWS EXCEPTION IF INVALID (should never happen)
        int index = Objects.requireNonNull(this.infoMap.replace(gene.iType, gene)).iType.index;
        this.geneMap.replace(index, gene);
        this.updateSequence();
    }
}
