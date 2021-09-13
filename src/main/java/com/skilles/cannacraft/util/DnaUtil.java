package com.skilles.cannacraft.util;

import com.skilles.cannacraft.blocks.weedCrop.WeedCropEntity;
import com.skilles.cannacraft.dna.chromosome.InfoChromosome;
import com.skilles.cannacraft.dna.chromosome.TraitChromosome;
import com.skilles.cannacraft.dna.genome.Enums;
import com.skilles.cannacraft.dna.genome.Genome;
import com.skilles.cannacraft.dna.genome.Meiosis;
import com.skilles.cannacraft.dna.genome.gene.BaseGene;
import com.skilles.cannacraft.dna.genome.gene.InfoGene;
import com.skilles.cannacraft.dna.genome.gene.TraitGene;
import com.skilles.cannacraft.strain.Strain;
import com.skilles.cannacraft.strain.StrainInfo;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.util.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

import static com.skilles.cannacraft.dna.genome.Enums.*;
import static com.skilles.cannacraft.util.WeedRegistry.*;

public class DnaUtil {

    public static final String STOP_CODON = "TGGGT";
    
    public static TraitGene convertGene(String string) {
        int codeLength = Code.length;
        Phenotype type = Phenotype.get(Code.convert(string.substring(0, codeLength)));
        int strength = Code.convert(string.substring(codeLength, codeLength * 2));
        State state = State.get(Code.convert(string.substring(codeLength * 2, codeLength * 3)));
        return new TraitGene(type, strength, state);
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
        int thc = infoChromosome.infoMap.get(InfoType.THC).value;
        boolean resource = infoChromosome.infoMap.get(InfoType.RESOURCE).value != 0;
        boolean male = genome.isMale();
        Strain strain = StrainUtil.getStrain(id, resource);
        List<TraitGene> geneList = new ArrayList<>(genome.traitMap.values());
        return new StrainInfo(strain, thc, identified, male, geneList);
    }

    public static Genome getGenome(ItemStack stack) {
        NbtCompound strainTag = getStrainTag(stack);
        return new Genome(strainTag.getString("DNA"));
    }

    public static Genome getGenome(WeedCropEntity weedEntity) {
        NbtCompound strainTag = getStrainTag(weedEntity);
        return new Genome(strainTag.getString("DNA"));
    }

    public static Genome getGenome(NbtCompound baseTag) {
        NbtCompound strainTag = baseTag.getCompound("cannacraft:strain");
        return new Genome(strainTag.getString("DNA"));
    }

    private static Genome convertGenome(StrainInfo strainInfo) {
        return convertGenome(strainInfo.strain().id(), strainInfo.thc(), strainInfo.strain().isResource(), strainInfo.male(), strainInfo.geneList());
    }

    public static Genome convertGenome(int id, int thc, boolean resource, boolean male, List<TraitGene> traitList) {
        List<BaseGene> geneList = new ArrayList<>(traitList);
        geneList.add(new InfoGene(InfoType.STRAIN, id));
        geneList.add(new InfoGene(InfoType.THC, thc));
        geneList.add(new InfoGene(InfoType.RESOURCE, resource ? 1 : 0));
        return new Genome(male, geneList.toArray(new BaseGene[0]));
    }

    public static NbtCompound generateNbt(Genome genome, boolean identified, @Nullable StatusTypes status) {
        NbtCompound baseTag = new NbtCompound();
        NbtCompound strainTag = new NbtCompound();
        strainTag.putString("DNA", genome.toString());
        if (status != null) {
            strainTag.putFloat("Status", status.value());
        }
        strainTag.putBoolean("Identified", identified);;
        baseTag.put("cannacraft:strain", strainTag);
        return baseTag;
    }

    public static ItemStack genomeToItem(Genome genome, WeedTypes type, StatusTypes status, boolean identified) {
        ItemStack itemStack = type.item().getDefaultStack();
        itemStack.setNbt(DnaUtil.generateNbt(genome, identified, status));

        return itemStack;
    }

    public static List<ItemStack> crossItems(ItemStack stack1, ItemStack stack2, int amount, boolean register) {
        assert stack1.isItemEqual(stack2);

        List<Genome> genomes = Meiosis.crossGenome(getGenome(stack1), getGenome(stack2), amount, register);
        List<ItemStack> output = new ArrayList<>();

        StatusTypes status = WeedTypes.fromStack(stack1).equals(WeedTypes.BUNDLE) ? getStatus(stack1) : null;
        boolean identified = isIdentified(stack1) && isIdentified(stack2);

        for (Genome genome : genomes) {
            output.add(genomeToItem(genome, WeedTypes.fromStack(stack1), status, identified));
        }
        return output;
    }

    public static Genome randomGenome() {
        StrainInfo randInfo = randomStrainInfo(true, false);
        List<TraitGene> randomGenes = new ArrayList<>();
        Random random = new Random();
        int MAX_GENES = 4;
        int j = 2;
        for (int i = 0; i < MAX_GENES; i++) {
            if (random.nextInt(j) == 0) {
                Phenotype randType = new EnumeratedDistribution<>(Arrays.stream(Enums.Phenotype.values())
                        .map(g -> new Pair<>(g, (double) g.rarity.getWeight())).collect(Collectors.toList()))
                        .sample();
                int randStrength = StrainUtil.normalDist(1, 1, 1);
                randomGenes.add(new TraitGene(randType, randStrength, randType.recessive ? State.RECESSIVE : State.DOMINANT));
                j++;
            }
        }
        return convertGenome(randInfo.withGenes(randomGenes));
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
