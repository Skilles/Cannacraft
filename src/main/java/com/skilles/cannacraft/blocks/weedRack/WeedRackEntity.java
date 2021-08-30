package com.skilles.cannacraft.blocks.weedRack;

import com.skilles.cannacraft.registry.ModEntities;
import com.skilles.cannacraft.registry.ModItems;
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import static com.skilles.cannacraft.Cannacraft.log;

public class WeedRackEntity extends BlockEntity implements Inventory, BlockEntityClientSerializable {
    private DefaultedList<ItemStack> inventory;
    public int dryingTime = 100;
    protected int processTime = 0;

    public WeedRackEntity(BlockPos pos, BlockState state) {
        super(ModEntities.WEED_RACK_ENTITY, pos, state);
        this.inventory = DefaultedList.ofSize(1, ItemStack.EMPTY);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        inventory.clear();
        Inventories.readNbt(nbt, inventory);
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        Inventories.writeNbt(nbt, inventory);
        return nbt;
    }

    public static void tick(World world, BlockPos pos, BlockState state, WeedRackEntity blockEntity) {
        blockEntity.update();
    }

    private void update() {
        ItemStack itemStack = this.getStack(0);
        if (!isEmpty() && itemStack.isOf(ModItems.WEED_BUNDLE) && itemStack.hasNbt() && itemStack.getNbt().getCompound("cannacraft:strain").getFloat("Status") == 1.0F) {
            ++processTime;
            if (processTime >= dryingTime) {
                log("Done!");
                processTime = 0;
                if(!this.world.isClient) {
                    NbtCompound itemTag = itemStack.getNbt();
                    itemTag.getCompound("cannacraft:strain").putFloat("Status", 0.5F);
                    itemStack.setNbt(itemTag);
                    this.setStack(0, itemStack);
                }
            }
        }
    }

    @Override
    public void markDirty() {
        super.markDirty();
        sendUpdate();
    }

    private void sendUpdate() {
        if (this.world != null) {
            BlockState state = this.world.getBlockState(this.pos);
            (this.world).updateListeners(this.pos, state, state, 3);
        }
    }

    @Override
    public void clear() {
        this.processTime = 0;
        this.inventory.clear();
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return this.getStack(0).isEmpty();
    }

    @Override
    public ItemStack getStack(int slot) {
        return this.inventory.get(0);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        ItemStack result = Inventories.splitStack(this.inventory, slot, 1);
        if (!result.isEmpty()) {
            markDirty();
        }
        return result;
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        this.clear();
        this.inventory.set(0, stack);
        this.markDirty();
    }

    @Override
    public ItemStack removeStack(int slot) {
        this.markDirty();
        return Inventories.removeStack(this.inventory, slot);
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return true;
    }

    @Override
    public void fromClientTag(NbtCompound tag) {
        inventory.clear();
        Inventories.readNbt(tag, inventory);
    }

    @Override
    public NbtCompound toClientTag(NbtCompound tag) {
        Inventories.writeNbt(tag, inventory);
        return tag;
    }
}
