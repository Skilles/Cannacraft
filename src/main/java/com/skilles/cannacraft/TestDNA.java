package com.skilles.cannacraft;

import com.skilles.cannacraft.dna.chromosome.TraitChromosome;
import com.skilles.cannacraft.dna.genome.Genome;
import com.skilles.cannacraft.dna.genome.gene.TraitGene;
import com.skilles.cannacraft.strain.StrainMap;
import com.skilles.cannacraft.util.ConvertUtil;

import java.util.ArrayList;
import java.util.List;

import static com.skilles.cannacraft.dna.genome.Enums.*;

public class TestDNA {

    public static void main(String[] args) {
        ChromoType[] c = ChromoType.values; // INIT CHROMOTYPE ENUM BEFORE GENE REFERENCE
        runTests();
    }

    private static Exception ChromosomeToGenomeTest() {
        try {
            Genome genome = genomeFromGenes();
            if (genome.traitMap.get(Phenotype.FLY).value != 3 || genome.traitMap.get(Phenotype.LUCK).value != 6) {
                throw new Exception("Genome failed to map correct genes!");
            }
        } catch (Exception e) {
            return e;
        }
        return null;
    }

    private static Genome genomeFromGenes() {
        TraitChromosome c1 = new TraitChromosome(ChromoType.PHYSICAL);
        TraitChromosome c2 = new TraitChromosome(new TraitGene(3, Phenotype.FLY, State.DOMINANT), new TraitGene(6, Phenotype.LUCK, State.CARRIER));
        return new Genome(c1, c2);
    }

    private static Exception GeneToGenomeTest() {
        try {
            Genome genome2 = new Genome(false, new TraitGene(3, Phenotype.FLY, State.DOMINANT), new TraitGene(6, Phenotype.LUCK, State.CARRIER));
            System.out.println(genome2);
        } catch (Exception e) {
            return e;
        }
        return null;
    }

    private static Exception StringToGenomeTest() {
        try {
            Genome genome3 = new Genome("AACAAATGGGTAACAAATGGGTAATAAAAAAAATAAAAATAATAAAAACAATAAAAAGTGGGTAAAAAAAAAAATAAAAAAAATAAATGGGTAAAATCAACAACAAAAAAAAGAATAAAAAGATAAATTGGGTAAAAAAATTAATAAAAAAATCAAATGGGT");
        } catch (Exception e) {
            return e;
        }
        return null;
    }

    private static Exception EqualityTest() {
        Genome genome = genomeFromGenes();
        Genome genome2 = new Genome(false, new TraitGene(3, Phenotype.FLY, State.DOMINANT), new TraitGene(6, Phenotype.LUCK, State.CARRIER));
        Genome genome3 = new Genome("AACAAATGGGTAACAAATGGGTAATAAAAAAAATAAAAATAATAAAAACAATAAAAAGTGGGTAAAAAAAAAAATAAAAAAAATAAATGGGTAAAATCAACAACAAAAAAAAGAATAAAAAGATAAATTGGGTAAAAAAATTAATAAAAAAATCAAATGGGT");
        Genome genome4 = ConvertUtil.convertGenome(0, StrainMap.Type.INDICA, 0, false, false, genome.traitMap.values().stream().toList());
        if (!genome.equals(genome2)) {
            System.out.println(genome);
            System.out.println(genome2);
            return new Exception("Chromosome and Gene are unequal");
        } else if (!genome.equals(genome3)) {
            System.out.println(genome);
            System.out.println(genome3);
            return new Exception("Chromosome and String are unequal");
        } else if (!genome2.equals(genome3)) {
            System.out.println(genome2);
            System.out.println(genome3);
            return new Exception("Gene and String are unequal");
        } else if (!genome.equals(genome4)) {
            System.out.println(genome);
            System.out.println(genome4);
            genome.prettyPrint();
            genome.sequencePrint();
            genome4.prettyPrint();
            genome4.sequencePrint();
            return new Exception("Value converted genome is unequal");
        } else {
            return null;
        }
    }

    private static void runTests() {
        Exception test1 = ChromosomeToGenomeTest();
        Exception test2 = GeneToGenomeTest();
        Exception test3 = StringToGenomeTest();
        Exception test4 = EqualityTest();
        List<Exception> exceptionsToPrint = new ArrayList<>();
        if (test1 != null) {
            System.err.println("ChromosomeToGenomeTest failed!");
            exceptionsToPrint.add(test1);
        } else {
            System.out.println("ChromosomeToGenomeTest passed!");
        }
        if (test2 != null) {
            System.err.println("GeneToGenomeTest failed!");
            exceptionsToPrint.add(test2);
        } else {
            System.out.println("GeneToGenomeTest passed!");
        }
        if (test3 != null) {
            System.err.println("StringToGenomeTest failed!");
            exceptionsToPrint.add(test3);
        } else {
            System.out.println("StringToGenomeTest passed!");
        }
        if (test4 != null) {
            System.err.println("EqualityTest failed!");
            exceptionsToPrint.add(test4);
        } else {
            System.out.println("EqualityTest passed!");
        }
        for (Exception e : exceptionsToPrint) {
            e.printStackTrace(System.err);
        }
        if (exceptionsToPrint.isEmpty()) {
            System.out.println("\u001B[32mAll tests passed!");
        }
    }
}
