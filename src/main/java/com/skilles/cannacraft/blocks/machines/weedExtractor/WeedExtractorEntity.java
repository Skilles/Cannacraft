package com.skilles.cannacraft.blocks.machines.weedExtractor;

import com.skilles.cannacraft.blocks.machines.MachineBlock;
import com.skilles.cannacraft.blocks.machines.MachineBlockEntity;
import com.skilles.cannacraft.registry.ModEntities;
import com.skilles.cannacraft.registry.ModItems;
import com.skilles.cannacraft.util.StrainUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class WeedExtractorEntity extends MachineBlockEntity {


    private final double powerMultiplier = 1; // Energy use multiplier
    private final boolean needsPower = true;
    private final PropertyDelegate propertyDelegate;
    protected static final int timeToProcess = 175;

    public WeedExtractorEntity(BlockPos pos, BlockState state) {
        super(ModEntities.WEED_EXTRACTOR_ENTITY, pos, state, DefaultedList.ofSize(2, ItemStack.EMPTY));
        this.propertyDelegate = new PropertyDelegate() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> WeedExtractorEntity.this.processingTime;
                    case 1 -> WeedExtractorEntity.this.powerStored;
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                switch(index) {
                    case 0:
                        WeedExtractorEntity.this.processingTime = value;
                    case 1:
                        WeedExtractorEntity.this.powerStored = value;
                }
            }

            @Override
            public int size() {
                return 2;
            }
        };
    }
    public static void tick(World world, BlockPos pos, BlockState state, WeedExtractorEntity blockEntity) {
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
        } else { // when no items or can't craft
            blockEntity.processingTime = 0;
            state = state.with(MachineBlock.ACTIVE, false);
            world.setBlockState(pos, state, Block.NOTIFY_ALL);
        }
        markDirty(world, pos, state);
    }
    public static boolean canCraft(DefaultedList<ItemStack> inventory) {
            ItemStack stack = inventory.get(1);
            ItemStack output = inventory.get(0);
            if(stack.equals(ItemStack.EMPTY)) return false;
                if (stack.isOf(ModItems.WEED_BUNDLE) && stack.getCount() >= 1 && stack.hasTag() && stack.getSubTag("cannacraft:strain").getFloat("Status") == 1.0F) {
                    if(output.equals(ItemStack.EMPTY)) return true;
                    return StrainUtil.getOutputStack(stack).isItemEqual(output);
            }
        return false;
    }
    protected static int craft(DefaultedList<ItemStack> inventory) {
        ItemStack stack = inventory.get(1);
        ItemStack outputSlot = inventory.get(0);
        ItemStack output = StrainUtil.getOutputStack(stack);
        if(outputSlot.isEmpty()) {
            inventory.set(0, output);
        }
        else if (outputSlot.isOf(output.getItem())) {
            outputSlot.increment(1);
        }
        stack.decrement(1);
        return 0;
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        return new WeedExtractorScreenHandler(syncId, inv, this, this.propertyDelegate);
    }
    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        return stack.isOf(ModItems.WEED_BUNDLE);
    }
}
