package com.skilles.cannacraft.dna.genome;

import com.skilles.cannacraft.dna.chromosome.*;
import com.skilles.cannacraft.dna.genome.gene.BaseGene;
import com.skilles.cannacraft.dna.genome.gene.InfoGene;
import com.skilles.cannacraft.dna.genome.gene.TraitGene;
import com.skilles.cannacraft.util.copy.DeepCopy;

import java.util.*;
import java.util.stream.Collectors;

import static com.skilles.cannacraft.dna.genome.Enums.ChromoType;
import static com.skilles.cannacraft.dna.genome.Enums.Phenotype;
import static com.skilles.cannacraft.util.ConvertUtil.STOP_CODON;
import static com.skilles.cannacraft.util.ConvertUtil.convertChromosome;

public class Genome {

    public static final int NUM_CHROMOSOMES = ChromoType.size;

    public static final char DEFAULT_SEX = 'X';


    public final Map<ChromoType, BaseChromosome> chromosomeMap =  new EnumMap<>(ChromoType.class);

    public Map<Phenotype, TraitGene> traitMap = new EnumMap<>(Phenotype.class);

    Diploid sexChromosomes;

    public final int size;

    private final String fullString;

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
            ChromoType type = chromosome.type;
            missingTypes.remove(type);
            if (!type.equals(ChromoType.SEX1) && !type.equals(ChromoType.SEX2) && !type.equals(ChromoType.INFO)) {
                TraitChromosome tChromosome = (TraitChromosome) chromosome;
                tChromosome.fillMissingGenes();
                this.traitMap.putAll(tChromosome.traitMap);
            }
            chromosomeMap.put(type, chromosome);
        }
        for (ChromoType type : missingTypes) {
            if (type.equals(ChromoType.SEX1)) {
                this.chromosomeMap.put(type, new SexChromosome('X', ChromoType.SEX1));
            } else if (type.equals(ChromoType.SEX2)) {
                this.chromosomeMap.put(type, new SexChromosome(DEFAULT_SEX, ChromoType.SEX2));
            } else if (type.equals(ChromoType.INFO)) {
                this.chromosomeMap.put(type, new InfoChromosome());
            } else {
                TraitChromosome chromosome = (TraitChromosome) type.emptyChromosome.copy();
                this.traitMap.putAll(chromosome.traitMap);
                this.chromosomeMap.put(type, chromosome);
            }
        }
    }


    public void prettyPrint() {
        chromosomeMap.forEach((type, c) -> System.out.println(type + ": " + c));
    }

    public void sequencePrint() {
        System.out.println(chromosomeMap.values().stream().map(c -> c.sequence).collect(Collectors.joining(" | ")));
    }

//    public boolean isMale() {
//        return (sexChromosomes.first().isY() && !sexChromosomes.second().isY()) || (sexChromosomes.second().isY() && !sexChromosomes.first().isY());
//    }

    // Called only on mother
    /*public Genome sex(Genome father) {
        Random random = new Random();
        List<Chromosome> output = new ArrayList<>();
        for (int i = 0; i < chromosomes.size(); i++) {
            Chromosome mChromosome = chromosomes.get(i);
            Chromosome fChromosome = father.chromosomes.get(i);
            if (mChromosome.equals(fChromosome)) {
                output.add(mChromosome);
                continue;
            }
            // Chromosome[] combinations = mChromosome.meiosis(fChromosome);
            Chromosome crossed = Meiosis.cross(mChromosome, fChromosome);
            output.add(crossed);
        }
        Genome offspring = new Genome(output.toArray(new Chromosome[0]));
        offspring.sexChromosomes = new Diploid((SexChromosome) this.sexChromosomes.random(), (SexChromosome) father.sexChromosomes.random());
        return offspring;
    }*/

    public static BaseChromosome[] build(boolean male, BaseGene... genes) {
        // Chromosome[] output = new Chromosome[NUM_CHROMOSOMES];
        List<BaseChromosome> output = new ArrayList<>();
        for (ChromoType type : ChromoType.values) {
            BaseChromosome chromosome;
            if (type.equals(ChromoType.SEX1)) {
                chromosome = new SexChromosome('X', ChromoType.SEX1);
            } else if (type.equals(ChromoType.SEX2)) {
                chromosome = new SexChromosome(male ? 'Y' : 'X', ChromoType.SEX2);
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
