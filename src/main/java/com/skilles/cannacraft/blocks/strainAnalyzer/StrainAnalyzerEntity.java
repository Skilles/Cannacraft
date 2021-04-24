package com.skilles.cannacraft.blocks.strainAnalyzer;

import com.skilles.cannacraft.registry.ModEntities;
import com.skilles.cannacraft.registry.ModItems;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class StrainAnalyzerEntity extends BlockEntity implements SidedInventory, NamedScreenHandlerFactory {

    private int processingTime;
    public DefaultedList<ItemStack> inventory;
    private final PropertyDelegate propertyDelegate;

    public StrainAnalyzerEntity(BlockPos pos, BlockState state) {
        super(ModEntities.STRAIN_ANALYZER_ENTITY, pos, state);
        this.inventory = DefaultedList.ofSize(2, ItemStack.EMPTY);
        this.propertyDelegate = new PropertyDelegate() {
            @Override
            public int get(int index) {
                return StrainAnalyzerEntity.this.processingTime;
            }

            @Override
            public void set(int index, int value) {
                StrainAnalyzerEntity.this.processingTime = value;
            }

            @Override
            public int size() {
                return 1;
            }
        };
    }


    public static void tick(World world, BlockPos pos, BlockState state, StrainAnalyzerEntity blockEntity) {
        if (world == null || world.isClient) return;
        if (blockEntity.isWorking()) {
            if (canCraft(blockEntity.inventory) && blockEntity.processingTime == 184) {
                craft(blockEntity.inventory);

                blockEntity.processingTime = 1;
                markDirty(world, pos, state);
            } else if (!canCraft(blockEntity.inventory)) {
                blockEntity.processingTime = 0;
                markDirty(world, pos, state);
            }

            state = state.with(StrainAnalyzer.ACTIVE, true);
            world.setBlockState(pos, state, Block.NOTIFY_ALL);
            markDirty(world, pos, state);

            if (!world.isReceivingRedstonePower(pos)) {
                blockEntity.processingTime++;
            }

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

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.putInt("processingTime", this.processingTime);
        Inventories.writeNbt(nbt, this.inventory);
        return nbt;
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        this.inventory = DefaultedList.ofSize(this.inventory.size(), ItemStack.EMPTY);
        Inventories.readNbt(nbt, this.inventory);
        this.processingTime = nbt.getInt("processingTime");
    }

    public boolean isWorking() {
        return processingTime != 0;
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

    public static void craft(DefaultedList<ItemStack> inventory) {

            ItemStack stack = inventory.get(1);
            NbtCompound tag = stack.getTag().copy();
            ItemStack outputSlot = inventory.get(0).copy();
            ItemStack output = ModItems.WEED_SEED.getDefaultStack();


            if(tag != null && outputSlot.isEmpty()) {
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
        return stack.isOf(ModItems.WEED_SEED);
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        return dir == Direction.DOWN && slot == 0;
    }

    @Override
    public int size() {
        return this.inventory.size();
    }

    @Override
    public boolean isEmpty() {
        return this.inventory.isEmpty();
    }

    @Override
    public ItemStack getStack(int slot) {
        //return this.inventory.get(slot);
        return slot >= 0 && slot < this.inventory.size() ? this.inventory.get(slot) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        return Inventories.splitStack(this.inventory, slot, amount);
    }

    @Override
    public ItemStack removeStack(int slot) {
        return Inventories.removeStack(this.inventory, slot);
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        if (slot >= 0 && slot < this.inventory.size()) {
            this.inventory.set(slot, stack);
        }
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
    public void clear() {
        this.inventory.clear();
    }

    @Override
    public Text getDisplayName() {
        return new TranslatableText(getCachedState().getBlock().getTranslationKey());
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        return new StrainAnalyzerScreenHandler(syncId, inv, this, this.propertyDelegate);
    }
}
