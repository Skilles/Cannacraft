package com.skilles.cannacraft.strain;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Cannadex {
    private final Set<Strain> smokedStrains = new HashSet<>();
    private final Set<Strain> resourceStrains = new HashSet<>();
    private final Set<Strain> discoveredStrains = new HashSet<>();

    public Set<Strain> getSmokedStrains() {
        return smokedStrains;
    }

    public void addSmoked(Strain smokedStrain) {
        this.smokedStrains.add(smokedStrain);
    }

    public Set<Strain> getDiscoveredStrains(boolean resource) {
        return resource ? resourceStrains : discoveredStrains;
    }

    public void discoverStrains(Strain... discoveredStrain) {
        this.resourceStrains.addAll(Arrays.stream(discoveredStrain).filter(Strain::isResource).collect(Collectors.toList()));
        this.discoveredStrains.addAll(List.of(discoveredStrain));
    }
}
