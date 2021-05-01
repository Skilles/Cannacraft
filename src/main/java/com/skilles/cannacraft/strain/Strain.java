package com.skilles.cannacraft.strain;


import java.util.Objects;

public final class Strain {
    public static int CLASS_COUNT = 0;
    private String name;
    private StrainMap.Type type;
    private final int id;

    public Strain(String strain, StrainMap.Type type) {
        setName(strain);
        setType(type);
        this.id = StrainMap.strainArray.size();
        CLASS_COUNT++;
    }
    public int id() { return id; }

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
