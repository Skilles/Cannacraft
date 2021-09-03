package com.skilles.cannacraft.dna.genome;

import com.skilles.cannacraft.dna.chromosome.TraitChromosome;
import com.skilles.cannacraft.dna.genome.gene.TraitGene;
import com.skilles.cannacraft.strain.StrainInfo;
import com.skilles.cannacraft.util.ConvertUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.skilles.cannacraft.dna.genome.Enums.Phenotype;
import static com.skilles.cannacraft.dna.genome.Enums.State;

public class Meiosis {

    private static final Random random = new Random();
    private static final int SEED_COUNT = 4;

    // Cross two chromosomes of the same type
    public static TraitChromosome cross(TraitChromosome mother, TraitChromosome father) {
        assert mother.type.equals(father.type);
        TraitGene[] geneList = new TraitGene[Phenotype.values.length];
        int i = 0;
        for (Phenotype type : Phenotype.values) {
            TraitGene motherGene = mother.traitMap.get(type);
            TraitGene fatherGene = father.traitMap.get(type);

            geneList[i] = crossGene(motherGene, fatherGene);
            i++;
        }
        return new TraitChromosome(geneList);
    }

    // Cross two genes of the same type
    // TODO: mutations
    private static TraitGene crossGene(TraitGene gene1, TraitGene gene2) {
        assert gene1.phenotype.equals(gene2.phenotype);
        return new TraitGene(crossStrength(gene1.value, gene2.value), gene1.phenotype, crossState(gene1.state, gene2.state));
    }

    private static State crossState(State state1, State state2) {
        if ((state1.equals(State.DOMINANT) && state2.equals(State.DOMINANT))) { // BOTH DOMINANT
            return State.DOMINANT; // 100% DOMINANT
        } else if ((state1.equals(State.RECESSIVE) && state2.equals(State.DOMINANT))
                || (state2.equals(State.RECESSIVE) && state1.equals(State.DOMINANT))) { // ONE DOMINANT ONE RECESSIVE
            return State.CARRIER; // 100% CARRIER
        } else if (state1.equals(State.RECESSIVE) && state2.equals(State.RECESSIVE)) { // BOTH RECESSIVE
            return State.RECESSIVE; // 100% RECESSIVE
        } else if ((state1.equals(State.CARRIER) && state2.equals(State.DOMINANT))
                || (state2.equals(State.CARRIER) && state1.equals(State.DOMINANT))) { // ONE DOMINANT ONE CARRIER
            return random.nextInt(2) == 1 ? State.DOMINANT : State.CARRIER; // 50% DOMINANT 50% CARRIER
        } else if ((state1.equals(State.CARRIER) && state2.equals(State.RECESSIVE))
                || (state2.equals(State.CARRIER) && state1.equals(State.RECESSIVE))) { // ONE RECESSIVE ONE CARRIER
            return random.nextInt(2) == 1 ? State.RECESSIVE : State.CARRIER; // 50% RECESSIVE 50% CARRIER
        } /*else if ((state1.equals(State.CARRIER) && state2.equals(State.CARRIER))) { // BOTH CARRIER
            return random.nextInt(2) == 1 ? State.CARRIER : 
                    random.nextInt(2) == 1 ? State.DOMINANT : State.RECESSIVE; // 50% CARRIER 25% DOMINANT 25% RECESSIVE
        }*/
        return random.nextInt(2) == 1 ? State.CARRIER :
                random.nextInt(2) == 1 ? State.DOMINANT : State.RECESSIVE;
    }

    private static int crossStrength(int strength1, int strength2) {
        // TODO: add more probability
        if (strength1 == 0 && strength2 == 0) return 0;
        if (strength1 == 0 || strength2 == 0) return Integer.max(strength1, strength2) - 1;
        return (strength1 + strength2) / 2;
    }

    /** Takes two genomes and crosses their traits together, generating a number of
     *  resulting genomes that can be used as the genomes for the mother's seeds.
     * @param mother a female genome
     * @param father a male genome
     * @return a list of genomes as a result of breeding the mother and father's traits
     */
    public static List<Genome> crossGenome(Genome mother, Genome father) {
        StrainInfo motherInfo = ConvertUtil.convertStrain(mother, true);
        StrainInfo fatherInfo = ConvertUtil.convertStrain(father, true);
        List<TraitGene> motherGenes = motherInfo.geneList();
        List<TraitGene> fatherGenes = fatherInfo.geneList();
        List<Genome> newGenomes = new ArrayList<>();
        for (int j = 0; j < SEED_COUNT; j++) {
            TraitGene[] seedGenes = new TraitGene[Phenotype.size];
            for (int i = 0; i < motherGenes.size(); i++) {
                TraitGene gene1 = motherGenes.get(i);
                TraitGene gene2 = fatherGenes.get(i);
                seedGenes[i] = crossGene(gene1, gene2);
            }
            newGenomes.add(new Genome(random.nextInt(2) == 1, seedGenes));
        }
        return newGenomes;
    }
}
