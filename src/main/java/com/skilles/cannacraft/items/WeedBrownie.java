package com.skilles.cannacraft.items;

import com.skilles.cannacraft.util.MiscUtil;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class WeedBrownie extends Item {
    public WeedBrownie(Settings settings) {
        super(settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        if(stack.hasTag()) {
            NbtCompound tag = stack.getSubTag("cannacraft:strain");
            if (tag.contains("ID") && !(tag.getInt("ID") == 0)) {
                MiscUtil.appendTooltips(tooltip, tag);
            }
        }
        super.appendTooltip(stack, world, tooltip, context);
    }
}
