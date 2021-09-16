package com.skilles.cannacraft.blocks.machines.generator;

import com.skilles.cannacraft.blocks.machines.MachineBlockEntity;
import com.skilles.cannacraft.registry.BlockEntities;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.Energy;
import team.reborn.energy.EnergySide;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GeneratorEntity extends MachineBlockEntity {

    int burnTime;

    int fuelTime;

    boolean isBurning;

    private static final int OUTPUT_SPEED = 5;

    protected final PropertyDelegate propertyDelegate;

    List<BlockEntity> adjLeachers = new ArrayList<>();

    public GeneratorEntity(BlockPos pos, BlockState state) {
        super(BlockEntities.GENERATOR, pos, state, DefaultedList.ofSize(1, ItemStack.EMPTY));
        this.propertyDelegate = new PropertyDelegate() {
            public int get(int index) {
                return switch (index) {
                    case 0 -> GeneratorEntity.this.burnTime;
                    case 1 -> GeneratorEntity.this.fuelTime;
                    case 2 -> GeneratorEntity.this.powerStored;
                    default -> 0;
                };
            }

            public void set(int index, int value) {
                switch (index) {
                    case 0 -> GeneratorEntity.this.burnTime = value;
                    case 1 -> GeneratorEntity.this.fuelTime = value;
                    case 2 -> GeneratorEntity.this.powerStored = value;
                }

            }

            public int size() {
                return 3;
            }
        };
    }

    public static int getItemBurnTime(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }
        Map<Item, Integer> burnMap = AbstractFurnaceBlockEntity.createFuelTimeMap();
        if (burnMap.containsKey(stack.getItem())) {
            return burnMap.get(stack.getItem()) / 4;
        }
        return 0;
    }

    public static void tick(World world, BlockPos pos, BlockState state, GeneratorEntity blockEntity) {
        if (world == null || world.isClient) return;

        if (blockEntity.powerStored > 0) {
            for (Direction direction : Direction.values()) {
                BlockPos checkPos = pos.offset(direction);
                BlockEntity adjEntity = world.getBlockEntity(checkPos);
                if (adjEntity != null && Energy.valid(adjEntity)) {
                    blockEntity.movePower(adjEntity, direction);
                    blockEntity.adjLeachers.add(adjEntity);
                }
            }
        }
        if (blockEntity.burnTime == 0) blockEntity.isBurning = false;
        if (blockEntity.isBurning) {
            blockEntity.burnTime--;
            blockEntity.addEnergy(10);
        } else if (blockEntity.canCraft(blockEntity.inventory)) {
            blockEntity.craft(blockEntity.inventory);
        }
    }

    private void movePower(BlockEntity target, Direction direction) {
        Energy.of(this)
                .side(direction)
                .into(Energy.of(target).side(direction.getOpposite()))
                .move(OUTPUT_SPEED);
    }

    @Override
    public boolean canCraft(DefaultedList<ItemStack> inventory) {
        ItemStack fuelStack = inventory.get(0);
        return this.burnTime <= 0 && !fuelStack.isEmpty() && getItemBurnTime(fuelStack) > 0;
    }

    @Override
    public int craft(DefaultedList<ItemStack> inventory) {
        ItemStack fuelStack = inventory.get(0);
        int fuelTime = getItemBurnTime(fuelStack);
        this.isBurning = true;
        this.burnTime = this.fuelTime = fuelTime;
        return fuelTime;
    }

    @Override
    public double getMaxInput(EnergySide side) {
        return 0;
    }

    @Override
    public double getMaxOutput(EnergySide side) {
        return OUTPUT_SPEED;
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        return null;
    }
}
