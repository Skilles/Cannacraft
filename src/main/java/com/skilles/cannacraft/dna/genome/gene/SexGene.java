package com.skilles.cannacraft.dna.genome.gene;

import com.skilles.cannacraft.dna.genome.Enums;

public class SexGene extends BaseGene {

    public static final int GENE_LENGTH = BaseGene.GENE_LENGTH;

    public char sex;

    public SexGene(String sequence) {
        super(sequence);
        this.sex = this.value == 0 ? 'X' : 'Y';
        assert this.sequence.length() == GENE_LENGTH;
    }

    public SexGene(char sex) {
        super(Enums.GeneType.SEX, sex == 'X' ? 0 : 1);
        this.sex = sex;
        assert this.sequence.length() == GENE_LENGTH;
    }

}
