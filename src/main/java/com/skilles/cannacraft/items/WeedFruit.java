package com.skilles.cannacraft.items;

import com.skilles.cannacraft.components.StrainInterface;
import com.skilles.cannacraft.registry.ModMisc;
import com.skilles.cannacraft.strain.GeneticsManager;
import com.skilles.cannacraft.strain.StrainMap;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.Text;
import net.minecraft.world.World;

import java.util.List;

public class WeedFruit extends Item {
    public WeedFruit(Settings settings) {
        super(settings);
    }

    @Override
    public Text getName(ItemStack stack) {
        if (stack.hasTag()) {
            CompoundTag tag = stack.getSubTag("cannacraft:strain");
            if (StrainMap.getStrain(tag.getInt("ID")).type().equals(StrainMap.Type.UNKNOWN)) tag.putInt("ID", 0);
            return tag.getBoolean("Identified") ? Text.of(StrainMap.getStrain(tag.getInt("ID")).name()) : Text.of("Unidentified Cannabis");
        }
        return super.getName(stack);
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        if (stack.hasTag()) {
            int thc = stack.getSubTag("cannacraft:strain").getInt("THC");
            int id = stack.getSubTag("cannacraft:strain").getInt("ID");
            GeneticsManager.applyHigh(user, id, thc);
            if (world.isClient) {
                ItemStack clientStack = user.getStackInHand(user.getActiveHand());
                StrainInterface clientStackInterface = ModMisc.STRAIN.get(clientStack);
                if (!user.isSneaking()) {
                    System.out.println("Strain of held fruit: " + clientStackInterface.getStrain() + " THC: " + clientStackInterface.getThc() + " Identified: " + clientStackInterface.identified());
                } else {
                    System.out.println(clientStack.getTag());
                }
            }
        }
        return super.finishUsing(stack, world, user);
    }

    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        CompoundTag tag = stack.getOrCreateSubTag("cannacraft:strain");
        if (tag != null && tag.contains("ID") && !(tag.getInt("ID") == 0)) { // checks if ID is set to actual strain
            GeneticsManager.appendTooltips(tooltip, tag);
        }
    }
}
