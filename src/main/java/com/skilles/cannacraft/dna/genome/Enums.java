package com.skilles.cannacraft.dna.genome;

import com.skilles.cannacraft.dna.chromosome.BaseChromosome;
import com.skilles.cannacraft.dna.chromosome.InfoChromosome;
import com.skilles.cannacraft.dna.chromosome.SexChromosome;
import com.skilles.cannacraft.dna.chromosome.TraitChromosome;

import java.util.HashMap;
import java.util.Map;

import static net.minecraft.enchantment.Enchantment.Rarity;

public class Enums {

    public enum Code {
        AAA(0),
        AAT(1),
        AAC(2),
        AAG(3),
        ATA(4),
        ATT(5),
        ATC(6),
        ATG(7),
        ACA(8),
        ACT(9),
        ACC(10),
        ACG(11),
        AGA(12),
        AGT(13),
        AGC(14),
        AGG(15),
        TAA(16),
        TAT(17),
        TAC(18),
        TAG(19),
        TTA(20),
        TTT(21),
        TTC(22),
        TTG(23),
        TCA(24),
        TCT(25),
        TCC(26),
        TCG(27),
        TGA(28),
        TGT(29),
        TGC(30),
        TGG(31);


        int value;

        public static int length = 3;

        private static final Map<Integer, Code> lookup = new HashMap<>();

        public static final Code[] values = values();

        Code(int i) {
            this.value = i;
        }

        public static String get(int index) {
            return lookup.get(index).name();
        }

        public static int convert(String string) {
            try {
                return Code.valueOf(string).value;
            } catch (IllegalArgumentException e) {
                System.err.println("Invalid pair: " + string);
                throw e;
            }
        }

        static {
            for (Code code : Code.values) {
                lookup.put(code.value, code);
            }
        }
    }

    public enum Phenotype {
        YIELD(0, 'Y', true, false, Rarity.UNCOMMON, ChromoType.PHYSICAL),
        SPEED(1, 'S', false, true, Rarity.COMMON, ChromoType.PHYSICAL), // grow speed, // thc+
        LUCK(2, 'L', true, false, Rarity.RARE, ChromoType.EFFECT), // extra block/mob drops
        STRENGTH(3, 'R', true, true, Rarity.COMMON, ChromoType.EFFECT), // strength based effects
        FLY(4, 'F', true, false, Rarity.RARE, ChromoType.EFFECT), // gives fly
        MUTATE(5, 'M', true, false, Rarity.RARE, ChromoType.BONUS),
        THC(6, 'T', false, false, Rarity.COMMON, ChromoType.BONUS);  // extra mutation chance

        public final int index;

        public final char symbol;

        public final boolean recessive;

        public final boolean sexLinked;

        public final Rarity rarity;

        public final ChromoType chromoType;

        public static int size;

        private static final Map<Integer, Phenotype> lookup = new HashMap<>();
        
        public static final Phenotype[] values = values();

        Phenotype(int index, char symbol, boolean recessive, boolean sexLinked, Rarity rarity, ChromoType chromoType) {
            this.index = index;
            this.symbol = symbol;
            this.recessive = recessive;
            this.sexLinked = sexLinked;
            this.rarity = rarity;
            this.chromoType = chromoType;
        }

        public static Phenotype get(int index) {
            return lookup.get(index);
        }

        static {
            for (Phenotype type : Phenotype.values) {
                lookup.put(type.index, type);
                size++;
            }
        }
    }

    public enum ChromoType {
        SEX1(0),
        SEX2(1),
        INFO(2),
        PHYSICAL(3),
        EFFECT(4),
        BONUS(5);

        public int index;

        public static int size;

        public BaseChromosome emptyChromosome;

        private static final Map<Integer, ChromoType> lookup = new HashMap<>();

        public static final ChromoType[] values = values();

        ChromoType(int index) {
            this.index = index;
        }

        public static ChromoType get(int index) {
            return lookup.get(index);
        }

        static {
            for (ChromoType type : ChromoType.values) {
                lookup.put(type.index, type);
                // Called in static initializer so that this entire enum inits before GeneType
                if (type.equals(SEX1) || type.equals(SEX2)) {
                    type.emptyChromosome = new SexChromosome(type);
                } else if (type.equals(INFO)) {
                    type.emptyChromosome = new InfoChromosome();
                } else {
                    type.emptyChromosome = new TraitChromosome(type);
                }
                size++;
            }
        }
    }

    public enum State {
        RECESSIVE(0),
        DOMINANT(1),
        CARRIER(2);

        public final int index;

        private static final Map<Integer, State> lookup = new HashMap<>();

        State(int index) {
            this.index = index;
        }

        public static final State[] values = values();

        public static State get(int i) {
            if (i > 2) throw new IllegalStateException("Invalid state: " + i);
            return lookup.get(i);
        }

        static {
            for (State state : State.values) {
                lookup.put(state.index, state);
            }
        }
    }

    public enum GeneType {
        TRAIT(0),
        INFO(1),
        SEX(2);

        public final int index;

        private static final Map<Integer, GeneType> lookup = new HashMap<>();

        public static final GeneType[] values = values();

        public static GeneType get(int index) {
            return lookup.get(index);
        }

        GeneType(int value) {
            this.index = value;
        }

        static {
            for (GeneType type : GeneType.values) {
                lookup.put(type.index, type);
            }
        }
    }

    public enum InfoType {
        STRAIN(0),
        THC(1),
        RESOURCE(2);

        private static final Map<Integer, InfoType> lookup = new HashMap<>();

        public static final InfoType[] values = values();

        public int index;

        InfoType(int index) {
            this.index = index;
        }

        public static InfoType get(int index) {
            return lookup.get(index);
        }

        static {
            for (InfoType type : InfoType.values) {
                lookup.put(type.index, type);
            }
        }
    }

}
