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

import static com.skilles.cannacraft.Cannacraft.log;

/**
 * This class is responsible for a lot of strain related data management
 */
public final class StrainMap {

    private static final GsonBuilder builder = new GsonBuilder().enableComplexMapKeySerialization().setPrettyPrinting();
    private static final Gson gson = builder.create();

    public static int ogStrainCount = 4;
    public static final BiMap<Integer, Strain> strainArray = HashBiMap.create();
    public static final Map<String, Strain> strainList = new HashMap<>(); // for name lookup
    public enum Type {
        INDICA,
        SATIVA,
        HYBRID,
        UNKNOWN
    }

    public static void registerStrains() {
        load();
        validateStrains();
        log("Strains initialized: "+ strainArray);
        log("Strains initialized: "+ strainList);
        //GeneticsManager.test();
        save();
    }

    public static void save() {
        try (Writer writer = new FileWriter("strains.json")) {
            gson.toJson(strainArray, writer);
            log("Strains saved to file");
        } catch(Exception e) {
            e.printStackTrace();
            log("Error saving file");
        }
    }
    public static void load() {
        try (Reader reader = Files.newBufferedReader(Paths.get("strains.json"))) {
            java.lang.reflect.Type type = new TypeToken<Map<Integer, Strain>>() {
            }.getType();
            Map<Integer, Strain> strainMap = gson.fromJson(reader, type);
            for (Map.Entry<Integer, Strain> entry : strainMap.entrySet()) {
                strainArray.put(entry.getKey(), entry.getValue());
                strainList.put(entry.getValue().name(), entry.getValue());
            }
        } catch(Exception e) {
            log("Error loading strains");
            StrainUtil.resetStrains();
        }
    }
    private static void validateStrains() {
        for (Strain strain: strainArray.values()) {
            if(strain.getItem() == null) {
                strain.init();
                log(strain.name()+" corrupted, attempting to fix");
            }
        }
    }
}