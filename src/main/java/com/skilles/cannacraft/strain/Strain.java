package com.skilles.cannacraft.strain;


import java.util.Objects;

public final class Strain {
    public static int CLASS_COUNT = 0;
    private String name;
    private StrainMap.Type type;

    public Strain(String strain, StrainMap.Type type) {
        setName(strain);
        setType(type);
        //setThc(normalDist(15, 5, 13));
        CLASS_COUNT++;
    }

    public String name() {
        return name;
    }

    protected void setName(String name) {
        this.name = name;
    }

    public StrainMap.Type type() {
        return type;
    }

    protected void setType(StrainMap.Type type) {
        this.type = type;
    }

    protected void setThc(int thc) {
        /*(switch(name) {
            case "OG Kush":
                thc = normalDist(15, 5, 13);
                break;
            case "Purple Punch":
                thc = normalDist(15, 5, 13);
                break;
            case "Chem Trix":
                thc = normalDist(15, 5, 13);
                break;
        }*/
    }
    public String toString() {
        return name + " | " + type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Strain strain = (Strain) o;
        return name.equals(strain.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

}
