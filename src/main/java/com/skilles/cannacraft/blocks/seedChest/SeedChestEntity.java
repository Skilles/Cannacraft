package com.skilles.cannacraft.blocks.seedChest;

import com.skilles.cannacraft.blocks.ImplementedInventory;
import com.skilles.cannacraft.registry.ModEntities;
import com.skilles.cannacraft.registry.ModItems;
import com.skilles.cannacraft.util.StrainUtil;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;

import java.util.Collections;
import java.util.Comparator;

public class SeedChestEntity extends BlockEntity implements NamedScreenHandlerFactory, ImplementedInventory {
    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(36, ItemStack.EMPTY);

    /*public SeedChestEntity(BlockEntityType<? extends BlockEntity> type, BlockPos pos, BlockState state) {
        super(ModEntities.SEED_CHEST_ENTITY, pos, state);
    }*/

    public SeedChestEntity(BlockPos pos, BlockState state) {
        super(ModEntities.SEED_CHEST_ENTITY, pos, state);
    }


    //From the ImplementedInventory Interface

    @Override
    public DefaultedList<ItemStack> getItems() {
        return inventory;
    }

    //These Methods are from the NamedScreenHandlerFactory Interface
    //createMenu creates the ScreenHandler itself
    //getDisplayName will Provide its name which is normally shown at the top

    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        //We provide *this* to the screenHandler as our class Implements Inventory
        //Only the Server has the Inventory at the start, this will be synced to the client in the ScreenHandler
        return new SeedChestScreenHandler(syncId, playerInventory, this);
    }

    @Override
    public Text getDisplayName() {
        return new TranslatableText(getCachedState().getBlock().getTranslationKey());
    }

    @Override
    public void readNbt(NbtCompound tag) {
        super.readNbt(tag);
        Inventories.readNbt(tag, this.inventory);
    }

    /**
     * Replaces the current stack in an inventory slot with the provided stack.
     *
     * @param slot  The inventory slot of which to replace the itemstack.
     * @param stack The replacing itemstack. If the stack is too big for
     *              this inventory ({@link Inventory#getMaxCountPerStack()}),
     */
    @Override
    public void setStack(int slot, ItemStack stack) {
        ImplementedInventory.super.setStack(slot, stack);
        if(!inventory.isEmpty()) {
            Collections.sort(inventory, new Comparator<ItemStack>() {
                @Override
                public int compare(ItemStack o1, ItemStack o2) {
                    if(o1.isOf(ModItems.WEED_SEED) && o2.isOf(ModItems.WEED_SEED)) {
                        NbtCompound tag1 = o1.getSubTag("cannacraft:strain");
                        NbtCompound tag2 = o2.getSubTag("cannacraft:strain");
                        String name1 = StrainUtil.getStrain(tag1.getInt("ID")).name();
                        String name2 = StrainUtil.getStrain(tag2.getInt("ID")).name();
                        return name1.compareTo(name2);
                    }
                    return 0;
                }
            });
        }
    }

    @Override
    public NbtCompound writeNbt(NbtCompound tag) {
        super.writeNbt(tag);
        Inventories.writeNbt(tag, this.inventory);
        return tag;
    }
}