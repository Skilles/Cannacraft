package com.skilles.cannacraft.blocks.machines.strainAnalyzer;

import com.skilles.cannacraft.blocks.machines.MachineBlockEntity;
import com.skilles.cannacraft.registry.BlockEntities;
import com.skilles.cannacraft.registry.ModContent;
import com.skilles.cannacraft.util.WeedRegistry;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

public class StrainAnalyzerEntity extends MachineBlockEntity {

    private final double powerMultiplier = 1; // Energy use multiplier

    private final boolean needsPower = true;

    private final PropertyDelegate propertyDelegate;

    protected static final int timeToProcess = 175;

    public StrainAnalyzerEntity(BlockPos pos, BlockState state) {
        super(BlockEntities.ANALYZER, pos, state, DefaultedList.ofSize(2, ItemStack.EMPTY));
        this.propertyDelegate = new PropertyDelegate() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> StrainAnalyzerEntity.this.processingTime;
                    case 1 -> StrainAnalyzerEntity.this.powerStored;
                    default -> 0;
                };
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

    public boolean canCraft(DefaultedList<ItemStack> inventory) {
        ItemStack stack = inventory.get(1);
        ItemStack output = inventory.get(0);
        if (stack.equals(ItemStack.EMPTY)) return false;
        if (output.equals(ItemStack.EMPTY)) return true;
        if (WeedRegistry.checkItem(stack) && stack.getCount() >= 1 && stack.hasNbt() && !WeedRegistry.isIdentified(stack)) {
            NbtCompound outputTag = output.copy().getSubNbt("cannacraft:strain");
            NbtCompound subTag = stack.copy().getSubNbt("cannacraft:strain");
            //  if unidentified and NBT aligns
            return subTag.getInt("ID") == outputTag.getInt("ID") && subTag.getInt("THC") == outputTag.getInt("THC") && subTag.getList("Attributes", NbtType.COMPOUND) == outputTag.getList("Attributes", NbtType.COMPOUND);
        }
        return false;
    }

    public int craft(DefaultedList<ItemStack> inventory) {
        ItemStack stack = inventory.get(1);
        ItemStack outputSlot = inventory.get(0);
        ItemStack output = ModContent.WEED_SEED.getDefaultStack();

        if (outputSlot.isEmpty()) {
            NbtCompound strainTag = stack.getSubNbt("cannacraft:strain").copy();
            strainTag.putBoolean("Identified", true);
            output.setSubNbt("cannacraft:strain", strainTag);
            inventory.set(0, output);
        } else if (outputSlot.isOf(output.getItem())) {
            outputSlot.increment(1);
        }
        stack.decrement(1);
        return 0;
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        return new StrainAnalyzerScreenHandler(syncId, inv, this, this.propertyDelegate);
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        return stack.isOf(ModContent.WEED_BUNDLE);
    }
}
