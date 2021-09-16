package com.skilles.cannacraft.blocks.machines.seedCrosser;

import com.skilles.cannacraft.blocks.machines.MachineBlockEntity;
import com.skilles.cannacraft.registry.BlockEntities;
import com.skilles.cannacraft.registry.ModContent;
import com.skilles.cannacraft.strain.Strain;
import com.skilles.cannacraft.util.CrossUtil;
import com.skilles.cannacraft.util.DnaUtil;
import com.skilles.cannacraft.util.StrainUtil;
import com.skilles.cannacraft.util.WeedRegistry;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class SeedCrosserEntity extends MachineBlockEntity {

    private final double powerMultiplier = 1; // Energy use multiplier

    private final boolean needsPower = true;

    protected final PropertyDelegate propertyDelegate;

    protected static final int timeToProcess = 175;

    public SeedCrosserEntity(BlockPos pos, BlockState state) {
        super(BlockEntities.CROSSER, pos, state, DefaultedList.ofSize(3, ItemStack.EMPTY));
        //this.inventory = DefaultedList.ofSize(3, ItemStack.EMPTY);
        this.propertyDelegate = new PropertyDelegate() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> SeedCrosserEntity.this.processingTime;
                    case 1 -> SeedCrosserEntity.this.powerStored;
                    default -> 0;
                };
            }
            @Override
            public void set(int index, int value) {
                switch (index) {
                    case 0 -> SeedCrosserEntity.this.processingTime = value;
                    case 1 -> SeedCrosserEntity.this.powerStored = value;
                }
            }
            @Override
            public int size() {
                return 2;
            }
        };
    }

    @Override
    public void playSound(int flag) {
        World world  = getWorld();
        SoundEvent runSound = SoundEvents.BLOCK_HONEY_BLOCK_SLIDE;
        assert world != null;
        if (!world.isClient) {
            if (this.processingTime % 25 == 0 && flag == 0) {
                world.playSound(
                        null, // Player - if non-null, will play sound for every nearby player *except* the specified player
                        pos, // The position of where the sound will come from
                        runSound, // The sound that will play, in this case, the sound the anvil plays when it lands.
                        SoundCategory.BLOCKS, // This determines which of the volume sliders affect this sound
                        0.15f, //Volume multiplier, 1 is normal, 0.5 is half volume, etc
                        this.processingTime != timeToProcess ? 1f : 2f // Pitch multiplier, 1 is normal, 0.5 is half pitch, etc
                );
            } else if (flag == 1) {
                world.playSound(
                        null,
                        pos,
                        SoundEvents.UI_TOAST_CHALLENGE_COMPLETE,
                        SoundCategory.BLOCKS,
                        0.07f,
                        3f
                );
                // Sends message to nearby players
                for (ServerPlayerEntity player : PlayerLookup.tracking((ServerWorld) world, pos)) {
                    player.sendSystemMessage(Text.of(StrainUtil.getLatestStrain().name() + " has been created!"), Util.NIL_UUID);
                }
            }
        }
    }

    public boolean canCraft(DefaultedList<ItemStack> inventory) {
        ItemStack stack = inventory.get(1);
        ItemStack stack2 = inventory.get(2);
        ItemStack output = inventory.get(0);
        if (stack.equals(ItemStack.EMPTY) || stack2.equals(ItemStack.EMPTY)) return false;
        if (stack.hasNbt() && stack2.hasNbt() && WeedRegistry.isIdentified(stack) && WeedRegistry.isIdentified(stack2)) {
            if (stack.getNbt() == stack2.getNbt()) return false;
            if (output.isEmpty()) return true;

            Strain newStrain = CrossUtil.crossStrains(WeedRegistry.getStrain(stack), WeedRegistry.getStrain(stack2));
            int newThc = CrossUtil.crossThc(WeedRegistry.getThc(stack), WeedRegistry.getThc(stack2));
            return WeedRegistry.getStrain(output) == newStrain && newThc == WeedRegistry.getThc(output);
        }
        return false;
    }

    public int craft(DefaultedList<ItemStack> inventory) {
        int flag = 0; // flag if no new strain was added

        ItemStack stack = inventory.get(1);
        ItemStack stack2 = inventory.get(2);
        ItemStack outputSlot = inventory.get(0);

        ItemStack output = DnaUtil.crossItems(stack, stack2, 1, true).get(0);

        if (outputSlot.isEmpty()) {
            inventory.set(0, output);
            flag = 1;
        } else {
            outputSlot.increment(1);
        }
        stack.decrement(1);
        stack2.decrement(1);
        return flag;
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        return new SeedCrosserScreenHandler(syncId, inv, this, this.propertyDelegate);
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        return stack.isOf(ModContent.WEED_SEED);
    }
}
