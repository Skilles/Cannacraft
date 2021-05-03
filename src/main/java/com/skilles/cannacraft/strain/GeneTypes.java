package com.skilles.cannacraft.strain;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;
// TODO: add rarity
public enum GeneTypes {
    NORMAL(0, "normal", false, 0),
    YIELD(1, "yield", false, 2),
    SPEED(2, "speed", true, 3);

    private static final GeneTypes[] VALUES = Arrays.stream(values()).sorted(Comparator.comparingInt(GeneTypes::getId)).toArray(GeneTypes[]::new);
    private final int id;
    private final String name;
    private final int maxLevel;
    private final boolean recessive;
    public static final int MAX_LEVEL = 3;

    GeneTypes(int id, String name, boolean recessive, int maxLevel) {
        this.id = id;
        this.name = name;
        this.maxLevel = maxLevel;
        this.recessive = recessive;
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public int getMax() { return this.maxLevel; }

    public boolean isRecessive() {
        return this.recessive;
    }

    private static GeneTypes getProductGene(GeneTypes mainGene, GeneTypes hiddenGene) { // returns dominant gene
        if (mainGene.isRecessive()) {
            return mainGene == hiddenGene ? mainGene : NORMAL;
        } else {
            return mainGene;
        }
    }

    public static GeneTypes byId(int id) {
        if (id < 0 || id >= VALUES.length) {
            id = 0;
        }

        return VALUES[id];
    }

    public static GeneTypes byName(String name) {
        GeneTypes[] var1 = values();
        int var2 = var1.length;

        for (GeneTypes gene : var1) {
            if (gene.name.equals(name)) {
                return gene;
            }
        }
        return NORMAL;
    }

    public static GeneTypes createRandom(Random random) {
        int i = random.nextInt(2);
        if (i == 0) {
            return YIELD;
        } else {
            return SPEED;
        }
    }
}
