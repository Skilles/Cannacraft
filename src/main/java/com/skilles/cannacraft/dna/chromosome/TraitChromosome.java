package com.skilles.cannacraft.dna.chromosome;

import com.skilles.cannacraft.dna.genome.gene.BaseGene;
import com.skilles.cannacraft.dna.genome.gene.TraitGene;
import com.skilles.cannacraft.util.ConvertUtil;

import java.util.EnumMap;
import java.util.Map;

import static com.skilles.cannacraft.dna.genome.Enums.*;
import static com.skilles.cannacraft.util.ConvertUtil.STOP_CODON;

// TODO: transfer and separate methods into base chromosome

public class TraitChromosome extends BaseChromosome {

    public Map<Phenotype, TraitGene> traitMap = new EnumMap<>(Phenotype.class);

    // TODO: convert gene list to type map
    public TraitChromosome(String sequence, ChromoType type) {
        super(sequence, type);
        this.updateGenes();

        for (BaseGene gene : this.geneMap.values()) {
            this.traitMap.putIfAbsent(((TraitGene) gene).phenotype, (TraitGene) gene);
        }
        // Collectors.toMap(gene -> ((TraitGene) gene).phenotype, gene -> gene, TreeMap::new)
        assert sequence.length() / TraitGene.GENE_LENGTH == this.traitMap.size();
        assert sequence.endsWith(STOP_CODON);
    }

    public TraitChromosome(ChromoType type) {
        this(defaultGenes(type));
    }

    public TraitChromosome(BaseGene... genes) {
        super(((TraitGene) genes[0]).phenotype.chromoType, genes);
        this.mapGenes(genes);
        assert sequence.length() / TraitGene.GENE_LENGTH == genes.length;
        if (!this.sequence.endsWith(STOP_CODON)) {
            System.err.println("NO STOP CODON! " + this + "  " + this.sequence);
        }
    }

    private void updateGenes() {
        for (int i = 0; i < sequence.length() / TraitGene.GENE_LENGTH; i += TraitGene.GENE_LENGTH) {
            String code = sequence.substring(i, i + TraitGene.GENE_LENGTH);
            this.geneMap.put(i / TraitGene.GENE_LENGTH, new TraitGene(code));
        }
    }

    public void mapGenes(BaseGene... genes) {
        this.checkGenes(genes);
        for (int i = 0, genesLength = genes.length; i < genesLength; i++) {
            TraitGene gene = (TraitGene) genes[i];
            this.traitMap.put(gene.phenotype, gene);
            this.geneMap.put(i, gene);
        }
        this.fillMissingGenes();
        this.updateSequence();
    }

    public void fillMissingGenes() {
        for (Phenotype phenotype : Phenotype.values) {
            if (traitMap.containsKey(phenotype) || !phenotype.chromoType.equals(this.type)) continue;
            TraitGene gene = new TraitGene(0, phenotype, phenotype.recessive ? State.DOMINANT : State.RECESSIVE);
            this.traitMap.put(phenotype, gene);
            this.geneMap.put(phenotype.index, gene);
        }
        this.geneMap = ConvertUtil.sortByValue(this.geneMap);
        this.updateSequence();
    }

    public void updateGene(TraitGene gene) {
        // THROWS EXCEPTION IF INVALID (should never happen)
        int index = -1;
        int i = 0;
        for (TraitGene tGene : this.traitMap.values()) {
            if (tGene.phenotype.equals(gene.phenotype)) {
                index = i;
            }
            i++;
        }
        this.traitMap.replace(gene.phenotype, gene);
        this.geneMap.replace(index, gene);
        this.updateSequence();
    }

}
