package com.skilles.cannacraft.items;

import com.skilles.cannacraft.Cannacraft;
import com.skilles.cannacraft.blocks.weedCrop.WeedCropEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import vazkii.patchouli.api.PatchouliAPI;

import java.util.List;

import static com.skilles.cannacraft.Cannacraft.id;
import static com.skilles.cannacraft.Cannacraft.log;

public class WeedManual extends Item {
    public WeedManual() {
        super(new Item.Settings().group(Cannacraft.ITEM_GROUP).maxCount(1).fireproof());
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (world.isClient) {
            PatchouliAPI.get().openBookGUI(id("manual"));
        }
        return super.use(world, user, hand);
    }

    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(new TranslatableText("cannacraft.manual.tooltip").formatted(Formatting.DARK_GRAY));
        super.appendTooltip(stack, world, tooltip, context);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        //BlockState blockState = world.getBlockState(context.getBlockPos());
        BlockEntity blockEntity = world.getBlockEntity(context.getBlockPos());
        if (blockEntity instanceof WeedCropEntity) {
            NbtCompound tag = blockEntity.writeNbt(new NbtCompound());
            log(tag.asString());
            return ActionResult.SUCCESS;
        }
        return super.useOnBlock(context);
    }
}
