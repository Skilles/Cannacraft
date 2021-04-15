package com.skilles.cannacraft.items;

import com.skilles.cannacraft.blocks.weedCrop.WeedCrop;
import com.skilles.cannacraft.registry.ModComponents;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AliasedBlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
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
            if(!playerEntity.isSneaking()) {
                ItemStack clientStack = playerEntity.getStackInHand(hand);

                StrainInterface clientStackInterface = ModComponents.STRAIN.get(clientStack);
                System.out.println(ModComponents.STRAIN.get(clientStack).syncTest());
                System.out.println("Strain of held seed: " + clientStackInterface.getStrain() + " NBT: " + clientStackInterface.getStrainNBT() + " Identified: " + clientStackInterface.identified());
            }
        }
        return TypedActionResult.success(playerEntity.getStackInHand(hand));
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        Block block = context.getWorld().getBlockState(context.getBlockPos()).getBlock();

        if(block instanceof WeedCrop && context.getPlayer().isSneaking()) {
                BlockEntity blockEntity = context.getWorld().getBlockEntity(context.getBlockPos());
                NbtCompound tag = blockEntity.toInitialChunkDataNbt();
                blockEntity.writeNbt(tag);
                System.out.println(tag);
        }
        return super.useOnBlock(context);
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        ModComponents.STRAIN.get(stack).sync();
    }
    @Override
    public Text getName(ItemStack stack) {
        NbtCompound tag = stack.getTag();
        Text strainName = new TranslatableText(this.getTranslationKey(stack));
        if(tag != null && tag.contains("Strain")) {
            String strain = tag.getString("Strain");
            strainName = new LiteralText(strain);
        }
        return strainName;
    }
    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        //stack.setTag(strainTag);
        NbtCompound tag = stack.getTag();
        if(tag != null && tag.contains("ID")){

            StrainInterface stackInterface = ModComponents.STRAIN.get(stack);
            //stackInterface.setTag();
            //String strain = ModComponents.STRAIN.get(stack).getStrain();
            //ModComponents.STRAIN.get(stack).setStrain(strain);
            //String type = ModComponents.STRAIN.get(stack).getType();
            //int thc = ModComponents.STRAIN.get(stack).getTHC();

            
            String strain = tag.getString("Strain"); // TODO: string tag is null here
            int thc = tag.getInt("THC");
            String type = tag.getString("Type");
            tooltip.add(new LiteralText("Strain: " + strain));
            tooltip.add(new LiteralText("Type: " + type));
            if(stackInterface.identified()) {
                tooltip.add(new LiteralText("THC: " + thc + "%"));
            }
            //System.out.println("Tooltip updated! strain="+strain);


       }
    }



}
