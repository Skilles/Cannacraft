package com.skilles.cannacraft.dna.genome.gene;

import static com.skilles.cannacraft.dna.genome.Enums.*;

public class InfoGene extends BaseGene {

    public InfoType iType;

    public static final int GENE_LENGTH = BaseGene.GENE_LENGTH + 3;

    public InfoGene(String sequence) {
        super(sequence);
        int codeLength = 3;
        this.iType = InfoType.get(Code.convert(sequence.substring(codeLength * 2, codeLength * 3)));
        assert this.sequence.length() == GENE_LENGTH;
    }

    public InfoGene(InfoType type, int value) {
        super(Code.get(GeneType.INFO.index) + Code.get(value) + Code.get(type.index));
        this.iType = type;
        assert this.sequence.length() == GENE_LENGTH;
    }

    @Override
    public String toString() {
        return "(" + this.iType + ", " +
                this.value + ")";
    }

}
