package com.skilles.cannacraft.util;

import com.skilles.cannacraft.registry.ModItems;
import com.skilles.cannacraft.strain.ResourcePair;
import com.skilles.cannacraft.strain.Strain;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Rarity;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.skilles.cannacraft.Cannacraft.log;
import static com.skilles.cannacraft.CannacraftClient.config;
import static com.skilles.cannacraft.strain.StrainMap.*;

public class StrainUtil {

    static final Strain UNKNOWN_STRAIN = new Strain("Unknown", Type.UNKNOWN, true);

    public static final int MIN_THC = 13;

    public static int getStrainCount() {
        return strainArray.size();
    }

    private static Strain getResourceStrain(int index) {
        if (!resourceStrainArray.containsKey(index)) return resourceStrainArray.get(0);
        return resourceStrainArray.get(index);
    }

    @Deprecated
    public static Strain getStrain(int index) {
        if (!strainArray.containsKey(index)) return strainArray.get(0);
        return strainArray.get(index);
    }

    public static Strain getStrain(int index, boolean resource) {
        return resource ? getResourceStrain(index) : getStrain(index);
    }

    public static Strain getStrain(NbtCompound tag) {
        if (tag != null) {
            if (tag.contains("cannacraft:strain")) {
                NbtCompound strainTag = (NbtCompound) tag.get("cannacraft:strain");
                return getStrain(strainTag.getInt("ID"), strainTag.getBoolean("Resource"));
            } else if (tag.contains("ID")) {
                return getStrain(tag.getInt("ID"), tag.getBoolean("Resource"));
            }
        }
        return getStrain(0, false);
    }

    public static Strain getStrain(String name) {
        if (!strainList.containsKey(name)) return strainArray.get(0);
        return strainList.get(name);
    }

    public static Strain getStrain(StrainItems item) {
        String name = item.getName();
        for (Strain strain : resourceStrainArray.values()) {
            if (containsWords(strain.name(), StringUtils.split(name)) || strain.name().contains(name)) return strain;
        }
        return strainArray.get(0);
    }

    /**
     * This method returns the index of a strain. If the strain is new, it is added to the strainArray
     * @param strain to get the index of
     * @param register whether to register the strain
     * @return the ID of the strain
     */
    public static int indexOf(Strain strain, boolean register) {
        if (strain.isResource()) {
            // Resource strains are manually assigned an ID
            return strain.id();
        } else {
            // Can't find strain
            try {
                return strainArray.inverse().get(strain);
            } catch (NullPointerException e) {
                if (register) {
                    log("Adding new strain");
                    strain.init();
                    log(strain);
                } else {
                    log("Error getting strain index");
                    log(e.getMessage());
                    System.out.println();
                    log(strainArray);
                }
                return indexOf(strain, register);
            }
        }
    }

    @Deprecated
    public static int indexOf(String name) {
        if (!strainList.containsKey(name)) return 0;
        return indexOf(toStrain(name), false);
    }

    private static boolean containsWords(String input, String[] words) {
        return Arrays.stream(words).anyMatch(input::contains);
    }

    public static StrainItems getStrainItem(Strain strain) {
        String name = strain.name();
        return Arrays.stream(StrainItems.values())
                .filter(item -> containsWords(name, StringUtils.split(item.getName())) || name.contains(item.getName()))
                .findFirst().orElse(StrainItems.DISTILLATE);
    }

    @Deprecated
    public static void addStrain(String name, Type type) {
        addStrain(new Strain(name, type, false));
    }

    public static void addStrain(Strain strain) {
        if (!isPresent(strain, false)) {
            strainArray.put(strainArray.size(), strain);
            if (strain.type() != Type.UNKNOWN) strainList.put(strain.name(), strain);
            save();
        } else {
            log("No duplicate strains!");
        }
    }

    @Deprecated
    public static void initDefaultStrains() {
        //strainArray.putAll(defaultStrains);
        resourceStrainArray.putAll(StrainUtil.defaultResourceStrains);
        log(resourceStrainArray);
    }

    public static void validateStrains() {
        boolean needsInit = false;
        for (Strain strain : strainArray.values()) {
            if (strain.type() != Type.UNKNOWN) {
                if (strain.getItem() == null || strain.getRarity() ==  null) {
                    log(Level.ERROR, strain.name() + " corrupted, reinitializing");
                    strain.init();
                    needsInit = true;
                }
                if (!strainList.containsKey(strain.name())) {
                    log(Level.ERROR, "Strain name mismatch, adding to name list");
                    if (strainList.containsValue(strain)) needsInit = true;
                    strainList.put(strain.name(), strain);
                }
            }
        }
        if (needsInit) initNames();
        save();
    }

