package com.skilles.cannacraft.blocks.machines;

import com.skilles.cannacraft.blocks.ImplementedInventory;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.Energy;
import team.reborn.energy.EnergySide;
import team.reborn.energy.EnergyStorage;
import team.reborn.energy.EnergyTier;

public abstract class MachineBlockEntity extends BlockEntity implements EnergyStorage, SidedInventory, NamedScreenHandlerFactory, ImplementedInventory {
    protected DefaultedList<ItemStack> inventory;
    protected int powerStored;
    protected int processingTime;

    public MachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, DefaultedList<ItemStack> inventory) {
        super(type, pos, state);
        this.inventory = inventory;
    }

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
    public void addEnergy(double amount) {
        setStored(powerStored + amount);
    }
    public double getEnergy() {
        return getStored(EnergySide.UNKNOWN);
    }
    public void useEnergy(double amount) {
        if (amount > powerStored) amount = powerStored;
        setStored(powerStored - amount);
    }
    public void setEnergy(double amount) {
        setStored(amount);
    }
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
}
