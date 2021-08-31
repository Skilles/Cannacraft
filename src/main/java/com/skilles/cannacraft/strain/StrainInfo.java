package com.skilles.cannacraft.strain;

public record StrainInfo(Strain strain, int thc, boolean identified, java.util.List<Gene> geneList) {


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StrainInfo other = (StrainInfo) o;
        return (this.strain == other.strain);
    }
}
