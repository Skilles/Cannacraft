package com.skilles.cannacraft.dna.chromosome;

import com.skilles.cannacraft.dna.genome.gene.SexGene;

import static com.skilles.cannacraft.dna.genome.Enums.ChromoType;
import static com.skilles.cannacraft.dna.genome.Genome.DEFAULT_SEX;
import static com.skilles.cannacraft.util.DnaUtil.STOP_CODON;

public class SexChromosome extends BaseChromosome {

    public char sex;

    public SexChromosome(ChromoType type, char sex) {
        super("", type);
        this.sex = sex;
        this.geneMap.put(0, new SexGene(sex));
        this.updateSequence();
    }

    public SexChromosome(ChromoType type) {
        super("", type);
        this.sex = DEFAULT_SEX;
        this.geneMap.put(0, new SexGene(sex));
        this.updateSequence();
        if (!this.sequence.endsWith(STOP_CODON)) {
            System.err.println("NO STOP CODON! " + this + "  " + this.sequence);
        }
    }

    public SexChromosome(String sequence, ChromoType type) {
        super(sequence, type);
        this.sex = this.geneMap.get(0).value == 1 ? 'Y' : 'X';
    }

    public boolean isY() {
        return this.sex == 'Y';
    }

    public static String getSequence(char sex) {
        return sex == 'X' ? "AAA" : "AAT";
    }

    public void setMale(boolean male) {
        this.sex = male ? 'Y' : 'X';
        this.geneMap.put(0, new SexGene(this.sex));
        this.updateSequence();
    }

}
