package com.skilles.cannacraft.blocks.machines.strainAnalyzer;

import com.skilles.cannacraft.blocks.MachineBlockEntity;
import com.skilles.cannacraft.registry.ModEntities;
import com.skilles.cannacraft.registry.ModItems;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.Energy;
import team.reborn.energy.EnergySide;
import team.reborn.energy.EnergyTier;

public class StrainAnalyzerEntity extends MachineBlockEntity {


    private final double powerMultiplier = 1; // Energy use multiplier
    private final boolean needsPower = true;
    private final PropertyDelegate propertyDelegate;

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
        }
        if (blockEntity.isWorking()) {
            if (!world.isReceivingRedstonePower(pos)) {
                processTick(blockEntity);
            }
            if (canCraft(blockEntity.inventory) && blockEntity.processingTime == 184) {
                craft(blockEntity.inventory);
                blockEntity.processingTime = 1;
                //blockEntity.propertyDelegate.set(0, 1);
                markDirty(world, pos, state);
            } else if (!canCraft(blockEntity.inventory)) {
                blockEntity.processingTime = 0;
                markDirty(world, pos, state);
            }

            state = state.with(StrainAnalyzer.ACTIVE, true);
            world.setBlockState(pos, state, Block.NOTIFY_ALL);
            markDirty(world, pos, state);

        } else if (canCraft(blockEntity.inventory)) {
            blockEntity.processingTime = 1;
            markDirty(world, pos, state);
        }

        if (!blockEntity.isWorking() && !canCraft(blockEntity.inventory)) {
            state = state.with(StrainAnalyzer.ACTIVE, false);
            world.setBlockState(pos, state, Block.NOTIFY_ALL);
            markDirty(world, pos, state);
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
