package com.skilles.cannacraft.dna.genome;

import com.skilles.cannacraft.dna.chromosome.*;
import com.skilles.cannacraft.dna.genome.gene.BaseGene;
import com.skilles.cannacraft.dna.genome.gene.InfoGene;
import com.skilles.cannacraft.dna.genome.gene.TraitGene;
import com.skilles.cannacraft.util.copy.DeepCopy;

import java.util.*;
import java.util.stream.Collectors;

import static com.skilles.cannacraft.dna.genome.Enums.*;
import static com.skilles.cannacraft.util.DnaUtil.STOP_CODON;
import static com.skilles.cannacraft.util.DnaUtil.convertChromosome;

public class Genome {

    public static final int NUM_CHROMOSOMES = ChromoType.size;

    public static final char DEFAULT_SEX = 'X';

    public final Map<ChromoType, BaseChromosome> chromosomeMap =  new EnumMap<>(ChromoType.class);

    public Map<Phenotype, TraitGene> traitMap = new EnumMap<>(Phenotype.class);

    public Map<InfoType, InfoGene> infoMap = new EnumMap<>(InfoType.class);

    Diploid sexChromosomes;

    public final int size;

    private String fullString;

    // Create a genome that expresses the following genes
    public Genome(boolean male, BaseGene... genes) {
        this(build(male, genes));
    }

    public Genome(BaseChromosome... chromosomes) {
        assert chromosomes.length == NUM_CHROMOSOMES;
        this.mapChromosomes(chromosomes);
        this.sexChromosomes = new Diploid((SexChromosome) this.chromosomeMap.get(ChromoType.SEX1), (SexChromosome) this.chromosomeMap.get(ChromoType.SEX2));
        this.size = this.chromosomeMap.size();
        this.fullString = this.chromosomeMap.values().stream().map(c -> c.sequence).collect(Collectors.joining());
    }

    public Genome(String sequence) {
        this(sequenceMap(sequence));
    }

    private static void checkChromosomes(BaseChromosome... chromosomes) {
        Set<ChromoType> typeSet = new HashSet<>();
        for (ChromoType type : ChromoType.values) {
            if (!typeSet.add(type)) {
                throw new IllegalArgumentException("Duplicate chromosome types! " + type + " | " + Arrays.toString(chromosomes));
            }
        }
    }

    private void updateSequence() {
        this.chromosomeMap.values().forEach(BaseChromosome::updateSequence);
        this.fullString = this.chromosomeMap.values().stream().map(c -> c.sequence).collect(Collectors.joining());
    }

    private void updateMaps() {
        this.traitMap.clear();
        this.infoMap.clear();
        for (BaseChromosome chromosome : this.chromosomeMap.values()) {
            GeneType type = chromosome.geneMap.get(0).geneType;
            if (type.equals(GeneType.INFO)) {
                InfoChromosome iChromosome = (InfoChromosome) chromosome;
                this.infoMap.putAll(iChromosome.infoMap);
            } else if (type.equals(GeneType.TRAIT)) {
                TraitChromosome tChromosome = (TraitChromosome) chromosome;
                // tChromosome.fillMissingGenes();
                this.traitMap.putAll(tChromosome.traitMap);
            }
        }
    }

    public void update() {
        this.updateMaps();
        this.updateSequence();
    }

    private static BaseChromosome[] sequenceMap(String string) {
        List<BaseChromosome> chromosomes = stringToChromosomes(string);
        return chromosomes.toArray(new BaseChromosome[0]);
    }

    public static List<BaseChromosome> stringToChromosomes(String string) {
        List<BaseChromosome> chromosomes = new ArrayList<>();
        String[] split = string.split(STOP_CODON);
        for (int i = 0; i < split.length; i++) {
            String chromoStr = split[i];
            BaseChromosome chromosome;
            if (i == 0) {
                chromosome = new SexChromosome(chromoStr, ChromoType.SEX1); // 3 - 6
            } else if (i == 1) {
                chromosome = new SexChromosome(chromoStr, ChromoType.SEX2); // 9 - 12
            } else if (i == 2) {
                chromosome = new InfoChromosome(chromoStr); // 12 - 21
            } else {
                chromosome = convertChromosome(chromoStr);
            }
            chromosomes.add(chromosome);
        }
        return chromosomes;
    }

