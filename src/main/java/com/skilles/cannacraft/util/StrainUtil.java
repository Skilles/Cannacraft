package com.skilles.cannacraft.util;

import com.skilles.cannacraft.strain.Strain;
import com.skilles.cannacraft.strain.StrainMap;

import java.util.Map;
import java.util.Random;

public class StrainUtil {
    public static int getStrainCount() {
        return StrainMap.strainArray.size();
    }

    public static Strain getStrain(int index) {
        if(!StrainMap.strainArray.containsKey(index)) return StrainMap.strainArray.get(0);
        return StrainMap.strainArray.get(index);
    }

    public static Strain getStrain(String name) {
        if(!StrainMap.strainList.containsKey(name)) return StrainMap.strainArray.get(0);
        return StrainMap.strainList.get(name);
    }

    public static int indexOf(Strain strain) {
        return StrainMap.strainArray.inverse().get(strain);
    }

    public static int indexOf(String name) {
        if(!StrainMap.strainList.containsKey(name)) return 0;
        return indexOf(toStrain(name));
    }

    public static void addStrain(String name, StrainMap.Type type) {
        Strain strain = new Strain(name, type);
        if (!type.equals(StrainMap.Type.UNKNOWN)) {
            int index;

            index = StrainMap.strainArray.size();

            if (StrainMap.strainList.containsKey(name)) {
                strain = null;
                System.out.println("No duplicate strains!");
            } else {
                StrainMap.strainArray.put(index, strain);
                StrainMap.strainList.put(StrainMap.strainArray.get(index).name(), StrainMap.strainArray.get(index));
                StrainMap.save();
            }
        } else {
            StrainMap.strainArray.put(0, strain);
        }
    }

    /**
     * @param type the type to find
     * @return returns true if the type is present in the strain list, false otherwise
     */
    private static boolean containsType(StrainMap.Type type) {
        for (int i = 1; i < StrainMap.strainArray.size(); i++) {
            if(StrainMap.strainArray.get(i).type().equals(type)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param type the type to find
     * @return returns 0 if that type is not found or the index of the first found (excluding index 0)
     */
    private static int indexOfType(StrainMap.Type type) {
        for (int i = 1; i < StrainMap.strainArray.size(); i++) {
            if(StrainMap.strainArray.get(i).type().equals(type)) {
                return i;
            }
        }
        return 0;
    }

    public static void removeStrain(int index) {
        if(StrainMap.strainArray.containsKey(index)) {
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
            StrainMap.strainList.remove(StrainMap.strainArray.get(index).name());
            StrainMap.strainArray.remove(index);
        }
    }

    public static void resetStrains() {
        StrainMap.strainArray.clear();
        StrainMap.strainList.clear();
        StrainMap.ogStrainCount = 4;
        addStrain("Unknown", StrainMap.Type.UNKNOWN);
        addStrain("OG Kush", StrainMap.Type.HYBRID);
        addStrain("Purple Punch", StrainMap.Type.INDICA);
        addStrain("Chem Trix", StrainMap.Type.SATIVA);
        for (int i = 0; StrainMap.strainArray.size() > i; i++) {
            System.out.println("Strain: "+ StrainMap.strainArray.get(i));
            StrainMap.strainList.put(StrainMap.strainArray.get(i).name(), StrainMap.strainArray.get(i));
            StrainMap.ogStrainCount++;
        }
    }

    public static boolean isPresent(Strain strain) {
        return StrainMap.strainArray.containsValue(strain);
    }

    public static boolean isPresent(String name) {
        return StrainMap.strainList.containsKey(name);
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
        return StrainMap.strainList.get(name);
    }

    public static Map<String, Strain> getNames() {
        return StrainMap.strainList;
    }

    public static Map<Integer, Strain> getStrains() {
        return StrainMap.strainArray;
    }
}
