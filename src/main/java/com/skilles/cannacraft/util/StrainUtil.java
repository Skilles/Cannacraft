package com.skilles.cannacraft.util;

import com.skilles.cannacraft.config.ModConfig;
import com.skilles.cannacraft.registry.ModItems;
import com.skilles.cannacraft.strain.Strain;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Map;
import java.util.Random;

import static com.skilles.cannacraft.Cannacraft.log;
import static com.skilles.cannacraft.strain.StrainMap.*;

public class StrainUtil {
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
    private static boolean containsWords(String input, String[] words) {
        return Arrays.stream(words).anyMatch(input::contains);
    }
    public static ItemStack getOutputStack(ItemStack stack) {
        if(AutoConfig.getConfigHolder(ModConfig.class).getConfig().getCrop().resource) {
            if (stack.isOf(ModItems.WEED_BUNDLE) && stack.hasTag()) {
                return getStrain(stack.getTag()).getItem().getDefaultStack();
            }
        } else {
            return StrainItems.WEED.item.getDefaultStack();
        }
        return Items.AIR.getDefaultStack();
    }
    public static StrainItems getStrainItems(Strain strain) {
        String name = strain.name();
        for(StrainItems item: StrainItems.values()) {
            if (containsWords(name, StringUtils.split(item.getName())) || name.contains(item.getName())) return item;
        }
        return StrainItems.WEED;
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
    public static void testItems() {
        log(getItem(new Strain("Purple Lapis", Type.HYBRID)));
    }
    public static void addStrain(String name, Type type) {
        Strain strain = new Strain(name, type);
        if (!type.equals(Type.UNKNOWN)) {
            int index;

            index = strainArray.size();

            if (strainList.containsKey(name)) {
                log("No duplicate strains!");
            } else {
                strainArray.put(index, strain);
                strainList.put(strainArray.get(index).name(), strainArray.get(index));
                save();
            }
        } else {
            strainArray.put(0, strain);
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
            /*boolean cascade = false;
            strainList.remove(strainArray.get(index).name());
            for (int i = index; strainArray.size() - 1 > index; i++) {
                strainArray.forcePut(i, strainArray.get(i + 1));
                strainArray.remove(i + 1);
                cascade = true;
            }
            if (!cascade) {
                strainArray.remove(index);
            }*/
            strainList.remove(strainArray.get(index).name());
            strainArray.remove(index);
        }
    }

    public static void resetStrains() {
        strainArray.clear();
        strainList.clear();
        ogStrainCount = 4;
        addStrain("Unknown", Type.UNKNOWN);
        addStrain("OG Kush", Type.HYBRID);
        addStrain("Purple Punch", Type.INDICA);
        addStrain("Chem Trix", Type.SATIVA);
        for (int i = 0; strainArray.size() > i; i++) {
            log("Strain: "+ strainArray.get(i));
            strainList.put(strainArray.get(i).name(), strainArray.get(i));
            ogStrainCount++;
        }
        save();
    }

    public static boolean isPresent(Strain strain) {
        return strainArray.containsValue(strain);
    }

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

    public static Strain toStrain(String name) {
        return strainList.get(name);
    }

    public static Map<String, Strain> getNames() {
        return strainList;
    }

    public static Map<Integer, Strain> getStrains() {
        return strainArray;
    }
    public enum StrainItems {
        WEED(ModItems.WEED_BUNDLE),
        COPPER(Items.RAW_COPPER),
        IRON(Items.IRON_NUGGET),
        DIAMOND(Items.DIAMOND),
        GOLD(Items.GOLD_NUGGET),
        REDSTONE(Items.REDSTONE),
        LAPIS(Items.LAPIS_LAZULI),
        EMERALD(Items.EMERALD),
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
