package com.skilles.cannacraft.items;

import com.skilles.cannacraft.registry.ModComponents;
import com.skilles.cannacraft.strain.GeneticsManager;
import com.skilles.cannacraft.strain.StrainMap;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.List;

public class WeedFruit extends Item {
    public WeedFruit(Settings settings) {
        super(settings);
    }
    @Override
    public Text getName(ItemStack stack) {
        if (stack.hasTag()) {
            NbtCompound tag = stack.getSubTag("cannacraft:strain");
            return tag.getBoolean("Identified") ? Text.of(StrainMap.getStrain(tag.getInt("ID")).name()) : Text.of("Unidentified Cannabis");
        }
        return super.getName(stack);
    }
    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity playerEntity, Hand hand) {
        if(world.isClient) {
            ItemStack clientStack = playerEntity.getStackInHand(hand);
            StrainInterface clientStackInterface = ModComponents.STRAIN.get(clientStack);
            if(!playerEntity.isSneaking()) {
                System.out.println("Strain of held fruit: " + clientStackInterface.getStrain() + " THC: " + clientStackInterface.getThc() + " Identified: " + clientStackInterface.identified());
            } else {
                System.out.println(clientStack.getTag());
            }
        }
        return TypedActionResult.success(playerEntity.getStackInHand(hand));
    }
    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        NbtCompound tag = stack.getOrCreateSubTag("cannacraft:strain");
        if (tag != null && tag.contains("ID") && !(tag.getInt("ID") == 0)) { // checks if ID is set to actual strain
            GeneticsManager.appendTooltips(tooltip, tag);
        }
    }
}
