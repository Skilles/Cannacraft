package com.skilles.cannacraft.dna.chromosome;

import java.util.Random;

public record Diploid(SexChromosome first, SexChromosome second) {

    private static final Random random = new Random();

    public SexChromosome random() {
        return random.nextInt(2) == 0 ? first : second;
    }

    public boolean isMale() { return first.isY() || second.isY(); }
}
