package com.skilles.cannacraft.blocks.machines;

import com.skilles.cannacraft.blocks.ImplementedInventory;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.EnergySide;
import team.reborn.energy.EnergyStorage;
import team.reborn.energy.EnergyTier;

public interface MachineInterface extends EnergyStorage, SidedInventory, NamedScreenHandlerFactory, ImplementedInventory {

    void playSound(int flag);
    boolean isWorking();

    boolean canCraft(DefaultedList<ItemStack> inventory);
    int craft(DefaultedList<ItemStack> inventory);


    @Override
    int[] getAvailableSlots(Direction side);

    @Override
    boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir);

    @Override
    boolean canExtract(int slot, ItemStack stack, Direction dir);

    @Override
    boolean canPlayerUse(PlayerEntity player);

    @Override
    Text getDisplayName();

    @Override
    double getMaxStoredPower();

    @Override
    double getStored(EnergySide face);

    @Override
    void setStored(double amount);

    void addEnergy(double amount);

    double getEnergy();

    void useEnergy(double amount);

    void setEnergy(double amount);

    void sideTransfer(World world, BlockPos pos, BlockEntity blockEntity);

    @Override
    EnergyTier getTier();

    @Override
    DefaultedList<ItemStack> getItems();
}
