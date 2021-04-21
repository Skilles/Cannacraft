package com.skilles.cannacraft.strain;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.Pair;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Random;

public final class GeneticsManager {
    public static int crossThc(int thc1, int thc2) {
        return (thc1 + thc2) / 2;
    }
    public static ArrayList<Pair<Genes, Integer>> geneArray = new ArrayList<>();
    /**
     *
     * @param name1 the first word of this argument will be the first word of the new name
     * @param name2 the second word of this argument will be the second word of the new name (first if only one word)
     * @return Returns a combined strain name
     */
    public static String crossNames(String name1, String name2) {
        String[] nameOneArray = StringUtils.split(name1);
        String[] nameTwoArray = StringUtils.split(name1);
        if(nameTwoArray.length < 2) {
            return nameOneArray[0] + " " + nameTwoArray[0];
        }
        return nameOneArray[0] + " " + nameTwoArray[1];
    }
    public static NbtList toNbtList(ArrayList<Pair<Genes, Integer>> list) {
        NbtList nbtList = new NbtList();
        for (Pair<Genes, Integer> entry: list) {
            NbtCompound compound = new NbtCompound();
            compound.putString("Gene", entry.getLeft().getName());
            compound.putInt("Level", entry.getRight());
            nbtList.add(compound);
        }
        return nbtList;
    }
    public static ArrayList<Pair<Genes, Integer>> fromNbtList(NbtList list) {
        ArrayList<Pair<Genes, Integer>> arrayList = new ArrayList<>();
        for (NbtElement compoundEntry : list) {
            Pair pair = new Pair(((NbtCompound) compoundEntry).get("Gene"),((NbtCompound) compoundEntry).get("Level"));
            arrayList.add(pair);
        }
        return arrayList;
    }
    public static void test() {
        ArrayList<Pair<Genes, Integer>> arrayList = new ArrayList<>();
        arrayList.add(new Pair<>(Genes.SPEED, 1));
        arrayList.add(new Pair<>(Genes.YIELD, 2));
        NbtList nbtList = toNbtList(arrayList);
        System.out.println(arrayList);
        System.out.println(nbtList);
        System.out.println(fromNbtList(nbtList));
    }
    public static ArrayList<Pair<Genes, Integer>> getTestArray() {
        ArrayList<Pair<Genes, Integer>> arrayList = new ArrayList<>();
        arrayList.add(new Pair<>(Genes.SPEED, 1));
        arrayList.add(new Pair<>(Genes.YIELD, 2));
        return arrayList;
    }
    public Pair<Genes, Integer> crossGenes(int level1, int level2, Genes type) {
        int levelDiff = Math.abs(level1 - level2);
        int newLevel = 0;
        Random random = new Random();

        switch(levelDiff) {
            case 0:
                newLevel = level1;
                break;
            case 1:
                int i = random.nextInt(2); // 2 cases
                switch(i) {
                    case 0: // 50%
                        newLevel = Integer.min(level1, level2);
                        break;
                    case 1: // 50%
                        newLevel = Integer.max(level1, level2);
                        break;
                }
            case 2:
                i = random.nextInt(3); // 3 cases
                if(i == 0) { // 0 25%
                    newLevel = Integer.min(level1, level2);
                    break;
                } else if(i > 0 && i < 3) { // 1 or 2 50%
                    newLevel = Integer.sum(level1, level2)/2;
                } else { // 3 25%
                    newLevel = Integer.max(level1, level2);
                    break;
                }
            case 3: // 1: 0, 2: 3
                i = random.nextInt(4); // 4 cases
                if(i < 2) { // 0 or 1 50%
                    newLevel = Integer.min(level1, level2);
                    break;
                } else if(i == 2) { // 2 25%
                    newLevel = Integer.sum(level1, level2)/2;
                } else { // 3 25%
                    newLevel = Integer.max(level1, level2);
                    break;
                }
        }
        return new Pair<>(type, newLevel);
    }
}
