package com.skilles.cannacraft.dna.genome.gene;

import java.io.Serializable;

import static com.skilles.cannacraft.dna.genome.Enums.Code;
import static com.skilles.cannacraft.dna.genome.Enums.GeneType;

public class BaseGene implements Comparable<BaseGene>, Serializable {

    public static final int GENE_LENGTH = 6;

    public String sequence;

    public final int value;

    public final GeneType geneType;

    public BaseGene(String sequence) {
        this.sequence = sequence;
        int codeLength = 3;
        this.geneType = GeneType.get(Code.convert(sequence.substring(0, codeLength)));
        this.value = Code.convert(sequence.substring(codeLength, codeLength * 2));
    }

    public BaseGene(GeneType geneType, int value) {
        this.sequence = Code.get(geneType.index) + Code.get(value);
        this.value = value;
        this.geneType = geneType;
    }

    @Override
    public int compareTo(BaseGene o) {
        return this.sequence.compareTo(o.sequence);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TraitGene other)) return false;
        return other.sequence.equals(this.sequence);
    }

    @Override
    public String toString() {
        return "(" + this.geneType + ", " +
                this.value + ")";
    }

}
