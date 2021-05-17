package com.skilles.cannacraft.strain;

import net.minecraft.nbt.NbtCompound;

import java.util.Objects;

import static com.skilles.cannacraft.Cannacraft.log;

public class Gene {
    private String name;
    private boolean recessive;
    private int id;
    private int level;

    public Gene(GeneTypes gene, int level) {
        this.name = gene.getName();
        if(level <= gene.getMax()) {
            this.level = level;
        } else {
            this.level = gene.getMax();
        }
        this.id = gene.getId();
    }
    public Gene(NbtCompound compound) {
        if(compound.contains("Gene") && compound.contains("Level")) {
            this.name = compound.getString("Gene");
            if(compound.getInt("Level") <= GeneTypes.byName(name).getMax()) {
                this.level = compound.getInt("Level");
            } else {
                this.level = GeneTypes.byName(name).getMax();
            }
            this.id = GeneTypes.byName(this.name).getId();
        } else {
            log("ERROR: no gene/level in constructor compound");
        }
    }
    public int id() { return id; }

    public int level() { return level; }

    public String name() {
        return name;
    }

    protected void setLevel(int level) {
        this.level = level;
    }

    public String toString() {
        return name + " | " + level;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Gene gene = (Gene) o;
        return name.equals(gene.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

}
