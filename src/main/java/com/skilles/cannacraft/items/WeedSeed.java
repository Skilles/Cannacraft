package com.skilles.cannacraft.items;

import com.skilles.cannacraft.Cannacraft;
import com.skilles.cannacraft.blocks.machines.MachineBlock;
import com.skilles.cannacraft.blocks.machines.MachineBlockEntity;
import com.skilles.cannacraft.blocks.weedCrop.WeedCrop;
import com.skilles.cannacraft.strain.cannadex.Cannadex;
import com.skilles.cannacraft.util.MiscUtil;
import com.skilles.cannacraft.util.StrainUtil;
import net.minecraft.block.Block;
import net.minecraft.block.FarmlandBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AliasedBlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.List;

import static com.skilles.cannacraft.Cannacraft.log;

public class WeedSeed extends AliasedBlockItem {

    public WeedSeed(Block block) {
        super(block, new Item.Settings().group(Cannacraft.ITEM_GROUP));
    }


    @Override
    public Text getName(ItemStack stack) {
        if (stack.hasNbt()) {
            return MiscUtil.getItemName(stack);
        }
        return super.getName(stack);
    }
    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity playerEntity, Hand hand) {
        StrainItem.debugAction(world, playerEntity, hand);
        return TypedActionResult.success(playerEntity.getStackInHand(hand));
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        Block block = context.getWorld().getBlockState(context.getBlockPos()).getBlock();
        if (!context.getWorld().isClient()) {
            if (block instanceof MachineBlock) {
                System.out.println("Active: " + context.getWorld().getBlockState(context.getBlockPos()).get(MachineBlock.ACTIVE)
                        + " Facing: " + context.getWorld().getBlockState(context.getBlockPos()).get(MachineBlock.FACING)
                + " Power: " + ((MachineBlockEntity) context.getWorld().getBlockEntity(context.getBlockPos())).getEnergy());
            }
            if (block instanceof WeedCrop) {
                BlockEntity blockEntity = context.getWorld().getBlockEntity(context.getBlockPos());
                NbtCompound tag = blockEntity.writeNbt(new NbtCompound());
                if (context.getPlayer().isSneaking()) {
                    System.out.println("Strain of crop: " + StrainUtil.getStrain(tag).name()
                            + " Identified: " + tag.getBoolean("identified")
                            + " THC: " + tag.getInt("THC"));
                    log(tag);
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
        if (stack.hasNbt()) {
            NbtCompound tag = stack.getSubNbt("cannacraft:strain");
            if (tag.contains("DNA")) {
                Cannadex.clientDiscoverStrain(stack);
                MiscUtil.appendTooltips(tooltip, stack, true);
            }
        }
    }
}
