package com.skilles.cannacraft.blocks.machines.strainAnalyzer;

import com.skilles.cannacraft.blocks.machines.MachineBlockEntity;
import com.skilles.cannacraft.registry.ModEntities;
import com.skilles.cannacraft.registry.ModItems;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class StrainAnalyzerEntity extends MachineBlockEntity {


    private final double powerMultiplier = 1; // Energy use multiplier
    private final boolean needsPower = true;
    private final PropertyDelegate propertyDelegate;
    protected static final int timeToProcess = 175;

    public StrainAnalyzerEntity(BlockPos pos, BlockState state) {
        super(ModEntities.STRAIN_ANALYZER_ENTITY, pos, state, DefaultedList.ofSize(2, ItemStack.EMPTY));
        this.propertyDelegate = new PropertyDelegate() {
            @Override
            public int get(int index) {
                switch(index) {
                    case 0:
                        return StrainAnalyzerEntity.this.processingTime;
                    case 1:
                        return StrainAnalyzerEntity.this.powerStored;
                    default:
                        return 0;
                }
            }

            @Override
            public void set(int index, int value) {
                switch(index) {
                    case 0:
                        StrainAnalyzerEntity.this.processingTime = value;
                    case 1:
                        StrainAnalyzerEntity.this.powerStored = value;
                }
            }

            @Override
            public int size() {
                return 2;
            }
        };
    }


    public static void tick(World world, BlockPos pos, BlockState state, StrainAnalyzerEntity blockEntity) {
        if (world == null || world.isClient) return;
        if(isNextTo(world, pos, Blocks.GLOWSTONE) && blockEntity.powerStored < blockEntity.getMaxStoredPower()) {
            blockEntity.addEnergy(2);
            markDirty(world, pos, state);
        }
        if (blockEntity.isWorking()) {
            if (!world.isReceivingRedstonePower(pos)) {
                processTick(blockEntity); // playSound is called here
                state = state.with(StrainAnalyzer.ACTIVE, true);
                world.setBlockState(pos, state, Block.NOTIFY_ALL);
                markDirty(world, pos, state);
            }
            if (canCraft(blockEntity.inventory) && blockEntity.processingTime == timeToProcess) { // when done crafting
                craft(blockEntity.inventory);
                blockEntity.processingTime = 1; // keep working
                markDirty(world, pos, state);
            } else if (!canCraft(blockEntity.inventory)) {
                blockEntity.processingTime = 0;
                markDirty(world, pos, state);
            }
        } else if (canCraft(blockEntity.inventory) && blockEntity.powerStored != 0) { // start if has power
            blockEntity.processingTime = 1;
            markDirty(world, pos, state);
        } else { // when no items or can't craft
            blockEntity.processingTime = 0;
            state = state.with(StrainAnalyzer.ACTIVE, false);
            world.setBlockState(pos, state, Block.NOTIFY_ALL);
            markDirty(world, pos, state);
        }

    }
    private void playSound() {
        World world  = getWorld();
        SoundEvent runSound = SoundEvents.BLOCK_FIRE_AMBIENT;
        assert world != null;
        if(!world.isClient) {
            if(this.processingTime % 25 == 0) {
                if (this.processingTime != timeToProcess) {
                    world.playSound(
                            null, // Player - if non-null, will play sound for every nearby player *except* the specified player
                            pos, // The position of where the sound will come from
                            runSound, // The sound that will play, in this case, the sound the anvil plays when it lands.
                            SoundCategory.BLOCKS, // This determines which of the volume sliders affect this sound
                            0.15f, //Volume multiplier, 1 is normal, 0.5 is half volume, etc
                            0.5f // Pitch multiplier, 1 is normal, 0.5 is half pitch, etc
                    );
                } else {
                    world.playSound(
                            null, // Player - if non-null, will play sound for every nearby player *except* the specified player
                            pos, // The position of where the sound will come from
                            runSound, // The sound that will play, in this case, the sound the anvil plays when it lands.
                            SoundCategory.BLOCKS, // This determines which of the volume sliders affect this sound
                            0.15f, //Volume multiplier, 1 is normal, 0.5 is half volume, etc
                            2f // Pitch multiplier, 1 is normal, 0.5 is half pitch, etc
                    );
                }
            }
        }
    }
    public boolean isWorking() {
        if(needsPower) {
            return processingTime != 0 && powerStored != 0;
        } else {
            return processingTime != 0;
        }
    }
    public static boolean canCraft(DefaultedList<ItemStack> inventory) {
            ItemStack stack = inventory.get(1);
            ItemStack output = inventory.get(0);
            if(stack.equals(ItemStack.EMPTY)) return false;
                if (stack.isOf(ModItems.WEED_SEED) && stack.getCount() >= 1 && stack.hasTag() && !stack.getSubTag("cannacraft:strain").getBoolean("Identified")) {
                    NbtCompound outputTag = output.copy().getSubTag("cannacraft:strain");
                    NbtCompound subTag = stack.copy().getSubTag("cannacraft:strain");
                    if(outputTag == null) return true;
                    //  if unidentified and NBT aligns
                    return subTag.getInt("ID") == outputTag.getInt("ID") && subTag.getInt("THC") == outputTag.getInt("THC");
            }
        return false;
    }
    private static void processTick(StrainAnalyzerEntity blockEntity) {
        blockEntity.processingTime++;
        if(blockEntity.needsPower) blockEntity.useEnergy(1 * blockEntity.powerMultiplier);
        blockEntity.playSound();
    }
    public static void craft(DefaultedList<ItemStack> inventory) {

            ItemStack stack = inventory.get(1);
            NbtCompound tag = stack.getTag().copy();
            ItemStack outputSlot = inventory.get(0);
            ItemStack output = ModItems.WEED_SEED.getDefaultStack();


            if(outputSlot.isEmpty()) {
                NbtCompound strainTag = tag.getCompound("cannacraft:strain").copy();
                strainTag.putBoolean("Identified", true);
                NbtCompound outputTag = new NbtCompound();
                outputTag.put("cannacraft:strain", strainTag);
                output.setTag(outputTag);
                inventory.set(0, output);
            }
            else if (outputSlot.isOf(output.getItem())) {
                outputSlot.increment(1);
            }
            stack.decrement(1);

    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        return new StrainAnalyzerScreenHandler(syncId, inv, this, this.propertyDelegate);
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.putInt("processingTime", this.processingTime);
        nbt.putInt("powerStored", this.powerStored);
        Inventories.writeNbt(nbt, this.inventory);
        return nbt;
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        this.inventory = DefaultedList.ofSize(this.inventory.size(), ItemStack.EMPTY);
        Inventories.readNbt(nbt, this.inventory);
        this.processingTime = nbt.getInt("processingTime");
        this.powerStored = nbt.getInt("powerStored");
    }
    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        return stack.isOf(ModItems.WEED_SEED);
    }
}