package com.skilles.cannacraft.util;

import com.google.common.collect.Lists;
import com.skilles.cannacraft.strain.*;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.Pair;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.skilles.cannacraft.Cannacraft.log;
import static com.skilles.cannacraft.strain.StrainMap.strainList;
import static com.skilles.cannacraft.util.StrainUtil.addStrain;

public class CrossUtil {
    private static final Map<String, Integer> suffixesMap = new HashMap<String, Integer>() {{
        put("OG", 5);
        put("Kush", 2);
        put("Cookies", 1);
        put("Dream", 1);
        put("Poison", 0);
        put("Crack", 0);
        put("Dawg", 0);
        put("Punch", 1);
        put("Trix", 0);
        put("Cake", 0);
    }
    };

    public static int crossThc(int thc1, int thc2) {
        return (thc1 + thc2) / 2;
    }

    /**
     * @return a list with all possible combinations of strains
     */
    public static List<String> getStrainCombinations(List<String> nameOne, List<String> nameTwo) {
        List<List<String>> combinationLists = Lists.cartesianProduct(nameOne, nameTwo);
        List<String> nameList = new ArrayList<>();
        for (List<String> stringList: combinationLists) {
            nameList.add(stringList.get(0) + " " + stringList.get(1));
            nameList.add(stringList.get(1) + " " + stringList.get(0));
        }
        return nameList;
    }

    /**
     * @param map to parse
     * @return returns the key with the highest value
     */
    public static <K, V extends Comparable<V>> K maxEntry(Map<K, V> map) {
        Optional<Map.Entry<K, V>> maxEntry = map.entrySet()
                .stream()
                .max(Map.Entry.comparingByValue()
                );
        assert maxEntry.isPresent();
        return maxEntry.get().getKey();
    }

    /**
     * TODO: follow standard of mother + father
     * TODO: return Strain instead of string
     * @param name1 by default is the first word
     * @param name2 by default is the second word
     * @return the crossed name of strains according to the prefix list
     */
    public static String crossNames(String name1, String name2) {
        List<String> nameOneFinal = Arrays.asList(StringUtils.split(name1));
        List<String> nameTwoFinal = Arrays.asList(StringUtils.split(name2));
        final List<String> finalNames = new ArrayList<String>() {{
            addAll(nameOneFinal);
            addAll(nameTwoFinal);
        }};
        if(nameOneFinal.contains("Unknown") || nameTwoFinal.contains("Unknown")) return "Unknown";


        // Finds all combinations of names and returns a value if any are a known strain
        List<String> nameList = getStrainCombinations(nameOneFinal, nameTwoFinal);
        Optional<String> optional = nameList.stream().filter(strainList::containsKey).findAny();
        if(optional.isPresent()) {
            return optional.get();
        }
        // Early return if both names are one word
        if(nameOneFinal.size() == 1 && nameTwoFinal.size() == 1){
            return nameOneFinal.get(0) + " " + nameTwoFinal.get(0);
        }
        // Creates a mutable array of all words
        List<String> nameOne = new LinkedList<>(Arrays.asList(StringUtils.split(name1)));
        List<String> nameTwo = new LinkedList<>(Arrays.asList(StringUtils.split(name2)));
        List<String> names = new ArrayList<String>() {{
            addAll(nameOneFinal);
            addAll(nameTwoFinal);
        }};
        // Set suffix and remove from names
        Map<String, Integer> tempSuffixMap = new HashMap<>();
        for (String finalName : finalNames) {
            if (suffixesMap.containsKey(finalName)) {
                tempSuffixMap.put(finalName, suffixesMap.get(finalName));
                // Checks if suffix is part of name1 or name2, then removes if it is
                if (nameOne.contains(finalName)) {
                    filterName(nameOne, tempSuffixMap, names);
                } else if (nameTwo.contains(finalName)) {
                    filterName(nameTwo, tempSuffixMap, names);
                }
            }
        }
        String newName1;
        String newName2;
        // Checks if suffix was found, if true then suffix is one with highest priority
        if(!tempSuffixMap.isEmpty()) {
            newName2 = maxEntry(tempSuffixMap);
        } else {
            // If no suffix was not found, set suffix to last element in name list
            newName2 = names.get(names.size() - 1);
        }
        // Sets prefix to first element in name list
        if(nameOne.isEmpty()) {
            newName1 = nameTwo.get(0);
        } else if(nameTwo.isEmpty()) {
            newName1 = nameOne.get(0);
        } else {
            if(nameOneFinal.contains(newName2)) {
                newName1 = nameTwo.get(0);
            } else {
                newName1 = nameOne.get(0);
            }
        }
        return newName1+ " " + newName2;
    }

