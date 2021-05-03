package com.skilles.cannacraft.items;

import com.skilles.cannacraft.blocks.machines.strainAnalyzer.StrainAnalyzer;
import com.skilles.cannacraft.blocks.machines.strainAnalyzer.StrainAnalyzerEntity;
import com.skilles.cannacraft.blocks.weedCrop.WeedCrop;
import com.skilles.cannacraft.components.StrainInterface;
import com.skilles.cannacraft.registry.ModMisc;
import com.skilles.cannacraft.strain.GeneticsManager;
import com.skilles.cannacraft.strain.StrainMap;
import net.minecraft.block.Block;
import net.minecraft.block.FarmlandBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AliasedBlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
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
    public Text getName(ItemStack stack) {
        if (stack.hasTag()) {
            NbtCompound tag = stack.getSubTag("cannacraft:strain");
            if(StrainMap.getStrain(tag.getInt("ID")).type().equals(StrainMap.Type.UNKNOWN)) tag.putInt("ID", 0);
            return tag.getBoolean("Identified") ? Text.of(StrainMap.getStrain(tag.getInt("ID")).name() + " Seeds") : Text.of("Unidentified Seeds");
        }
        return super.getName(stack);
    }
    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity playerEntity, Hand hand) {
        if (world.isClient) {
            ItemStack clientStack = playerEntity.getStackInHand(hand);
            StrainInterface clientStackInterface = ModMisc.STRAIN.get(clientStack);
            if (!playerEntity.isSneaking()) {
                System.out.println("Strain of held seed: " + clientStackInterface.getStrain() + " THC: " + clientStackInterface.getThc() + " Identified: " + clientStackInterface.identified() + " Genes: " + clientStackInterface.getGenetics());
            } else {
                System.out.println(clientStack.getTag());
            }
        }
        return TypedActionResult.success(playerEntity.getStackInHand(hand));
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        Block block = context.getWorld().getBlockState(context.getBlockPos()).getBlock();
        if (!context.getWorld().isClient()) {
            if (block instanceof StrainAnalyzer) {
                System.out.println("Active: " + context.getWorld().getBlockState(context.getBlockPos()).get(StrainAnalyzer.ACTIVE)
                        + " Facing: " + context.getWorld().getBlockState(context.getBlockPos()).get(StrainAnalyzer.FACING)
                + " Power: " + ((StrainAnalyzerEntity) context.getWorld().getBlockEntity(context.getBlockPos())).getEnergy());
            }
            if (block instanceof WeedCrop) {
                BlockEntity blockEntity = context.getWorld().getBlockEntity(context.getBlockPos());
                NbtCompound tag = blockEntity.writeNbt(new NbtCompound());
                if (context.getPlayer().isSneaking()) {
                    System.out.println("Strain of crop: " + StrainMap.getStrain(tag.getInt("ID")).name()
                            + " Identified: " + tag.getBoolean("identified")
                            + " THC: " + tag.getInt("THC"));
                    System.out.println(tag);
                } else {
                    System.out.println("Max Age: " + context.getWorld().getBlockState(context.getBlockPos()).get(WeedCrop.MAXAGE)
                            + " Age: " + context.getWorld().getBlockState(context.getBlockPos()).get(WeedCrop.AGE)
                            + " Mature: " + !context.getWorld().getBlockState(context.getBlockPos()).hasRandomTicks()
                            + " Breeding: " + context.getWorld().getBlockState(context.getBlockPos()).get(WeedCrop.BREEDING));
                }
            }
        }
        if (!(block instanceof FarmlandBlock)) {
            return ActionResult.FAIL;
        }
        return super.useOnBlock(context);
    }
    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        NbtCompound tag = stack.getSubTag("cannacraft:strain");
        if (tag != null && tag.contains("ID") && !(tag.getInt("ID") == 0)) { // checks if ID is set to actual strain
            GeneticsManager.appendTooltips(tooltip, tag);
        }


    }
}
