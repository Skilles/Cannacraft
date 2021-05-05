package com.skilles.cannacraft.items;

import com.skilles.cannacraft.strain.GeneticsManager;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.Util;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

public class WeedJoint extends BowItem {
    public WeedJoint(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        ItemStack offhandStack = user.getOffHandStack();
        NbtCompound tag = itemStack.getOrCreateTag();
        if(offhandStack.isOf(Items.FLINT_AND_STEEL) || user.getMainHandStack().isOf(Items.FLINT_AND_STEEL)) {
            tag.putBoolean("Lit", true);
            user.getInventory().insertStack(user.getOffHandStack());
            user.getOffHandStack().decrement(1);
            return TypedActionResult.success(itemStack, true);
        }
        if(tag.getBoolean("Lit")) {
            user.setCurrentHand(hand);
            return TypedActionResult.consume(itemStack);
        } else {
            return TypedActionResult.fail(itemStack);
        }
    }

    @Override
    public int getEnchantability() {
        return 0;
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return stack.getOrCreateTag().getBoolean("Lit");
    }

    @Override
    public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        NbtCompound tag = stack.getOrCreateTag();
        if(user instanceof PlayerEntity && tag.getBoolean("Lit")) {
            if (remainingUseTicks == 1) {
                if (tag.contains("cannacraft:strain")) {
                    if (!world.isClient) {
                        GeneticsManager.applyHigh(user);
                        stack.damage(1, user, (p) -> {
                            p.sendToolBreakStatus(user.getActiveHand());
                        });
                    }
                    //((PlayerEntity) user).incrementStat(Stats.CUSTOM.getOrCreateStat(new Identifier("text.cannacraft.stat.joint")));
                } else if (!world.isClient) {
                    user.sendSystemMessage(Text.of("...what did you pack in here?"), Util.NIL_UUID);
                }
            }
        }
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        if(stack.getTag().contains("cannacraft:strain")) {
            NbtCompound tag = stack.getSubTag("cannacraft:strain");
            if (tag.contains("ID") && !(tag.getInt("ID") == 0)) {
                GeneticsManager.appendTooltips(tooltip, tag);
            }
        }
    }

    @Override
    public Predicate<ItemStack> getHeldProjectiles() {
        return super.getHeldProjectiles();
    }

    @Override
    public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        if(remainingUseTicks == 1) {
            user.stopUsingItem();
        }
        super.usageTick(world, user, stack, remainingUseTicks);
    }

    @Override
    public int getMaxUseTime(ItemStack stack) {
        return 50;
    }
}
