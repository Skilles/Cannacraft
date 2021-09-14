package com.skilles.cannacraft.blocks.machines;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.Energy;
import team.reborn.energy.EnergySide;
import team.reborn.energy.EnergyTier;

public abstract class MachineBlockEntity extends BlockEntity implements MachineInterface {
    protected DefaultedList<ItemStack> inventory;
    protected int powerStored;
    protected int processingTime;
    private final boolean needsPower = true;
    private static final int timeToProcess = 175;
    private final int powerMultiplier = 0;

    public MachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, DefaultedList<ItemStack> inventory) {
        super(type, pos, state);
        this.inventory = inventory;
    }
    protected void playSound() { this.playSound(0) ;}
    @Override
    public void playSound(int flag) {
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
    public static void tick(World world, BlockPos pos, BlockState state, MachineBlockEntity blockEntity) {
        if (world == null || world.isClient) return;
        if (isNextTo(world, pos, Blocks.GLOWSTONE) && blockEntity.powerStored < blockEntity.getMaxStoredPower()) {
            blockEntity.addEnergy(2);
            markDirty(world, pos, state);
        }
        if (blockEntity.isWorking()) {
            if (!world.isReceivingRedstonePower(pos)) {
                processTick(blockEntity); // playSound is called here
                state = state.with(MachineBlock.ACTIVE, true);
                world.setBlockState(pos, state, Block.NOTIFY_ALL);
                markDirty(world, pos, state);
            }
            if (blockEntity.canCraft(blockEntity.inventory) && blockEntity.processingTime == timeToProcess) { // when done crafting
                blockEntity.craft(blockEntity.inventory);
                blockEntity.processingTime = 1; // keep working
                markDirty(world, pos, state);
            } else if (!blockEntity.canCraft(blockEntity.inventory)) {
                blockEntity.processingTime = 0;
                markDirty(world, pos, state);
            }
        } else if (blockEntity.canCraft(blockEntity.inventory) && blockEntity.powerStored != 0) { // start if has power
            blockEntity.processingTime = 1;
        } else { // when no items or can't craft
            blockEntity.processingTime = 0;
            state = state.with(MachineBlock.ACTIVE, false);
            world.setBlockState(pos, state, Block.NOTIFY_ALL);
        }
        markDirty(world, pos, state);
    }
    public boolean isWorking() {
        if(needsPower) { // TODO: use solar power if no generators found
            return processingTime != 0 && powerStored != 0;
        } else {
            return processingTime != 0;
        }
    }
    protected static void processTick(MachineBlockEntity blockEntity) {
        blockEntity.processingTime++;
        if(blockEntity.needsPower) blockEntity.useEnergy(blockEntity.powerMultiplier);
        blockEntity.playSound();
    }
    /**
     * Energy & ImplementedInventory
     */
    @Override
    public int[] getAvailableSlots(Direction side) {
        if (side == Direction.DOWN) {
            return new int[] {0};
        } else {
            return new int[] {1};
        }
    }
    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        return false;
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        return dir == Direction.DOWN && slot == 0;
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        //noinspection ConstantConditions
        if (this.world.getBlockEntity(this.pos) != this) {
            return false;
        } else {
            return player.squaredDistanceTo((double)this.pos.getX() + 0.5D, (double)this.pos.getY() + 0.5D, (double)this.pos.getZ() + 0.5D) <= 64.0D;
        }
    }

    @Override
    public Text getDisplayName() {
        return new TranslatableText(getCachedState().getBlock().getTranslationKey());
    }

    @Override
    public double getMaxStoredPower() {
        return 10000;
    }
    @Override
    public double getStored(EnergySide face) {
        return this.powerStored;
    }

    @Override
    public void setStored(double amount) { powerStored = (int) amount; }
    @Override
    public void addEnergy(double amount) {
        setStored(powerStored + amount);
    }
    @Override
    public double getEnergy() {
        return getStored(EnergySide.UNKNOWN);
    }
    @Override
    public void useEnergy(double amount) {
        if (amount > powerStored) amount = powerStored;
        setStored(powerStored - amount);
    }
    @Override
    public void setEnergy(double amount) {
        setStored(amount);
    }
    @Override
    public void sideTransfer(World world, BlockPos pos, BlockEntity blockEntity) {
        for (Direction side : Direction.values()) {
            BlockEntity sideBlockEntity = world.getBlockEntity(pos.offset(side));
            if (sideBlockEntity == null || !Energy.valid(sideBlockEntity)) {
                continue;
            }
            Energy.of(blockEntity)
                    .side(side)
                    .into(Energy.of(sideBlockEntity).side(side.getOpposite()))
                    .move();
        }
    }
    protected static boolean isNextTo(World world, BlockPos pos, Block block) {
        for (Direction side : Direction.values()) {
            Block sideBlock = world.getBlockState(pos.offset(side)).getBlock();
            if (sideBlock == block) {
                return true;
            }
        }
        return false;
    }
    @Override
    public EnergyTier getTier() {
        return EnergyTier.LOW;
    }
    @Override
    public DefaultedList<ItemStack> getItems() {
        return inventory;
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
}
