package com.skilles.cannacraft.misc;

import com.skilles.cannacraft.Cannacraft;
import com.skilles.cannacraft.components.EntityInterface;
import com.skilles.cannacraft.registry.ModMisc;
import com.skilles.cannacraft.strain.Strain;
import com.skilles.cannacraft.util.StrainUtil;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class Network {

    final static Identifier DISCOVER_STRAIN = Cannacraft.id("discover_strain");

    final static Identifier SEND_TOAST = Cannacraft.id("send_toast");

    /**
     * Sends a discover packet to the server in order to synchronise discoveries with
     * the client and server. Called ONLY on the client.
     * @param strain the strain to encode into a packet to send to the server
     */
    public static void sendDiscoverPacket(Strain strain) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeInt(strain.id());
        buf.writeBoolean(strain.isResource());

        ClientPlayNetworking.send(DISCOVER_STRAIN, buf);
    }

    /**
     * Sends a send_toast packet that displays a toast
     * @param strain the strain to encode into a packet to send to the server
     */
    public static void sendToastPacket(ServerPlayerEntity player, Strain strain, WeedToast.Type type) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeInt(strain.id());
        buf.writeBoolean(strain.isResource());
        buf.writeInt(type.ordinal());

        ServerPlayNetworking.send(player, SEND_TOAST, buf);
    }



    public static void register() {
        // Discover strain
        ServerPlayNetworking.registerGlobalReceiver(DISCOVER_STRAIN, (server,  player, handler, buf, responseSender) -> {
            // Read packet data on the event loop
            int id = buf.readInt();
            boolean resource = buf.readBoolean();

            server.execute(() -> {
                // Everything in this lambda is run on the server thread
                EntityInterface playerInterface = ModMisc.PLAYER.get(player);
                Strain strain = StrainUtil.getStrain(id, resource);
                playerInterface.getCannadex().discoverStrain(strain);
            });
        });

        // Send toast
        ClientPlayNetworking.registerGlobalReceiver(SEND_TOAST, (client, handler, buf, responseSender) -> {
            // Read packet data on the event loop
            int id = buf.readInt();
            boolean resource = buf.readBoolean();
            int typeId = buf.readInt();

            client.execute(() -> {
                // Everything in this lambda is run on the client thread
                Strain strain = StrainUtil.getStrain(id, resource);
                // TODO implement typeId
                client.getToastManager().add(new WeedToast(WeedToast.Type.DISCOVER, strain));
            });
        });
    }

}