    private void mapChromosomes(BaseChromosome... chromosomes) {
        checkChromosomes(chromosomes);
        List<ChromoType> missingTypes = new ArrayList<>(List.of(ChromoType.values));
        for (BaseChromosome chromosome : chromosomes) {
            GeneType type = chromosome.geneMap.get(0).geneType;
            missingTypes.remove(chromosome.type);
            if (type.equals(GeneType.INFO)) {
                InfoChromosome iChromosome = (InfoChromosome) chromosome;
                this.infoMap.putAll(iChromosome.infoMap);
            } else if (type.equals(GeneType.TRAIT)) {
                TraitChromosome tChromosome = (TraitChromosome) chromosome;
                tChromosome.fillMissingGenes();
                this.traitMap.putAll(tChromosome.traitMap);
            }
            chromosomeMap.put(chromosome.type, chromosome);
        }
        for (ChromoType type : missingTypes) {
            if (type.equals(ChromoType.SEX1)) {
                this.chromosomeMap.put(type, new SexChromosome(ChromoType.SEX1, 'X'));
            } else if (type.equals(ChromoType.SEX2)) {
                this.chromosomeMap.put(type, new SexChromosome(ChromoType.SEX2, DEFAULT_SEX));
            } else if (type.equals(ChromoType.INFO)) {
                this.chromosomeMap.put(type, new InfoChromosome());
            } else {
                TraitChromosome chromosome = (TraitChromosome) type.emptyChromosome.copy();
                this.traitMap.putAll(chromosome.traitMap);
                this.chromosomeMap.put(type, chromosome);
            }
        }
    }


    public String prettyPrint() {
        return chromosomeMap.entrySet().stream().map(entry -> entry.getKey().toString() + ": " + entry.getValue() + "\n").collect(Collectors.joining());
        // chromosomeMap.forEach((type, c) -> System.out.println(type + ": " + c));
    }

    public void sequencePrint() {
        System.out.println(chromosomeMap.values().stream().map(c -> c.sequence).collect(Collectors.joining(" | ")));
    }

    public void updateGene(TraitGene gene, boolean update) {
        ((TraitChromosome) this.chromosomeMap.get(gene.phenotype.chromoType)).updateGene(gene);
        if (update) {
            this.update();
        }
    }

    public void updateGene(InfoGene gene, boolean update) {
        ((InfoChromosome) this.chromosomeMap.get(ChromoType.INFO)).updateGene(gene);
        if (update) {
            this.update();
        }
    }

    public void setMale(boolean male) {
        this.sexChromosomes.second().setMale(male);
        this.chromosomeMap.replace(ChromoType.SEX2, this.sexChromosomes.second());
        this.updateSequence();
    }

    public static BaseChromosome[] build(boolean male, BaseGene... genes) {
        // Chromosome[] output = new Chromosome[NUM_CHROMOSOMES];
        List<BaseChromosome> output = new ArrayList<>();
        for (ChromoType type : ChromoType.values) {
            BaseChromosome chromosome;
            if (type.equals(ChromoType.SEX1)) {
                chromosome = new SexChromosome(ChromoType.SEX1, 'X');
            } else if (type.equals(ChromoType.SEX2)) {
                chromosome = new SexChromosome(ChromoType.SEX2, male ? 'Y' : 'X');
            } else if (type.equals(ChromoType.INFO)) {
                chromosome = new InfoChromosome();
            } else {
                chromosome = type.emptyChromosome.copy();
                // ((TraitChromosome) chromosome).fillMissingGenes();
            }
            for (BaseGene gene : genes) {
                // Updates every info and trait chromosome's genes
                if (gene instanceof TraitGene tGene
                        && tGene.phenotype.chromoType.equals(chromosome.type)
                        && chromosome instanceof TraitChromosome tChromosome) {
                    tChromosome.updateGene(tGene);
                } else if (gene instanceof InfoGene iGene && chromosome instanceof InfoChromosome iChromosome) {
                    iChromosome.updateGene(iGene);
                }
            }
            output.add(chromosome);
        }
        return output.toArray(new BaseChromosome[0]);
    }

    public boolean isMale() {
        return this.sexChromosomes.isMale();
    }

    @Override
    public String toString() {
        return this.fullString;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.fullString);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Genome other)) return false;
        return other.fullString.equals(this.fullString);
    }

    protected Genome copy() {
        return (Genome) DeepCopy.copy(this);
    }

}
