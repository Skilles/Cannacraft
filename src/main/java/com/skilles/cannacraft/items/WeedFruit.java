package com.skilles.cannacraft.items;

import com.skilles.cannacraft.strain.StrainMap;
import com.skilles.cannacraft.util.MiscUtil;
import com.skilles.cannacraft.util.StrainUtil;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.world.World;

import java.util.List;
@Deprecated
public class WeedFruit extends Item {
    public WeedFruit(Settings settings) {
        super(settings);
    }
    @Override
    public Text getName(ItemStack stack) {
        if (stack.hasNbt()) {
            NbtCompound tag = stack.getSubNbt("cannacraft:strain");
            if(!tag.contains("ID") || StrainUtil.getStrain(tag).type().equals(StrainMap.Type.UNKNOWN)) tag.putInt("ID", 0);
            return tag.getBoolean("Identified") ? Text.of(StrainUtil.getStrain(tag).name()) : Text.of("Unidentified Cannabis");
        }
        return super.getName(stack);
    }

    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        NbtCompound tag = stack.getOrCreateSubNbt("cannacraft:strain");
        if (tag != null && tag.contains("ID") && !(tag.getInt("ID") == 0)) { // checks if ID is set to actual strain
            MiscUtil.appendTooltips(tooltip, tag);
        }
    }
}
