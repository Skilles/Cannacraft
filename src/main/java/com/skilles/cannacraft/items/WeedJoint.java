package com.skilles.cannacraft.items;

import com.skilles.cannacraft.util.HighUtil;
import com.skilles.cannacraft.util.MiscUtil;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;
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
    private boolean isLit(ItemStack stack) {
        return stack.hasTag() && stack.getTag().getBoolean("Lit");
    }
    // TODO: add left hand & fix bodyYaw mismatch
    private Vec3d itemVector(PlayerEntity player, int flag) {
        Vec3d look = flag == 0 ? player.getRotationVector() : Vec3d.fromPolar(new Vec2f(player.getPitch(), player.bodyYaw)); // multiplayer offset
        Vec3d playerPos = player.getPos().add(0, player.getHeight(), 0);
        //The next 3 variables are directions on the screen relative to the players look direction. So right = to the right of the player, regardless of facing direction.
        Vec3d right = new Vec3d(-look.z, 0, look.x).normalize();
        Vec3d forward = look;
        Vec3d down = right.crossProduct(forward);

        //These are used to calculate where the particles are going.
        right = right.multiply(0.65f);
        forward = forward.multiply(0.85f);
        down = down.multiply(-0.40);

        //Height modifiers
        double y = player.getY() + 1.2D;
        if(player.getPose().equals(EntityPose.CROUCHING)) y -= 0.5D;
        if(player.getPose().equals(EntityPose.SWIMMING)) y -= 1.0D;

        Vec3d laserPos = playerPos;
        //Multiplayer offsets
        if(flag != 0) {
            right = new Vec3d(-0.7F * MathHelper.sin(-(player.bodyYaw + 30) * 0.017453292F - (float) Math.PI),0,0);
            forward = new Vec3d(0, 0, -0.7F * MathHelper.cos(-(player.bodyYaw + 30) * 0.017453292F - (float) Math.PI));
            down = Vec3d.ZERO;
            laserPos = laserPos.multiply(1, 0, 1);
            laserPos = laserPos.add(0, y, 0);
        }
        //Take the player's eye position, and shift it to where the end of the item is
        laserPos = laserPos.add(right);
        laserPos = laserPos.add(down);
        laserPos = laserPos.add(forward);
        return laserPos;
    }
    private void spawnSmoke(Entity entity) {
        if (entity instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) entity;
            if (!player.world.isClient()) {
                Vec3d laserPos = itemVector(player, 1);
                // Send packets to other players
                for (ServerPlayerEntity serverPlayer : PlayerLookup.tracking(player)) {
                    serverPlayer.networkHandler.sendPacket(
                            new ParticleS2CPacket(ParticleTypes.SMOKE, false,
                                    laserPos.x,
                                    laserPos.y,
                                    laserPos.z,
                                    0.0F, 0.0F, 0.0F, 0.0F, 1));
                }
            } else {
                Vec3d laserPos;
                MinecraftClient client = MinecraftClient.getInstance();
                if (client.options.getPerspective().isFirstPerson() && !client.options.hudHidden) { // using vectors
                    laserPos = itemVector(player, 0);
                } else {
                    laserPos = itemVector(player, 1);
                }
                client.world.addParticle(ParticleTypes.SMOKE,
                        laserPos.x,
                        laserPos.y,
                        laserPos.z,
                        0.0F, 0.0F, 0.0F);
            }
        }
    }
    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        // TODO: add lifespan
        Random random = new Random();
            if (selected && isLit(stack)) {
                if(!((LivingEntity) entity).isUsingItem()) {
                    if (random.nextInt(60) == 0) {
                        BlockPos pos = entity.getBlockPos();
                        world.playSound((double) pos.getX() + 0.5D, (double) pos.getY() + 0.5D, (double) pos.getZ() + 0.5D, SoundEvents.BLOCK_CAMPFIRE_CRACKLE, SoundCategory.BLOCKS, 0.1F + random.nextFloat(), random.nextFloat() * 0.7F + 2.6F, true);
                    }
                    if (random.nextInt(2) == 0) {
                        for (int i = 0; i < random.nextInt(2) + 1; ++i) {
                            spawnSmoke(entity);
                        }
                    }
                } else {
                    BlockPos pos = entity.getBlockPos();
                    if(random.nextBoolean()) {
                        world.playSound((double) pos.getX() + 0.5D, (double) pos.getY() + 0.5D, (double) pos.getZ() + 0.5D, SoundEvents.BLOCK_FIRE_AMBIENT, SoundCategory.BLOCKS, 0.35F + random.nextFloat(), random.nextFloat() * 0.7F + 1.6F, true);
                    }
                }
            }
        super.inventoryTick(stack, world, entity, slot, selected);
    }

    @Override
    public int getEnchantability() {
        return 0;
    }

    @Override
    public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        NbtCompound tag = stack.getOrCreateTag();
        if(user instanceof PlayerEntity && isLit(stack)) {
            if (remainingUseTicks == 1) {
                if (tag.contains("cannacraft:strain")) {
                    if (!world.isClient) {
                        HighUtil.applyHigh(user);
                        stack.damage(1, user, (p) -> {
                            p.sendToolBreakStatus(user.getActiveHand());
                        });
                    }
                    //((PlayerEntity) user).incrementStat(Stats.CUSTOM.getOrCreateStat(new Identifier("text.cannacraft.stat.joint")));
                } else if (!world.isClient) {
                    user.sendSystemMessage(Text.of("...what did you pack in here?"), Util.NIL_UUID);
                }
            }
            Random random = new Random();
            BlockPos pos = user.getBlockPos().up(1);
            for(int i = 0; i < random.nextInt(2) + (getMaxUseTime(stack) - remainingUseTicks)/5; ++i) {
                world.addImportantParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, true, (double)pos.getX() + 0.5D + random.nextDouble() / 3.0D * (double)(random.nextBoolean() ? 1 : -1), (double)pos.getY() + random.nextDouble() + random.nextDouble(), (double)pos.getZ() + 0.5D + random.nextDouble() / 3.0D * (double)(random.nextBoolean() ? 1 : -1), 0.0D, 0.07D, 0.0D);
            }
        }
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        if(stack.getTag().contains("cannacraft:strain")) {
            NbtCompound tag = stack.getSubTag("cannacraft:strain");
            if (tag.contains("ID") && !(tag.getInt("ID") == 0)) {
                MiscUtil.appendTooltips(tooltip, tag);
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
