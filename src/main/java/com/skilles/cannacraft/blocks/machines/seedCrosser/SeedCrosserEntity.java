package com.skilles.cannacraft.blocks.machines.seedCrosser;

import com.skilles.cannacraft.blocks.machines.MachineBlockEntity;
import com.skilles.cannacraft.blocks.machines.strainAnalyzer.StrainAnalyzer;
import com.skilles.cannacraft.registry.ModEntities;
import com.skilles.cannacraft.registry.ModItems;
import com.skilles.cannacraft.strain.GeneticsManager;
import com.skilles.cannacraft.strain.StrainMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class SeedCrosserEntity extends MachineBlockEntity {
    private final double powerMultiplier = 1; // Energy use multiplier
    private final boolean needsPower = true;
    protected final PropertyDelegate propertyDelegate;
    protected static final int timeToProcess = 175;

    public SeedCrosserEntity(BlockPos pos, BlockState state) {
        super(ModEntities.SEED_CROSSER_ENTITY, pos, state, DefaultedList.ofSize(3, ItemStack.EMPTY));
        //this.inventory = DefaultedList.ofSize(3, ItemStack.EMPTY);
        this.propertyDelegate = new PropertyDelegate() {
            @Override
            public int get(int index) {
                switch(index) {
                    case 0:
                        return SeedCrosserEntity.this.processingTime;
                    case 1:
                        return SeedCrosserEntity.this.powerStored;
                    default:
                        return 0;
                }
            }

            @Override
            public void set(int index, int value) {
                switch(index) {
                    case 0:
                        SeedCrosserEntity.this.processingTime = value;
                        break;
                    case 1:
                        SeedCrosserEntity.this.powerStored = value;
                }
            }

            @Override
            public int size() {
                return 2;
            }
        };
    }
    public static void tick(World world, BlockPos pos, BlockState state, SeedCrosserEntity blockEntity) {
        if (world == null || world.isClient) return;
        if(isNextTo(world, pos, Blocks.GLOWSTONE) && blockEntity.powerStored < blockEntity.getMaxStoredPower()) {
            blockEntity.addEnergy(2);
            markDirty(world, pos, state);
        }
        if (blockEntity.isWorking()) {
            state = state.with(StrainAnalyzer.ACTIVE, true);
            world.setBlockState(pos, state, Block.NOTIFY_ALL);
            markDirty(world, pos, state);
            if (canCraft(blockEntity.inventory) && blockEntity.processingTime == timeToProcess) { // when done crafting
                blockEntity.playSound(craft(blockEntity.inventory));
                blockEntity.processingTime = 1; // keep working
                markDirty(world, pos, state);
            } else if (!canCraft(blockEntity.inventory)) {
                blockEntity.processingTime = 0;
                markDirty(world, pos, state);
            } else if (!world.isReceivingRedstonePower(pos)) {
                processTick(blockEntity); // playSound is called here
                markDirty(world, pos, state);
            }
        } else if (canCraft(blockEntity.inventory) && blockEntity.powerStored != 0) { // start if has power
            blockEntity.processingTime = 1;
            markDirty(world, pos, state);
        } else { // when no items or can't craft
            blockEntity.processingTime = 0;
            state = state.with(StrainAnalyzer.ACTIVE, false);
            world.setBlockState(pos, state, Block.NOTIFY_ALL);
            markDirty(world, pos, state);
        }

    }
    private void playSound(int flag) {
        World world  = getWorld();
        SoundEvent runSound = SoundEvents.BLOCK_HONEY_BLOCK_SLIDE;
        assert world != null;
        if(!world.isClient) {
            if(this.processingTime % 25 == 0 && flag == 0) {
                if (this.processingTime != timeToProcess) {
                    world.playSound(
                            null, // Player - if non-null, will play sound for every nearby player *except* the specified player
                            pos, // The position of where the sound will come from
                            runSound, // The sound that will play, in this case, the sound the anvil plays when it lands.
                            SoundCategory.BLOCKS, // This determines which of the volume sliders affect this sound
                            0.15f, //Volume multiplier, 1 is normal, 0.5 is half volume, etc
                            1f // Pitch multiplier, 1 is normal, 0.5 is half pitch, etc
                    );
                } else {
                    world.playSound(
                            null,
                            pos,
                            runSound,
                            SoundCategory.BLOCKS,
                            0.15f,
                            2f
                    );
                }
            } else if(flag == 1) {
                world.playSound(
                        null,
                        pos,
                        SoundEvents.UI_TOAST_CHALLENGE_COMPLETE,
                        SoundCategory.BLOCKS,
                        0.07f,
                        3f
                );
            }
        }
    }
    public boolean isWorking() {
        if(needsPower) { // TODO: use solar power if no generators found
            return processingTime != 0 && powerStored != 0;
        } else {
            return processingTime != 0;
        }
    }
    protected static boolean canCraft(DefaultedList<ItemStack> inventory) {
            ItemStack stack = inventory.get(1);
            ItemStack stack2 = inventory.get(2);
            ItemStack output = inventory.get(0);
            if(stack.equals(ItemStack.EMPTY) || stack2.equals(ItemStack.EMPTY)) return false;
            if (stack.hasTag() && stack2.hasTag()) {
                NbtCompound tag = stack.getSubTag("cannacraft:strain");
                NbtCompound tag2 = stack2.getSubTag("cannacraft:strain");
                if (tag.getBoolean("Identified") && tag2.getBoolean("Identified")) {
                    if(tag.equals(tag2)) return false;
                    if (output.isEmpty()) {
                        return true;
                    } else {
                        NbtCompound outputTag = output.getSubTag("cannacraft:strain");
                        String newName = GeneticsManager.crossStrains(StrainMap.getStrain(tag.getInt("ID")).name(), StrainMap.getStrain(tag2.getInt("ID")).name());
                        int newThc = GeneticsManager.crossThc(tag.getInt("THC"), tag2.getInt("THC"));
                        return StrainMap.isPresent(newName) && outputTag.getInt("ID") == StrainMap.indexOf(newName) && newThc == outputTag.getInt("THC");
                    }
                }
            }
        return false;
    }
    private static void processTick(SeedCrosserEntity blockEntity) {
        blockEntity.processingTime++;
        if(blockEntity.needsPower) blockEntity.useEnergy(1 * blockEntity.powerMultiplier);
        blockEntity.playSound(0);
    }
    protected static int craft(DefaultedList<ItemStack> inventory) {
        int flag = 0; // flag if no new strain was added

        ItemStack stack = inventory.get(1);
        ItemStack stack2 = inventory.get(2);
        ItemStack outputSlot = inventory.get(0);
        ItemStack output = ModItems.WEED_SEED.getDefaultStack();
        NbtCompound tag = stack.getSubTag("cannacraft:strain");
        NbtCompound tag2 = stack2.getSubTag("cannacraft:strain");

        String newName = GeneticsManager.crossStrains(StrainMap.getStrain(tag.getInt("ID")).name(), StrainMap.getStrain(tag2.getInt("ID")).name());
        StrainMap.Type newType = GeneticsManager.crossTypes(StrainMap.getStrain(tag.getInt("ID")).type(), StrainMap.getStrain(tag2.getInt("ID")).type());
        int newThc = GeneticsManager.crossThc(tag.getInt("THC"), tag2.getInt("THC"));

        if(!StrainMap.getStrains().containsKey(newName)) {
            StrainMap.addStrain(newName, newType);
            System.out.println("New strain: "+StrainMap.getStrain(StrainMap.getStrainCount() - 1)); // print latest strain
            flag = 1; // flag if strain was added
        }
        NbtCompound strainTag = new NbtCompound();
        strainTag.putInt("ID", StrainMap.indexOf(newName));
        strainTag.putBoolean("Identified", true);
        strainTag.putInt("THC", newThc);
        NbtCompound outputTag = new NbtCompound();
        outputTag.put("cannacraft:strain", strainTag);
        output.setTag(outputTag);

        if(outputSlot.isEmpty()) {
            inventory.set(0, output);
        } else if(outputSlot.getTag().equals(outputTag)) {
            outputSlot.increment(1);
        }
        stack.decrement(1);
        stack2.decrement(1);
        return flag;
    }
    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        return new SeedCrosserScreenHandler(syncId, inv, this, this.propertyDelegate);
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