    private static void initNames() {
        log("Strain name mismatch, reinitializing names");
        strainList.clear();
        strainArray.values().stream().filter(strain -> strain.type() != Type.UNKNOWN).forEachOrdered(strain -> strainList.put(strain.name(), strain));
        resourceStrainArray.values().forEach(strain -> strainList.put(strain.name(), strain));
    }

    /**
     * @param type the type to find
     * @return returns true if the type is present in the strain list, false otherwise
     */
    private static boolean containsType(Type type) {
        for (int i = 1; i < strainArray.size(); i++) {
            if (strainArray.get(i).type().equals(type)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param type the type to find
     * @return returns 0 if that type is not found or the index of the first found (excluding index 0)
     */
    private static int indexOfType(Type type) {
        for (int i = 1; i < strainArray.size(); i++) {
            if (strainArray.get(i).type().equals(type)) {
                return i;
            }
        }
        return 0;
    }

    public static void removeStrain(int index) {
        if (strainArray.containsKey(index)) {
            strainList.remove(strainArray.get(index).name());
            strainArray.remove(index);
        }
    }

    public static void resetStrains() {
        strainArray.clear();
        strainList.clear();
        /*for (Strain strain : defaultStrains.values()) {
            addStrain(strain);
        }*/
        //initDefaultStrains();
        save();
    }

    /**
     * Immutable map of default strains
     * TODO: add default rarities
     */
    public static Map<Integer, Strain> defaultStrains = Map.ofEntries(
            defaultStrain(0, UNKNOWN_STRAIN),
            defaultStrain(1, new Strain("OG Kush", Type.HYBRID, true)),
            defaultStrain(2, new Strain("Purple Punch", Type.INDICA, Rarity.UNCOMMON, true)),
            defaultStrain(3, new Strain("Chem Trix", Type.SATIVA, Rarity.UNCOMMON, true)),
            defaultStrain(4, new Strain("Blue Dream", Type.HYBRID, true)),
            defaultStrain(5, new Strain("Bubba Kush", Type.INDICA, true)),
            defaultStrain(6, new Strain("Grandaddy Purple", Type.INDICA, true)),
            defaultStrain(7, new Strain("Green Crack", Type.SATIVA, true)),
            defaultStrain(8, new Strain("Northern Lights", Type.INDICA, Rarity.UNCOMMON, true)),
            defaultStrain(9, new Strain("Pineapple Express", Type.HYBRID, Rarity.RARE, true)),
            defaultStrain(10, new Strain("Girl Scout Cookies", Type.HYBRID, true)),
            defaultStrain(11, new Strain("Blueberry", Type.INDICA, Rarity.UNCOMMON, true))
    );

    /**
     * Immutable map of resource strains
     */
    public static Map<Integer, Strain> defaultResourceStrains = Map.ofEntries(
            defaultStrain(defaultStrains.size(), new Strain("Iron OG", Type.HYBRID, Rarity.UNCOMMON, true, true)),
            defaultStrain(defaultStrains.size() + 1, new Strain("Diamond Kush", Type.INDICA, Rarity.RARE, true, true)),
            defaultStrain(defaultStrains.size() + 2, new Strain("Lapis Dream", Type.SATIVA, Rarity.COMMON, true, true)),
            defaultStrain(defaultStrains.size() + 3, new Strain("Alaskan Emerald", Type.HYBRID, Rarity.RARE, true, true)),
            defaultStrain(defaultStrains.size() + 4, new Strain("Cherrystone", Type.INDICA, Rarity.UNCOMMON, true, true)),
            defaultStrain(defaultStrains.size() + 5, new Strain("Copper Haze", Type.INDICA, Rarity.COMMON, true, true)),
            defaultStrain(defaultStrains.size() + 6, new Strain("Coal Crack", Type.SATIVA, Rarity.COMMON, true, true)),
            defaultStrain(defaultStrains.size() + 7, new Strain("Goldberry", Type.INDICA, Rarity.UNCOMMON, true, true)),
            defaultStrain(defaultStrains.size() + 8, new Strain("Nether Lights", Type.INDICA, Rarity.EPIC, true, true))
    );

    private static AbstractMap.SimpleEntry<Integer, Strain> defaultStrain(int index, Strain strain) { return new AbstractMap.SimpleEntry<>(index, strain.withId(index)); }

    public static boolean isPresent(Strain strain, boolean resource) { return resource ? resourceStrainArray.containsValue(strain) :  strainArray.containsValue(strain); }

    public static boolean isPresent(String name) {
        return strainList.containsKey(name);
    }

    public static Strain getLatestStrain() {
        return getStrain(strainArray.size() - 1);
    }

    public static int normalDist(int mean, int std, int min) {
        Random random = new Random();
        int newThc = (int) Math.round(random.nextGaussian()*std+mean);
        if (newThc < min) {
            newThc = min;
        }
        return newThc;
    }

    public static List<Strain> getStrainsByRarity(Rarity rarity) {
        List<Strain> output = resourceStrainArray.values().stream().filter(strain -> strain.getRarity().equals(rarity)).collect(Collectors.toList());
        output.addAll(strainArray.values().stream().filter(strain -> strain.getRarity().equals(rarity)).collect(Collectors.toList()));
        return output;
    }

    public static List<Strain> getStrainPool() {
        List<Strain> output = new ArrayList<>(defaultStrains.values());
        if (config.getCrop().resource) {
            output.addAll(defaultResourceStrains.values());
        }
        output.remove(UNKNOWN_STRAIN);
        return output;
    }

    public static Map<Integer, Strain> getCustomStrains() {
        Map<Integer, Strain> output = new HashMap<>(strainArray);
        IntStream.iterate(strainArray.size() - 1, i -> i == -1, i -> i - 1).forEach(output::remove);
        return output;
    }

    public static Strain toStrain(String name) {
        return strainList.getOrDefault(name, resourceStrainArray.values().stream().filter(x -> x.name().equalsIgnoreCase(name)).findAny().orElse(UNKNOWN_STRAIN));
    }

    public static Map<String, Strain> getNames() {
        return strainList;
    }

    public static Map<Integer, Strain> getStrains() {
        return strainArray;
    }

    public static float getThcMultiplier(Strain strain) {
        switch(strain.getRarity()) {
            case UNCOMMON -> {
                return 1.2F;
            }
            case RARE -> {
                return 1.5F;
            }
            case EPIC -> {
                return 1.8F;
            }
            default -> {
                return 1.0F;
            }
        }
    }

    public static ResourcePair[] resourcePairs = new ResourcePair[]{
            new ResourcePair(StrainItems.COAL, StrainItems.COPPER, StrainItems.IRON),
            new ResourcePair(StrainItems.IRON, StrainItems.COPPER, StrainItems.GOLD),
            new ResourcePair(StrainItems.IRON, StrainItems.GOLD, StrainItems.IRON),
            new ResourcePair(StrainItems.GOLD, StrainItems.DIAMOND, StrainItems.BLAZE),
            new ResourcePair(StrainItems.COAL, StrainItems.REDSTONE, StrainItems.COPPER),
            new ResourcePair(StrainItems.EMERALD, StrainItems.LAPIS, StrainItems.DIAMOND),
            new ResourcePair(StrainItems.DIAMOND, StrainItems.LAPIS, StrainItems.ENDER_PEARL),
            new ResourcePair(StrainItems.ENDER_PEARL, StrainItems.DIAMOND, StrainItems.NETHERITE)};

    public enum StrainItems {
        DISTILLATE(ModItems.WEED_DISTILLATE),
        COAL(Items.COAL),
        COPPER(Items.RAW_COPPER),
        IRON(Items.IRON_NUGGET),
        DIAMOND(Items.DIAMOND),
        GOLD(Items.GOLD_NUGGET),
        REDSTONE(Items.REDSTONE),
        LAPIS(Items.LAPIS_LAZULI),
        EMERALD(Items.EMERALD),
        // TODO: add to resource crops
        ENDER_PEARL(Items.ENDER_PEARL),
        BLAZE(Items.BLAZE_POWDER),
        NETHERITE(Items.NETHERITE_SCRAP);

        public final Item item;
        StrainItems(Item item) {
            this.item = item;
        }

        String getName() {
            return I18n.translate(item.getTranslationKey());
        }
    }
}
