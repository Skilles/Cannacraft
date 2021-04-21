package com.skilles.cannacraft.strain;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;

public enum Genes {
    NORMAL(0, "normal", false),
    YIELD(1, "yield", false),
    SPEED(2, "speed", true);

    private static final Genes[] VALUES = Arrays.stream(values()).sorted(Comparator.comparingInt(Genes::getId)).toArray((i) -> new Genes[i]);
    private final int id;
    private final String name;
    private final boolean recessive;
    public static final int MAX_LEVEL = 3;

    Genes(int id, String name, boolean recessive) {
        this.id = id;
        this.name = name;
        this.recessive = recessive;
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public boolean isRecessive() {
        return this.recessive;
    }

    private static Genes getProductGene(Genes mainGene, Genes hiddenGene) { // returns dominant gene
        if (mainGene.isRecessive()) {
            return mainGene == hiddenGene ? mainGene : NORMAL;
        } else {
            return mainGene;
        }
    }

    public static Genes byId(int id) {
        if (id < 0 || id >= VALUES.length) {
            id = 0;
        }

        return VALUES[id];
    }

    public static Genes byName(String name) {
        Genes[] var1 = values();
        int var2 = var1.length;

        for (int var3 = 0; var3 < var2; ++var3) {
            Genes gene = var1[var3];
            if (gene.name.equals(name)) {
                return gene;
            }
        }
        return NORMAL;
    }

    public static Genes createRandom(Random random) {
        int i = random.nextInt(2);
        if (i == 0) {
            return YIELD;
        } else {
            return SPEED;
        }
    }
}
