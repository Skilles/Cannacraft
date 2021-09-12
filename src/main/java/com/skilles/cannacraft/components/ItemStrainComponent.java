package com.skilles.cannacraft.components;

import com.skilles.cannacraft.dna.chromosome.InfoChromosome;
import com.skilles.cannacraft.dna.genome.Genome;
import com.skilles.cannacraft.dna.genome.gene.InfoGene;
import com.skilles.cannacraft.dna.genome.gene.TraitGene;
import com.skilles.cannacraft.strain.StrainInfo;
import com.skilles.cannacraft.util.BundleUtil;
import com.skilles.cannacraft.util.DnaUtil;
import dev.onyxstudios.cca.api.v3.component.CopyableComponent;
import dev.onyxstudios.cca.api.v3.item.ItemComponent;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

import static com.skilles.cannacraft.dna.genome.Enums.ChromoType;
import static com.skilles.cannacraft.dna.genome.Enums.InfoType;


public class ItemStrainComponent extends ItemComponent implements StrainInterface, CopyableComponent<ItemStrainComponent> {

    private Genome cachedGenome;

    private boolean genomeInit;

    private int cachedThc = -1;

    private StrainInfo cachedStrainInfo;

    private List<TraitGene> cachedGenes;

    public ItemStrainComponent(ItemStack stack) {
        super(stack);
    }

    @Override
    public void setStrain(int index) {
        this.getGenome().updateGene(new InfoGene(InfoType.STRAIN, index), true);
        this.onTagInvalidated();
    }

    @Override
    public void setTraits(List<TraitGene> geneList) {
        for (TraitGene gene : geneList) {
            this.getGenome().updateGene(gene, false);
        }
        this.getGenome().update();
        this.onTagInvalidated();
    }

    @Override
    public void setThc(int thc) {
        this.getGenome().updateGene(new InfoGene(InfoType.THC, thc), true);
        this.onTagInvalidated();
    }

    @Override
    public void setMale(boolean male) {
        this.getGenome().setMale(male);
        this.onTagInvalidated();
    }

    @Override
    public void addGene(TraitGene gene) {
        this.getGenome().updateGene(gene, true);
        this.onTagInvalidated();
    }

    @Override
    public boolean hasGenes() {
        return this.getTraits().isEmpty();
    }

    @Override
    public StrainInfo getStrainInfo() {
        if (!this.genomeInit || this.cachedStrainInfo == null) {
            this.cachedStrainInfo = DnaUtil.convertStrain(this.getGenome(), this.identified());
        }
        return this.cachedStrainInfo;
    }

    @Override
    public List<TraitGene> getTraits() {
        if (!this.genomeInit || this.cachedGenes == null) {
            this.cachedGenes = new ArrayList<>(this.getGenome().traitMap.values());
        }
        return this.cachedGenes;
    }

    @Override
    public int getThc() {
        if (!this.genomeInit || this.cachedThc == -1) {
            this.cachedThc = ((InfoChromosome) this.getGenome().chromosomeMap.get(ChromoType.INFO)).infoMap.get(InfoType.THC).value;
        }
        return this.cachedThc;
    }

    @Override
    public boolean isMale() {
        return this.getStrainInfo().male();
    }

    @Override
    public TriState getStatus() {
        if (!this.hasTag("Status")) {
            this.putFloat("Status", 0.0F);
        }
        return BundleUtil.convertStatus(this.getFloat("Status"));
    }

    @Override
    public void setStatus(TriState status) {
        this.putFloat("Status", BundleUtil.convertStatus(status));
    }

    @Override
    public void identify() {
        if (!this.identified()) {
            this.cachedStrainInfo = this.cachedStrainInfo.asIdentified();
        }
    }

    @Override
    public boolean identified() {
        if (this.cachedStrainInfo == null) {
            return false;
        }
        return this.getStrainInfo().identified();
    }

    @Override
    public Genome getGenome() {
        if (!this.genomeInit) {
            this.cachedGenome = DnaUtil.getGenome(this.stack);
            this.genomeInit = true;
            this.putString("DNA", cachedGenome.toString());
        }
        return this.cachedGenome;
    }

    @Override
    public void copyFrom(ItemStrainComponent other) {
    }

    @Override
    public void onTagInvalidated() {
        super.onTagInvalidated();
        this.genomeInit = false;
    }
}