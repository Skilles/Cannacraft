package com.skilles.cannacraft.items;

import com.skilles.cannacraft.registry.ModItems;
import com.skilles.cannacraft.registry.ModMisc;
import com.skilles.cannacraft.util.MiscUtil;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import static com.skilles.cannacraft.Cannacraft.log;

public class WeedGrinder extends Item {
    public WeedGrinder(Settings settings) {
        super(settings);
    }
    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        boolean bl = false;
        if(world.isClient()) {
            ItemStack crosshairItem = MiscUtil.getCrosshairItem();
            if(crosshairItem != null && crosshairItem.isOf(ModItems.WEED_BUNDLE)) {
                if(!ModMisc.STRAIN.get(crosshairItem).getStatus().equals(TriState.DEFAULT)) bl = true; //  set to GROUND if DRY TODO: send packet
            }
        }
        log(bl);
        return TypedActionResult.pass(user.getStackInHand(hand));
    }
}
