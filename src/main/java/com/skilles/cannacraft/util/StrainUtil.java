package com.skilles.cannacraft.util;

import com.skilles.cannacraft.Cannacraft;
import com.skilles.cannacraft.CannacraftClient;
import com.skilles.cannacraft.config.ModConfig;
import com.skilles.cannacraft.registry.ModItems;
import com.skilles.cannacraft.strain.Strain;
import com.skilles.cannacraft.strain.StrainMap.Type;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Rarity;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.jetbrains.annotations.TestOnly;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;

import static com.skilles.cannacraft.Cannacraft.log;
import static com.skilles.cannacraft.strain.StrainMap.*;

public class StrainUtil {
    static final Strain UNKNOWN_STRAIN = new Strain("Unknown", Type.UNKNOWN);
    public static final int MIN_THC = 13;

    public static int getStrainCount() {
        return strainArray.size();
    }
    public static Strain getStrain(int index) {
        if(!strainArray.containsKey(index)) return strainArray.get(0);
        return strainArray.get(index);
    }
    public static Strain getStrain(NbtCompound tag) {
        if(tag != null) {
            if(tag.contains("cannacraft:strain")) {
                return getStrain(((NbtCompound) tag.get("cannacraft:strain")).getInt("ID"));
            } else if(tag.contains("ID")) {
                return getStrain(tag.getInt("ID"));
            }
        }
        return getStrain(0);
    }
    public static Strain getStrain(String name) {
        if(!strainList.containsKey(name)) return strainArray.get(0);
        return strainList.get(name);
    }
    @Deprecated
    public static int indexOf(Strain strain) {
        return strainArray.inverse().get(strain);
    }
    @Deprecated
    public static int indexOf(String name) {
        if(!strainList.containsKey(name)) return 0;
        return indexOf(toStrain(name));
    }
    public static ItemStack getOutputStack(ItemStack stack) {
        if(AutoConfig.getConfigHolder(ModConfig.class).getConfig().getCrop().resource) {
            if (stack.isOf(ModItems.WEED_BUNDLE) && stack.hasTag()) {
                return getStrain(stack.getTag()).getItem().getDefaultStack();
            }
        } else {
            return StrainItems.DISTILLATE.item.getDefaultStack();
        }
        return Items.AIR.getDefaultStack();
    }
    private static boolean containsWords(String input, String[] words) {
        return Arrays.stream(words).anyMatch(input::contains);
    }
    public static StrainItems getStrainItem(Strain strain) {
        String name = strain.name();
        for(StrainItems item: StrainItems.values()) {
            if (containsWords(name, StringUtils.split(item.getName())) || name.contains(item.getName())) return item;
        }
        return StrainItems.DISTILLATE;
    }
    public static Item getItem(Strain strain) {
        // TODO: add other mod metals
        //Class<Items> itemsClass = Items.class;
        //Field[] itemFields = itemsClass.getFields();
        /*for (Field field: itemFields) {
            try {
                Item item = (Item) field.get(itemsClass);
                if(Arrays.stream(StrainItems.values()).anyMatch(obj -> obj.getKey().equals(item.getTranslationKey()))) {
                    String itemName = I18n.translate(item.getTranslationKey());
                    String[] itemNames = StringUtils.split(itemName);
                    if(name.contains(itemNames[0])) {
                        return item;
                    }
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }*/
        return strain.getItem();
    }
    @TestOnly
    public static void testItems() {
        log(getItem(new Strain("Purple Lapis", Type.HYBRID)));
    }
    @Deprecated
    public static void addStrain(String name, Type type) {
        addStrain(new Strain(name, type));
    }
    public static void addStrain(Strain strain) {
        if(!isPresent(strain)) {
            strainArray.put(strainArray.size(), strain);
            if(strain.type() != Type.UNKNOWN) strainList.put(strain.name(), strain);
            save();
        } else {
            log("No duplicate strains!");
        }
    }
    public static void initDefaultStrains() {
        strainArray.putAll(defaultStrains);
    }
    public static void validateStrains() {
        boolean needsInit = false;
        for(Strain strain : strainArray.values()) {
            if(strain.type() != Type.UNKNOWN) {
                if (strain.getItem() == null || strain.getRarity() ==  null) {
                    log(Level.ERROR, strain.name() + " corrupted, reinitializing");
                    strain.init();
                    needsInit = true;
                }
                if (!strainList.containsKey(strain.name())) {
                    log(Level.ERROR, "Strain name mismatch, resetting strains");
                    resetStrains();
                }
            }
        }
        if(needsInit) initNames();
    }
    private static void initNames() {
        log( "Strain name mismatch, reinitializing names");
        strainList.clear();
        for(Strain strain : strainArray.values()) {
            if(strain.type() != Type.UNKNOWN) strainList.put(strain.name(), strain);
        }
    }
    /**
     * @param type the type to find
     * @return returns true if the type is present in the strain list, false otherwise
     */
    private static boolean containsType(Type type) {
        for (int i = 1; i < strainArray.size(); i++) {
            if(strainArray.get(i).type().equals(type)) {
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
            if(strainArray.get(i).type().equals(type)) {
                return i;
            }
        }
        return 0;
    }

    public static void removeStrain(int index) {
        if(strainArray.containsKey(index)) {
            strainList.remove(strainArray.get(index).name());
            strainArray.remove(index);
        }
    }

    public static void resetStrains() {
        strainArray.clear();
        strainList.clear();
        /*for(Strain strain : defaultStrains.values()) {
            addStrain(strain);
        }*/
        strainArray.putAll(defaultStrains);
        if(CannacraftClient.config.getCrop().resource) {
            resourceStrainArray.putAll(defaultResourceStrains);
        }
        save();
    }

    /**
     * Immutable map of default strains
     * TODO: add default rarities
     */
    public static Map<Integer, Strain> defaultStrains = Map.ofEntries(
            defaultStrain(0, UNKNOWN_STRAIN),
            defaultStrain(1, new Strain("OG Kush", Type.HYBRID)),
            defaultStrain(2, new Strain("Purple Punch", Type.INDICA)),
            defaultStrain(3, new Strain("Chem Trix", Type.SATIVA)),
            defaultStrain(4, new Strain("Blue Dream", Type.HYBRID)),
            defaultStrain(5, new Strain("Bubba Kush", Type.INDICA)),
            defaultStrain(6, new Strain("Grandaddy Purple", Type.INDICA)),
            defaultStrain(7, new Strain("Green Crack", Type.SATIVA)),
            defaultStrain(8, new Strain("Northern Lights", Type.INDICA)),
            defaultStrain(9, new Strain("Pineapple Express", Type.HYBRID)),
            defaultStrain(10, new Strain("Girl Scout Cookies", Type.HYBRID)),
            defaultStrain(11, new Strain("Blueberry", Type.INDICA))

    );
    /**
     * Immutable map of resource strains
     */
    public static Map<Integer, Strain> defaultResourceStrains = Map.ofEntries(
            defaultStrain(defaultStrains.size(), new Strain("Iron OG", Type.HYBRID, Rarity.UNCOMMON)),
            defaultStrain(1, new Strain("Diamond Kush", Type.INDICA, Rarity.RARE)),
            defaultStrain( 2, new Strain("Lapis Dream", Type.SATIVA, Rarity.COMMON)),
            defaultStrain( 3, new Strain("Alaskan Emerald", Type.HYBRID, Rarity.RARE)),
            defaultStrain( 4, new Strain("Cherrystone", Type.INDICA, Rarity.UNCOMMON)),
            defaultStrain( 5, new Strain("Copper Haze", Type.INDICA, Rarity.COMMON)),
            defaultStrain(defaultStrains.size() + 6, new Strain("Coal Crack", Type.SATIVA, Rarity.COMMON)),
            defaultStrain(defaultStrains.size() + 7, new Strain("Goldberry", Type.INDICA, Rarity.UNCOMMON)),
            defaultStrain(defaultStrains.size() + 8, new Strain("Nether Lights", Type.INDICA, Rarity.EPIC))

    );
    private static AbstractMap.SimpleEntry<Integer, Strain> defaultStrain(int index, Strain strain) { return new AbstractMap.SimpleEntry<Integer, Strain>(index, strain); }
    public static boolean isPresent(Strain strain) { return strainArray.containsValue(strain); }

    public static boolean isPresent(String name) {
        return strainList.containsKey(name);
    }

    public static int normalDist(int mean, int std, int min) {
        Random random = new Random();
        int newThc = (int) Math.round(random.nextGaussian()*std+mean);
        if(newThc < min) {
            newThc = min;
        }
        return newThc;
    }
    public static int randThc(Strain strain) {
        return (int) (normalDist(15, 5, MIN_THC) * strain.thcMultiplier());
    }

    public static Strain toStrain(String name) {
        return strainList.getOrDefault(name, UNKNOWN_STRAIN);
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
        String getKey() {
            return item.getTranslationKey();
        }
    }
}
