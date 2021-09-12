package com.skilles.cannacraft.dna.genome.gene;

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

    public TraitGene(int strength, Phenotype phenotype, State state) {
        super(GeneType.TRAIT, strength);
        this.phenotype = phenotype;
        this.state = state;
        this.genotype = getGenotype();
        this.sequence = Code.get(this.geneType.index) + Code.get(this.value) + Code.get(this.phenotype.index) + Code.get(this.state.index);
        assert this.sequence.length() == GENE_LENGTH;
    }

    private String getGenotype() {

        char upper = this.phenotype.symbol;
        char lower = Character.toLowerCase(upper);

        StringBuilder output = new StringBuilder();

        switch (this.state) {
            case RECESSIVE ->  {
                output.append(lower);
                output.append(lower);
            }
            case DOMINANT -> {
                output.append(upper);
                output.append(upper);
            }
            case CARRIER -> {
                output.append(upper);
                output.append(lower);
            }
        }
        return output.toString();
    }

    public static boolean isExpressed(TraitGene gene, boolean male) {
        return gene.phenotype.recessive == gene.state.equals(State.RECESSIVE);
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
