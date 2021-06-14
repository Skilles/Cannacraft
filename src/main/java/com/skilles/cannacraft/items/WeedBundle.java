package com.skilles.cannacraft.items;

import com.skilles.cannacraft.registry.ModMisc;
import com.skilles.cannacraft.strain.StrainMap;
import com.skilles.cannacraft.util.BundleUtil;
import com.skilles.cannacraft.util.StrainUtil;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class WeedBundle extends Item {
    public WeedBundle(Settings settings) {
        super(settings);
    }
    @Override
    public Text getName(ItemStack stack) {
        if (stack.hasTag()) {
            NbtCompound strainTag = stack.getOrCreateSubTag("cannacraft:strain");
            String name = BundleUtil.getName(stack);
            if (!strainTag.contains("ID") || StrainUtil.getStrain(strainTag).type().equals(StrainMap.Type.UNKNOWN))
                strainTag.putInt("ID", 0);
            if (strainTag.getBoolean("Identified")) {
                name += StrainUtil.getStrain(strainTag.getInt("ID")).name();
            } else {
                name += "Unidentified Cannabis";
            }
            if(strainTag.contains("Status")) {
                if(strainTag.getInt("Status") == 1) {
                    StringUtils.prependIfMissing(name, "Ground ");
                } else if(strainTag.getInt("Status") == 2) {
                    StringUtils.prependIfMissing(name, "Wet ");
                }
            }
            return Text.of(name);
        }
        return super.getName(stack);
    }

    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        int count = stack.getCount();
        BundleUtil.appendBundleTooltip(tooltip, count);
    }
}