    /**
     * Crosses two strains (name and type) and adds them to the list if not present
     * @param female strain of female
     * @param male strain of male
     * @return crossed strain
     */
    public static Strain crossStrains(Strain female, Strain male) {
        Strain crossedStrain;
        if(female.isResource() && male.isResource()) {
            crossedStrain = crossResources(female, male);
        } else {
            crossedStrain = new Strain(crossNames(female.name(), male.name()), crossTypes(female.type(), male.type()), false);
            // Unnecessary TODO: maybe remove
            if(!StrainUtil.isPresent(crossedStrain, false)) {
                addStrain(crossedStrain);
                log("New Strain: " + crossedStrain);
                return crossedStrain;
            } else {
                log("Duplicate strain");
            }
        }
        return strainList.get(crossedStrain.name());
    }
    /**
     * Crosses two resource strains using predefined recipes
     * @param female strain of female
     * @param male strain of male
     * @return crossed strain
     */
    public static Strain crossResources(Strain female, Strain male) {
        StrainUtil.StrainItems fItem = female.strainItem;
        StrainUtil.StrainItems mItem = male.strainItem;
        for(ResourcePair pair : StrainUtil.resourcePairs) {
            if((pair.strain1() == fItem || pair.strain2() == fItem) && (pair.strain1() == mItem || pair.strain2() == mItem))
                return pair.getOutputStrain();
        }
        if (MiscUtil.random().nextFloat() > 0.7F) return male;
        return female;
    }
    /**
     * This method eventually sorts the output list to only contain names that are not part of the suffix name
     * @param name name to filter
     * @param tempSuffixMap suffix map that contains suffixes from name1 and name2
     * @param names output list
     * @return returns how many names were removed from the compound word (0 = simple word)
     */
    private static int filterName(List<String> name, Map<String, Integer> tempSuffixMap, List<String> names) {
        if(name.size() > 1) {
            // Check if nameTwo has two prefixes and clears if it does
            if(suffixesMap.containsKey(name.get(0)) && suffixesMap.containsKey(name.get(1))) {
                names.remove(name.get(0));
                names.remove(name.get(1));
                name.clear();
                return 2;
            } else {
                if (tempSuffixMap.containsKey(name.get(0))) {
                    names.remove(name.remove(0));
                    return 1;
                } else if (tempSuffixMap.containsKey(name.get(1))) {
                    names.remove(name.remove(1));
                    return 1;
                }
            }
        } else {
            // Remove name from names because it is for sure a lone prefix
            names.remove(name.get(0));
            name.clear();
        }
        return 0;
    }

    /**
     * @param type1 dominant type which has priority
     * @return returns hybrid ONLY if sativa x indica or hybrid x hybrid
     * TODO: set type dynamically based on name
     */
    public static StrainMap.Type crossTypes(StrainMap.Type type1, StrainMap.Type type2) {
        if(type2.equals(StrainMap.Type.UNKNOWN)) return StrainMap.Type.UNKNOWN;
        switch(type1) {
            case INDICA:
                if(type2.equals(StrainMap.Type.SATIVA)) return StrainMap.Type.HYBRID;
                return StrainMap.Type.INDICA;
            case SATIVA:
                if(type2.equals(StrainMap.Type.INDICA)) return StrainMap.Type.HYBRID;
                return StrainMap.Type.SATIVA;
            case HYBRID:
                if(type2.equals(StrainMap.Type.HYBRID)) return StrainMap.Type.HYBRID;
                return type2;
            default:
                return StrainMap.Type.UNKNOWN;
        }
    }
    @Nullable
    public static NbtList crossGenes(ItemStack stack1, ItemStack stack2) {
        if(stack1.hasNbt() && stack1.getOrCreateSubNbt("cannacraft:strain").getList("Attributes", NbtType.COMPOUND) != null && stack2.hasNbt() && stack2.getOrCreateSubNbt("cannacraft:strain").getList("Attributes", NbtType.COMPOUND) != null) {
            ArrayList<Gene> list1 = MiscUtil.fromNbtList(stack1.getSubNbt("cannacraft:strain").getList("Attributes", NbtType.COMPOUND));
            ArrayList<Gene> list2 = MiscUtil.fromNbtList(stack2.getSubNbt("cannacraft:strain").getList("Attributes", NbtType.COMPOUND));
            ArrayList<Gene> geneList = new ArrayList<>();
            for(int i = 0; i < list1.size() && i < list2.size(); i++) {
                geneList.add(crossGenes(list1.get(i), list2.get(i)));
            }
            return MiscUtil.toNbtList(geneList);
        }
        return null;
    }
    protected static Gene crossGenes(Gene gene1, Gene gene2) {
        if(gene1.type().equals(gene2.type())) {
            int level1 = gene1.level();
            int level2 = gene2.level();
            /*switch (Math.abs(level2 - level1)) {
                case 0:
                    break;
                case 1:
                    break;
                case 2:
                    break;
                case 3:
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + Math.abs(level2 - level1));
            }*/
            return new Gene(gene1.type(), (level1 + level2) / 2);
        } else {
            return gene1;
        }
    }
    // terrible code
    private static Pair<GeneTypes, Integer> crossGenes(int level1, int level2, GeneTypes type) {
        int levelDiff = Math.abs(level1 - level2);
        int newLevel = 0;
        Random random = new Random();

        switch(levelDiff) {
            case 0:
                newLevel = level1;
                break;
            case 1:
                int i = random.nextInt(2); // 0 - 1
                newLevel = switch (i) {
                    case 0 -> // 50%
                            Integer.min(level1, level2);
                    case 1 -> // 50%
                            Integer.max(level1, level2);
                    default -> newLevel;
                };
            case 2:
                i = random.nextInt(4); // 0 - 3
                if(i == 0) { // 0 25%
                    newLevel = Integer.min(level1, level2);
                    break;
                } else if(i <= 2) { // 1 or 2 50%
                    newLevel = Integer.sum(level1, level2)/2;
                    break;
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
                    break;
                } else { // 3 25%
                    newLevel = Integer.max(level1, level2);
                    break;
                }
        }
        return new Pair<>(type, newLevel);
    }
}
