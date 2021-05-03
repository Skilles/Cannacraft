package com.skilles.cannacraft.strain;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.FileWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
/**
 * This class is responsible for a lot of strain related data management
 */
public final class StrainMap {

    private static final GsonBuilder builder = new GsonBuilder().enableComplexMapKeySerialization().setPrettyPrinting();
    private static final Gson gson = builder.create();

    public static int ogStrainCount = 4;
    protected static final BiMap<Integer, Strain> strainArray = HashBiMap.create();
    protected static final Map<String, Strain> strainList = new HashMap<>(); // for name lookup
    public enum Type {
        INDICA,
        SATIVA,
        HYBRID,
        UNKNOWN
    }

    public static void registerStrains() {
        load();
        System.out.println("Strains initialized: "+ strainArray);
        System.out.println("Strains initialized: "+ strainList);
        //GeneticsManager.test();
        save();
    }
    public static int getStrainCount() {
        return strainArray.size();
    }
    public static Strain getStrain(int index) {
        if(!strainArray.containsKey(index)) return strainArray.get(0);
        return strainArray.get(index);
    }
    public static Strain getStrain(String name) {
        if(!strainList.containsKey(name)) return strainArray.get(0);
        return strainList.get(name);
    }
    public static int indexOf(Strain strain) {
        return strainArray.inverse().get(strain);
    }
    public static int indexOf(String name) {
        if(!strainList.containsKey(name)) return 0;
        return indexOf(toStrain(name));
    }
    public static void addStrain(String name, Type type) {
        Strain strain = new Strain(name, type);
        if (!type.equals(Type.UNKNOWN)) {
            int index;

            index = strainArray.size();

            if (strainList.containsKey(name)) {
                strain = null;
                System.out.println("No duplicate strains!");
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
            System.out.println("Strain: "+ strainArray.get(i));
            strainList.put(strainArray.get(i).name(),strainArray.get(i));
            ogStrainCount++;
        }
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
    public static void save() {
        try (Writer writer = new FileWriter("strains.json")) {
            gson.toJson(strainArray, writer);
            writer.close();
            System.out.println("Strains saved to file");
        } catch(Exception e) {
            System.out.println("Error saving file");
        }
    }
    public static void load() {
        try {
            java.lang.reflect.Type type = new TypeToken<Map<Integer, Strain>>() {
            }.getType();
            Reader reader = Files.newBufferedReader(Paths.get("strains.json"));
            Map<Integer, Strain> strainMap = gson.fromJson(reader, type);
            for (Map.Entry<Integer, Strain> entry : strainMap.entrySet()) {
                strainArray.put(entry.getKey(), entry.getValue());
                strainList.put(entry.getValue().name(), entry.getValue());
            }
        } catch(Exception e) {
            System.out.println("Error loading strains");
            resetStrains();
            save();
        }
    }
}