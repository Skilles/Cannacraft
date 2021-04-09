package com.skilles.cannacraft.items;

import com.skilles.cannacraft.StrainType;
import net.minecraft.block.Block;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.AliasedBlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.world.World;

import java.util.Collection;
import java.util.List;

public class Seed extends AliasedBlockItem {
    public Seed(Block block, Settings settings) {
        super(block, settings);
    }
    /*@Override
    public String getTranslationKey(ItemStack stack) {
        CompoundTag compoundTag = stack.getTag();
        if (compoundTag != null && compoundTag.contains("Strain", 8)) {
            String strain = compoundTag.getString("Strain");
            return super.getTranslationKey(stack) + "." + strain;
        }
        return super.getTranslationKey(stack);
    }*/
    public StrainType strain = new StrainType();
    public int strainType = 3; // default strain type
    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        CompoundTag compoundTag = new CompoundTag();
        strain.setStrain(strainType);
        CompoundTag strainTag = strain.strainTag(compoundTag);
        stack.setTag(strainTag);
        if (strainTag != null && strainTag.contains("Strain") ){
            int thc = strainTag.getInt("THC");
            String type = strainTag.getString("Type");
            String strain = strainTag.getString("Strain");
            tooltip.add(new LiteralText("Strain: " + strain));
            tooltip.add(new LiteralText("Type: " + type));
            tooltip.add(new LiteralText("THC: " + thc+ "%"));
            //System.out.println("Tooltip updated!");

       }
    }
}
