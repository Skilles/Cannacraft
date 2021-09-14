package com.skilles.cannacraft.strain.cannadex;

import com.skilles.cannacraft.components.EntityInterface;
import com.skilles.cannacraft.components.StrainInterface;
import com.skilles.cannacraft.registry.ModMisc;
import com.skilles.cannacraft.strain.Strain;
import com.skilles.cannacraft.strain.StrainMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Cannadex {
    private final Set<Strain> smokedStrains = new HashSet<>();
    private final Set<Strain> resourceStrains = new HashSet<>();
    private final Set<Strain> discoveredStrains = new HashSet<>();
    private final Set<Strain> allStrains = new HashSet<>();

    public Cannadex() {

    }

    public Set<Strain> getSmokedStrains() {
        return smokedStrains;
    }

    public Set<Strain> getAllStrains() {
        return this.allStrains;
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

    public void discoverStrain(Strain discoveredStrain) {
        if (discoveredStrain.isResource()) {
            this.resourceStrains.add(discoveredStrain);
        } else {
            this.discoveredStrains.add(discoveredStrain);
        }
        this.allStrains.add(discoveredStrain);
    }

    /**
     * Gets the strain of the {@code ItemStack} and adds it to the player's
     * {@code Cannadex}. Should only be called on the client.
     * @param stack the strain item of a certain strain
     */
    public static void clientDiscoverStrain(ItemStack stack) {
        StrainInterface stackInterface = ModMisc.STRAIN.get(stack);
        if (stackInterface.identified()) {
            Strain strain = stackInterface.getStrainInfo().strain();
            MinecraftClient client = MinecraftClient.getInstance();
            EntityInterface playerInterface = ModMisc.PLAYER.get(client.player);

            playerInterface.discoverStrain(strain, client);
        }
    }

    public boolean isKnown(Strain strain) {
        return this.allStrains.contains(strain);
    }

    public void validate() {
        this.allStrains.removeIf(strain -> strain.type().equals(StrainMap.Type.UNKNOWN));
    }
}
