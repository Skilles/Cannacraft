package com.skilles.cannacraft.items.weedManual;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class WeedManual extends Item {
    public WeedManual(Settings settings) {
        super(settings);
    }
    @Override
    public TypedActionResult<ItemStack> use(final World world, final PlayerEntity player,
                                            final Hand hand) {
        if (world.isClient) {
            openGui(player);
        }
        return new TypedActionResult<>(ActionResult.SUCCESS, player.getStackInHand(hand));
    }

    @Environment(EnvType.CLIENT)
    private void openGui(PlayerEntity playerEntity) {
        MinecraftClient.getInstance().openScreen(new ManualGui(playerEntity));
    }
}
