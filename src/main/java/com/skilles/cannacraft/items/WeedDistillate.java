package com.skilles.cannacraft.items;

import com.skilles.cannacraft.Cannacraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import static com.skilles.cannacraft.Cannacraft.log;

public class WeedDistillate extends StrainItem {
    public WeedDistillate() {
        super(new Item.Settings().group(Cannacraft.ITEM_GROUP));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity playerEntity, Hand hand) {
        if (world.isClient) {
            ItemStack clientStack = playerEntity.getStackInHand(hand);
            if (!playerEntity.isSneaking()) {
                StrainItem.debugAction(world, playerEntity, hand);
            } else {
                log(clientStack.getNbt());
            }
        }
        return TypedActionResult.pass(playerEntity.getStackInHand(hand));
    }
}
