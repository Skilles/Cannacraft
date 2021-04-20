package com.skilles.cannacraft.items;

import com.skilles.cannacraft.blocks.weedCrop.WeedCrop;
import com.skilles.cannacraft.registry.ModComponents;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AliasedBlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.List;

public class WeedSeed extends AliasedBlockItem {

    public WeedSeed(Block block, Settings settings) {
        super(block, settings);
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
                System.out.println("Strain of held seed: " + clientStackInterface.getStrain() + " THC: " + clientStackInterface.getThc() + " Identified: " + clientStackInterface.identified());
            } else {
                System.out.println(clientStack.getTag());
            }
        }
        return TypedActionResult.success(playerEntity.getStackInHand(hand));
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        Block block = context.getWorld().getBlockState(context.getBlockPos()).getBlock();

        if(block instanceof WeedCrop && context.getWorld().isClient) {
            BlockEntity blockEntity = context.getWorld().getBlockEntity(context.getBlockPos());
            NbtCompound tag = blockEntity.writeNbt(new NbtCompound());
            if(context.getPlayer().isSneaking()) {
                System.out.println("Strain of crop: " + tag.getString("Strain")
                        + " Identified: " + tag.getBoolean("identified")
                        + " THC: " + tag.getInt("THC"));
                System.out.println(tag);
            } else {
                System.out.println("Max Age: " + context.getWorld().getBlockState(context.getBlockPos()).get(WeedCrop.MAXAGE)
                        + " Age: " + context.getWorld().getBlockState(context.getBlockPos()).get(WeedCrop.AGE)
                        + " Mature: " + !context.getWorld().getBlockState(context.getBlockPos()).hasRandomTicks());
            }
        }
        return super.useOnBlock(context);
    }
    private boolean appended = false;
    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        NbtCompound tag = stack.getSubTag("cannacraft:strain");
        //System.out.println(tag);
        if(tag != null && tag.contains("ID") && !(tag.getInt("ID") == ItemStrainComponent.UNKNOWN_ID) && !appended){ // checks if ID is set to actual strain

            StrainInterface stackInterface = ModComponents.STRAIN.get(stack);
            if(stackInterface.identified()) {
                String strain = stackInterface.getStrain();
                String type = stackInterface.getType();
                int thc = stackInterface.getThc();
                tooltip.add(new LiteralText("Strain: " + strain));
                tooltip.add(new LiteralText("Type: " + type));
                tooltip.add(new LiteralText("THC: " + thc + "%"));
            } else {
                tooltip.add(new LiteralText("Strain: Unidentified"));
                tooltip.add(new LiteralText("Type: Unknown"));
            }
            //System.out.println("Tooltip updated! strain="+strain);
        appended = true;


       }
    }



}
