package com.skilles.cannacraft.items;

import com.skilles.cannacraft.registry.ModComponents;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.LiteralText;
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
    public String getTranslationKey(ItemStack stack) {
        NbtCompound tag = stack.getSubTag("cannacraft:strain");
        if (tag != null && tag.contains("ID", NbtElement.INT_TYPE)) {
            if(!tag.getBoolean("Identified")) {
                return super.getTranslationKey(stack) + ".unidentified";
            } else {
                int id = tag.getInt("ID");
                return super.getTranslationKey(stack) + "." + id;
            }
        }
        return super.getTranslationKey(stack);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity playerEntity, Hand hand) {
        if(world.isClient) {
            ItemStack clientStack = playerEntity.getStackInHand(hand);
            StrainInterface clientStackInterface = ModComponents.STRAIN.get(clientStack);
            if(!playerEntity.isSneaking()) {
                //System.out.println(ModComponents.STRAIN.get(clientStack).syncTest());
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
        //System.out.println(tag);
        if(tag != null && tag.contains("ID")){ // checks if ID is set to actual strain
            //StrainInterface stackInterface = ModComponents.STRAIN.get(stack);
            if(tag.getBoolean("Identified")) {
                String strain = ModComponents.STRAIN.get(stack).getStrain();
                String type = ModComponents.STRAIN.get(stack).getType();
                int thc = ModComponents.STRAIN.get(stack).getThc();
                tooltip.add(new LiteralText("Strain: " + strain));
                tooltip.add(new LiteralText("Type: " + type));
                tooltip.add(new LiteralText("THC: " + thc + "%"));

            } else {
                tooltip.add(new LiteralText("Strain: Unidentified"));
                tooltip.add(new LiteralText("Type: Unknown"));
            }
            //System.out.println("Tooltip updated! strain="+strain);


        }
    }
}
