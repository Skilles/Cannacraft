package com.skilles.cannacraft.components;

import com.skilles.cannacraft.dna.chromosome.InfoChromosome;
import com.skilles.cannacraft.dna.genome.Genome;
import com.skilles.cannacraft.dna.genome.gene.InfoGene;
import com.skilles.cannacraft.dna.genome.gene.TraitGene;
import com.skilles.cannacraft.strain.StrainInfo;
import com.skilles.cannacraft.util.DnaUtil;
import dev.onyxstudios.cca.api.v3.component.CopyableComponent;
import dev.onyxstudios.cca.api.v3.item.ItemComponent;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

import static com.skilles.cannacraft.dna.genome.Enums.ChromoType;
import static com.skilles.cannacraft.dna.genome.Enums.InfoType;
import static com.skilles.cannacraft.util.WeedRegistry.StatusTypes;


public class ItemStrainComponent extends ItemComponent implements StrainInterface, CopyableComponent<ItemStrainComponent> {

    private Genome cachedGenome;

    private boolean genomeInit;

    private int cachedThc = -1;

    private StrainInfo cachedStrainInfo;

    private List<TraitGene> cachedGenes;

    private List<TraitGene> expressedGenes;

    public ItemStrainComponent(ItemStack stack) {
        super(stack);
    }

    @Override
    public void setStrain(int index) {
        this.getGenome().updateGene(new InfoGene(InfoType.STRAIN, index), true);
        update();
    }

    @Override
    public void setTraits(List<TraitGene> geneList) {
        for (TraitGene gene : geneList) {
            this.getGenome().updateGene(gene, false);
        }
        this.getGenome().update();
        update();
    }

    @Override
    public void setThc(int thc) {
        this.getGenome().updateGene(new InfoGene(InfoType.THC, thc), true);
        update();
    }

    @Override
    public void setMale(boolean male) {
        this.getGenome().setMale(male);
        update();
    }

    @Override
    public void addGene(TraitGene gene) {
        this.getGenome().updateGene(gene, true);
        update();
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
    public List<TraitGene> getExpressed() {
        if (this.expressedGenes == null || this.cachedGenes == null) {
            this.expressedGenes = new ArrayList<>(this.getTraits().stream().filter(gene -> gene.value > 0 && gene.isExpressed()).toList());
        }
        return this.expressedGenes;
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
    public StatusTypes getStatus() {
        if (!this.hasTag("Status")) {
            this.putFloat("Status", 0.0F);
        }
        return StatusTypes.byValue(this.getFloat("Status"));
    }

    @Override
    public void setStatus(StatusTypes status) {
        this.putFloat("Status", status.value());
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
            update();
        }
        return this.cachedGenome;
    }

    private void update() {
        if (this.cachedGenome == null) {
            this.cachedGenome = DnaUtil.getGenome(this.stack);
        }
        this.putString("DNA", cachedGenome.toString());
        this.genomeInit = true;
    }

    @Override
    public void copyFrom(ItemStrainComponent other) {
        this.cachedGenome = other.getGenome();
        this.cachedStrainInfo = other.getStrainInfo();
        this.expressedGenes = other.getExpressed();
        this.cachedThc = other.getThc();
    }

    @Override
    public void onTagInvalidated() {
        super.onTagInvalidated();
        this.genomeInit = false;
    }
}