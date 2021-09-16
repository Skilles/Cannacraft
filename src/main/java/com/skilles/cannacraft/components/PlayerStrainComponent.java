package com.skilles.cannacraft.components;

import com.skilles.cannacraft.Cannacraft;
import com.skilles.cannacraft.misc.Network;
import com.skilles.cannacraft.misc.WeedToast;
import com.skilles.cannacraft.registry.ModMisc;
import com.skilles.cannacraft.strain.Strain;
import com.skilles.cannacraft.strain.StrainInfo;
import com.skilles.cannacraft.strain.cannadex.Cannadex;
import com.skilles.cannacraft.util.StrainUtil;
import dev.onyxstudios.cca.api.v3.component.ComponentV3;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.entity.PlayerComponent;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerStrainComponent implements EntityInterface, ComponentV3, AutoSyncedComponent, PlayerComponent<PlayerStrainComponent> {

    Strain strain; // strain the player is high on

    StrainInfo strainInfo;

    Cannadex cannadex;

    PlayerEntity player;

    private boolean initialized;

    // TODO: add research/progression

    public PlayerStrainComponent(PlayerEntity player) {
        this.player = player;
        this.cannadex = new Cannadex();
    }

    @Override
    public void readFromNbt(NbtCompound tag) {
        if (!this.initialized && tag.contains("Cannadex")) {
            for (NbtElement element : tag.getList("Cannadex", NbtType.STRING)) {
                String name = element.asString();
                Strain strain = StrainUtil.getStrain(name);
                this.cannadex.discoverStrain(strain);
            }
            this.initialized = true;
        }
    }

    @Override
    public void writeToNbt(@NotNull NbtCompound tag) {
        this.cannadex.validate();
        NbtList nbtList = new NbtList();
        for (Strain strain : this.cannadex.getAllStrains()) {
            nbtList.add(NbtString.of(strain.name()));
        }
        tag.put("Cannadex", nbtList);
    }

    @Override
    public void setStrain(int index) {
        strain = StrainUtil.getStrain(index, false);
        ModMisc.PLAYER.sync(this.player);
    }

    @Override
    public Strain getStrain() {
        return strain;
    }

    @Override
    public Cannadex getCannadex() {
        return cannadex;
    }

    @Override
    public void discoverStrain(Strain strain, @Nullable MinecraftClient client) {
        if (!this.isDiscovered(strain)) {
            // Different behavior whether clientside or serverside
            if (client != null) { // Client -> server
                // Sends a toast message to the client
                client.getToastManager().add(new WeedToast(WeedToast.Type.DISCOVER, strain));
                // Ensures discovery is only run on the server
                Network.sendDiscoverPacket(strain);
            } else { // Server -> client
                Network.sendToastPacket((ServerPlayerEntity) this.player, strain, WeedToast.Type.DISCOVER);
            }
            // Need to also add to client Cannadex so no toast spamming
            this.cannadex.discoverStrain(strain);
            Cannacraft.log("Discovered " + client == null ? "Server" : "Client");
        }
    }

    @Override
    public boolean isDiscovered(Strain strain) {
        return this.cannadex.isKnown(strain);
    }

    @Override
    public boolean shouldSyncWith(ServerPlayerEntity player) {
        return this.player == player;
    }

}
