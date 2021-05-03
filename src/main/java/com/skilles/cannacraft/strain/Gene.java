package com.skilles.cannacraft.strain;

import net.minecraft.nbt.NbtCompound;

import java.util.Objects;

public class Gene {
    private String name;
    private boolean recessive;
    private int id;
    private int level;

    public Gene(GeneTypes gene, int level) {
        this.name = gene.getName();
        this.level = level;
        this.id = gene.getId();
    }
    public Gene(NbtCompound compound) {
        if(compound.contains("Gene") && compound.contains("Level")) {
            this.name = compound.getString("Gene");
            this.level = compound.getInt("Level");
            this.id = GeneTypes.byName(this.name).getId();
        } else {
            System.out.println("ERROR: no gene/level in constructor compound");
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
