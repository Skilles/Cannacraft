package com.skilles.cannacraft.dna.genome;

import com.skilles.cannacraft.dna.chromosome.TraitChromosome;
import com.skilles.cannacraft.dna.genome.gene.BaseGene;
import com.skilles.cannacraft.dna.genome.gene.InfoGene;
import com.skilles.cannacraft.dna.genome.gene.TraitGene;
import com.skilles.cannacraft.strain.Strain;
import com.skilles.cannacraft.strain.StrainInfo;
import com.skilles.cannacraft.util.CrossUtil;
import com.skilles.cannacraft.util.DnaUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.skilles.cannacraft.dna.genome.Enums.*;

public class Meiosis {

    private static final Random random = new Random();

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
        State crossedState = crossState(gene1.state, gene2.state);
        return new TraitGene(gene1.phenotype, crossStrength(gene1, gene2, crossedState), crossedState);
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

    private static int crossStrength(TraitGene gene1, TraitGene gene2, State crossedState) {
        // TODO: add more probability, mutations

        int strength1 = gene1.value;
        int strength2 = gene2.value;

        int maxValue = Integer.max(strength1, strength2);
        int minValue = Integer.min(strength1, strength2);

        // Checks if gene should be expressed
        if (isExpressed(gene1.phenotype, crossedState)) {

            // If both genes are unexpressed keep them unexpressed
            if (strength1 == 0 && strength2 == 0) {
                return 0;
            }
            // If one gene is unexpressed have a 50% chance of expressing that gene
            if (strength1 == 0 || strength2 == 0) {
                return random.nextInt(2) == 0 ? maxValue : 0;
            }
            // Otherwise, returns average of that gene with a 50% chance of a +1 bonus
            return roundedAverage(strength1, strength2) + (random.nextInt(2) == 0 ? 1 : 0);
        }
        // Returns the average rounded up if unexpressed
        return roundedAverage(strength1, strength2);
    }

    public static boolean isExpressed(Phenotype phenotype, State state) {
        if (phenotype.recessive) {
            return state == State.RECESSIVE;
        } else {
            return state == State.DOMINANT || state == State.CARRIER;
        }
    }

    private static int roundedAverage(int num1, int num2) {
        return (int) Math.round((((double) num1) + ((double) num2)) / 2);
    }

    /** Takes two genomes and crosses their genes together, generating a number of
     *  resulting genomes that can be used as the genomes for the mother's seeds.
     * @param mother a female genome
     * @param father a male genome
     * @param amount how many resulting genomes to generate
     * @param register whether to register the resulting strain
     * @return a list of genomes as a result of breeding the mother and father's traits
     */
    public static List<Genome> crossGenome(Genome mother, Genome father, int amount, boolean register) {
        StrainInfo motherInfo = DnaUtil.convertStrain(mother, true);
        StrainInfo fatherInfo = DnaUtil.convertStrain(father, true);
        List<TraitGene> motherGenes = motherInfo.geneList();
        List<TraitGene> fatherGenes = fatherInfo.geneList();

        assert motherInfo.strain().isResource() == fatherInfo.strain().isResource();

        List<Genome> newGenomes = new ArrayList<>();
        Strain crossedStrain = CrossUtil.crossStrains(motherInfo.strain(), fatherInfo.strain(), register);
        int[] crossedThcs = CrossUtil.multiCrossThc(motherInfo.thc(), fatherInfo.thc(), amount);
        int maxGenes = motherGenes.size();
        for (int j = 0; j < amount; j++) {
            List<BaseGene> seedGenes = new ArrayList<>();
            for (int i = 0; i < maxGenes; i++) {
                TraitGene gene1 = motherGenes.get(i);
                TraitGene gene2 = fatherGenes.get(i);
                seedGenes.add(crossGene(gene1, gene2));
            }
            seedGenes.add(new InfoGene(InfoType.STRAIN, crossedStrain.id()));
            seedGenes.add(new InfoGene(InfoType.THC, crossedThcs[j]));
            seedGenes.add(new InfoGene(InfoType.RESOURCE, crossedStrain.isResource() ? 1 : 0));
            newGenomes.add(new Genome(random.nextInt(2) == 1, seedGenes.toArray(new BaseGene[0])));
        }
        return newGenomes;
    }
}
