package com.skilles.cannacraft.strain;

import static com.skilles.cannacraft.util.StrainUtil.StrainItems;
import static com.skilles.cannacraft.util.StrainUtil.getStrain;

public record ResourcePair(StrainItems strain1,
                           StrainItems strain2,
                           StrainItems output) {

    public Strain getOutputStrain() {
        return getStrain(output);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResourcePair pair = (ResourcePair) o;
        return (this.strain1 == pair.strain1 && this.strain2 == pair.strain2);
    }
}
