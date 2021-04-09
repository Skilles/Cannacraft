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
    StrainType strain = new StrainType();
    public int strainType = 0;
    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        CompoundTag compoundTag2 = new CompoundTag();
        strain.setStrain(strainType);

        CompoundTag compoundTag = strain.strainTag(compoundTag2);
        stack.setTag(compoundTag);
        if (compoundTag != null && compoundTag.contains("Strain") ){
            int thc = compoundTag.getInt("THC");
            String type = compoundTag.getString("Type");
            String strain = compoundTag.getString("Strain");
            tooltip.add(new LiteralText("Strain: " + strain));
            tooltip.add(new LiteralText("Type: " + type));
            tooltip.add(new LiteralText("THC: " + thc+ "%"));

       }
    }
}
