package com.skilles.cannacraft.strain;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.skilles.cannacraft.util.StrainUtil;

import java.io.FileWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
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
            StrainUtil.resetStrains();
            save();
        }
    }
}