package com.skilles.cannacraft.items;

import com.skilles.cannacraft.StrainType;
import com.skilles.cannacraft.registry.ModComponents;
import com.sun.org.apache.xpath.internal.operations.Mod;
import net.minecraft.block.Block;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AliasedBlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.List;

public class Seed extends AliasedBlockItem {
    //CompoundTag strainTag;
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
    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity playerEntity, Hand hand) {

        if(world.isClient) {
            String strain = ModComponents.STRAIN.get(playerEntity.getStackInHand(hand)).getStrain();
            System.out.println("Strain of held seed: " + strain+" NBT: "+ModComponents.STRAIN.get(playerEntity.getStackInHand(hand)).getStrain2());
        }
        return TypedActionResult.success(playerEntity.getStackInHand(hand));
    }
    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        ModComponents.STRAIN.get(stack).readNbt();
    }
    @Override
    public Text getName(ItemStack stack) {
        ModComponents.STRAIN.get(stack).readNbt();
        CompoundTag tag = stack.getTag();
        if(tag != null) {
            String strain = ModComponents.STRAIN.get(stack).getStrain();
            stack.setCustomName(new LiteralText(strain));
        }
        return new TranslatableText(this.getTranslationKey(stack));
    }
    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        //stack.setTag(strainTag);
        CompoundTag tag = stack.getTag();
        if(tag != null){

            //String strain2 = tag.getString("Strain");
            ModComponents.STRAIN.get(stack).readNbt();
            String strain = ModComponents.STRAIN.get(stack).getStrain();
            //ModComponents.STRAIN.get(stack).setStrain(strain);
            String type = ModComponents.STRAIN.get(stack).getType();
            int thc = ModComponents.STRAIN.get(stack).getTHC();


            //CompoundTag strainTag = ModComponents.STRAIN.get(stack).setTag(stack);
            //String strain = strainTag.getString("Strain");
            //int thc = strainTag.getInt("THC");
            //String type = strainTag.getString("Type");
            tooltip.add(new LiteralText("Strain: " + strain));
            tooltip.add(new LiteralText("Type: " + type));
            tooltip.add(new LiteralText("THC: " + thc + "%"));
            stack.setCustomName(new LiteralText(strain));
            //System.out.println("Tooltip updated! strain="+strain);


       }
    }



}
