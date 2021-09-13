package com.skilles.cannacraft.dna.genome.gene;

import com.skilles.cannacraft.dna.genome.Meiosis;

import static com.skilles.cannacraft.dna.genome.Enums.*;

public class TraitGene extends BaseGene {

    public static final int GENE_LENGTH = BaseGene.GENE_LENGTH + 6;

    public final Phenotype phenotype;

    public final State state;

    public final String genotype;

    public TraitGene(String sequence) {
        super(sequence);
        assert sequence.length() % GENE_LENGTH == 0;
        int codeLength = Code.length;
        this.phenotype = Phenotype.get(Code.convert(sequence.substring(codeLength * 2, codeLength * 3)));
        this.state = State.get(Code.convert(sequence.substring(codeLength * 3, codeLength * 4)));
        this.genotype = getGenotype();
        assert this.sequence.length() == GENE_LENGTH;
    }

    public TraitGene(Phenotype phenotype, int strength, State state) {
        super(GeneType.TRAIT, strength);
        this.phenotype = phenotype;
        this.state = state;
        this.genotype = getGenotype();
        this.sequence = Code.get(this.geneType.index) + Code.get(this.value) + Code.get(this.phenotype.index) + Code.get(this.state.index);
        assert this.sequence.length() == GENE_LENGTH;
    }

    private String getGenotype() {
        return this.state.withChar(this.phenotype.symbol);
    }

    public boolean isExpressed() {
        return this.value > 0  && Meiosis.isExpressed(this.phenotype, this.state);
    }

    @Override
    public String toString() {
        return "(" + this.phenotype + ", " +
                this.value + ", " +

                this.genotype + ")";
    }

    @Override
    public int compareTo(BaseGene o) {
        if (o instanceof TraitGene tGene) {
            return Integer.compare(this.phenotype.index, tGene.phenotype.index);
        }
        return super.compareTo(o);
    }

}
