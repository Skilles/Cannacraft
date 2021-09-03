package com.skilles.cannacraft.util;

import com.skilles.cannacraft.dna.chromosome.InfoChromosome;
import com.skilles.cannacraft.dna.chromosome.TraitChromosome;
import com.skilles.cannacraft.dna.genome.Genome;
import com.skilles.cannacraft.dna.genome.gene.BaseGene;
import com.skilles.cannacraft.dna.genome.gene.InfoGene;
import com.skilles.cannacraft.dna.genome.gene.TraitGene;
import com.skilles.cannacraft.strain.Strain;
import com.skilles.cannacraft.strain.StrainInfo;
import com.skilles.cannacraft.strain.StrainMap;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.skilles.cannacraft.dna.genome.Enums.*;

public class ConvertUtil {

    public static final String STOP_CODON = "TGGGT";
    
    public static TraitGene convertGene(String string) {
        int codeLength = Code.values[0].name().length();
        Phenotype type = Phenotype.get(Code.convert(string.substring(0, codeLength)));
        int strength = Code.convert(string.substring(codeLength, codeLength * 2));
        State state = State.get(Code.convert(string.substring(codeLength * 2, codeLength * 3)));
        return new TraitGene(strength, type, state);
    }
    
    public static TraitChromosome convertChromosome(String string) {
        string = string.replace(" ", "");
        int numGenes = string.length() / TraitGene.GENE_LENGTH;
        TraitGene[] genes = new TraitGene[numGenes];
        for (int i = 0; i < numGenes; i++) {
            String code = string.substring(i * TraitGene.GENE_LENGTH, i * TraitGene.GENE_LENGTH + TraitGene.GENE_LENGTH);
            genes[i] = new TraitGene(code);
        }
        return new TraitChromosome(genes);
    }

    public static StrainInfo convertStrain(Genome genome, boolean identified) {
        InfoChromosome infoChromosome = (InfoChromosome) genome.chromosomeMap.get(ChromoType.INFO);
        int id = infoChromosome.infoMap.get(InfoType.STRAIN).value;
        int typeId = infoChromosome.infoMap.get(InfoType.TYPE).value;
        int thc = infoChromosome.infoMap.get(InfoType.THC).value;
        boolean resource = infoChromosome.infoMap.get(InfoType.RESOURCE).value != 0;
        boolean male = genome.isMale();
        Strain strain = StrainUtil.getStrain(id, resource);
        List<TraitGene> geneList = new ArrayList<>(genome.traitMap.values());
        return new StrainInfo(strain, thc, identified, male, geneList);
    }

    public static Genome getGenome(ItemStack stack) {
        StrainInfo info = WeedRegistry.getStrainInfo(stack);
    }

    private static Genome convertGenome(StrainInfo strainInfo) {
        return convertGenome(strainInfo.strain().id(), strainInfo.strain().type(), strainInfo.thc(), strainInfo.strain().isResource(), , );
    }

    public static Genome convertGenome(int id, StrainMap.Type type, int thc, boolean resource, boolean male, List<TraitGene> traitList) {
        List<BaseGene> geneList = new ArrayList<>(traitList);
        geneList.add(new InfoGene(InfoType.STRAIN, id));
        geneList.add(new InfoGene(InfoType.TYPE, type.ordinal()));
        geneList.add(new InfoGene(InfoType.THC, thc));
        geneList.add(new InfoGene(InfoType.RESOURCE, resource ? 1 : 0));
        return new Genome(male, geneList.toArray(new BaseGene[0]));
    }

    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
        List<Map.Entry<K, V>> list = new ArrayList<>(map.entrySet());
        list.sort(Map.Entry.comparingByValue());

        Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }

}
