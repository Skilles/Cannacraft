package com.skilles.cannacraft.dna.chromosome;

import com.skilles.cannacraft.dna.genome.Genome;
import com.skilles.cannacraft.dna.genome.gene.BaseGene;
import com.skilles.cannacraft.dna.genome.gene.InfoGene;
import com.skilles.cannacraft.dna.genome.gene.SexGene;
import com.skilles.cannacraft.dna.genome.gene.TraitGene;
import com.skilles.cannacraft.util.copy.DeepCopy;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.skilles.cannacraft.dna.genome.Enums.*;
import static com.skilles.cannacraft.util.ConvertUtil.STOP_CODON;

public class BaseChromosome implements Comparable<BaseChromosome>, Serializable {

    public String sequence;

    public final ChromoType type;

    public Map<Integer, BaseGene> geneMap = new TreeMap<>();

    public BaseChromosome(String sequence, ChromoType type) {
        sequence = sequence.replace(" ", "");
        this.sequence = sequence;
        this.type = type;
        this.mapGenes();
        String oldSequence = sequence;
        this.updateSequence();
        if (!this.sequence.equals(oldSequence)  && !(this instanceof SexChromosome)) {
            System.err.println("Old sequence changed! " + this);
        }
    }

    public BaseChromosome(ChromoType type) {
        this(type, defaultGenes(type));
    }

    protected BaseChromosome(ChromoType type, BaseGene... genes) {
        this.type = type;
        AtomicInteger i = new AtomicInteger(0);
        this.geneMap = Arrays.stream(genes).collect(Collectors.toMap(gene -> i.getAndIncrement(), gene -> gene, (g1, g2) -> g1, TreeMap::new));
        this.updateSequence();
    }

    public static BaseGene[] defaultGenes(ChromoType type) {
        if (type.equals(ChromoType.SEX1) || type.equals(ChromoType.SEX2)) {
            return new BaseGene[] { new SexGene(Genome.DEFAULT_SEX) };
        } else if (type.equals(ChromoType.INFO)) {
            return new BaseGene[] {
                new InfoGene(InfoType.STRAIN, 0),
                new InfoGene(InfoType.TYPE, 0),
                new InfoGene(InfoType.THC, 0),
                new InfoGene(InfoType.RESOURCE, 0)
            };
        } else {
            return Arrays.stream(Phenotype.values)
                    .filter(geneType -> geneType.chromoType.equals(type))
                    .map(geneType -> new TraitGene(0, geneType, geneType.recessive ? State.DOMINANT : State.RECESSIVE))
                    .toArray(BaseGene[]::new);
        }
    }

    private void mapGenes() {
        this.sequence = sequence.replace(" ", "");
        int length;
        GeneType type;
        if (this instanceof InfoChromosome) {
            length = InfoGene.GENE_LENGTH;
            type = GeneType.INFO;
        } else if (this instanceof SexChromosome) {
            length = SexGene.GENE_LENGTH;
            type = GeneType.SEX;
        } else {
            length = TraitGene.GENE_LENGTH;
            type = GeneType.TRAIT;
        }
        int numGenes = sequence.length() / length;
        for (int i = 0; i < numGenes; i++) {
            String code = sequence.substring(i * length, i * length + length);
            if (type.equals(GeneType.INFO)) {
                this.geneMap.put(i, new InfoGene(code));
            } else if (type.equals(GeneType.SEX)) {
                this.geneMap.put(i, new SexGene(code));
            } else {
                this.geneMap.put(i, new TraitGene(code));
            }
        }
    }



    void checkGenes(BaseGene... genes) {
        for (BaseGene gene : genes) {
            if (gene.geneType.equals(GeneType.INFO) && !this.type.equals(ChromoType.INFO)) {
                throw new IllegalArgumentException("Mismatched gene! Gene: " + gene.geneType + " | Chromosome: " + this.type);
            } else if (gene.geneType.equals(GeneType.SEX) && !this.type.equals(ChromoType.SEX1) && !this.type.equals(ChromoType.SEX2)) {
                throw new IllegalArgumentException("Mismatched gene! Gene: " + gene.geneType + " | Chromosome: " + this.type);
            } else if (!((TraitGene) gene).phenotype.chromoType.equals(this.type)) {
                throw new IllegalArgumentException("Mismatched gene! Gene: " + ((TraitGene) gene).phenotype.chromoType + " | Chromosome: " + this.type);
            }
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.sequence);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BaseChromosome other)) return false;
        return other.sequence.equals(this.sequence);
    }

    public BaseChromosome copy() {
        return (BaseChromosome) DeepCopy.copy(this);
    }

    @Override
    public int compareTo(BaseChromosome o) {
        return this.type.compareTo(o.type);
    }

    @Override
    public String toString() {
        return this.geneMap.values().toString();
    }

    protected void updateSequence() {
        this.sequence = geneMap.values().stream().map(gene -> gene.sequence).collect(Collectors.joining());
        this.sequence += STOP_CODON;
    }

}
