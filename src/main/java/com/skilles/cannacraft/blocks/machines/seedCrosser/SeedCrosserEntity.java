package com.skilles.cannacraft.blocks.machines.seedCrosser;

import com.skilles.cannacraft.blocks.MachineBlockEntity;
import com.skilles.cannacraft.blocks.machines.strainAnalyzer.StrainAnalyzer;
import com.skilles.cannacraft.registry.ModEntities;
import com.skilles.cannacraft.registry.ModItems;
import com.skilles.cannacraft.strain.GeneticsManager;
import com.skilles.cannacraft.strain.StrainMap;
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

public class SeedCrosserEntity extends MachineBlockEntity {
    private double powerMultiplier = 1; // Energy use multiplier
    private boolean needsPower = true;
    private int processingTime;
    private int powerStored;
    protected final PropertyDelegate propertyDelegate;
    //private DefaultedList<ItemStack> inventory;

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
        }
        if (blockEntity.isWorking()) {
            if (!world.isReceivingRedstonePower(pos)) {
                processTick(blockEntity);
            }
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
    protected static boolean canCraft(DefaultedList<ItemStack> inventory) {
            ItemStack stack = inventory.get(1);
            ItemStack stack2 = inventory.get(2);
            ItemStack output = inventory.get(0);
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
    protected static void processTick(SeedCrosserEntity blockEntity) {
        blockEntity.processingTime++;
        if(blockEntity.needsPower) blockEntity.useEnergy(1 * blockEntity.powerMultiplier);
    }
    protected static void craft(DefaultedList<ItemStack> inventory) {

        ItemStack stack = inventory.get(1);
        ItemStack stack2 = inventory.get(2);
        ItemStack outputSlot = inventory.get(0);
        ItemStack output = ModItems.WEED_SEED.getDefaultStack();
            NbtCompound tag = stack.getSubTag("cannacraft:strain");
            NbtCompound tag2 = stack2.getSubTag("cannacraft:strain");
                NbtCompound strainTag = new NbtCompound();
                String newName = GeneticsManager.crossStrains(StrainMap.getStrain(tag.getInt("ID")).name(), StrainMap.getStrain(tag2.getInt("ID")).name());
                if(!StrainMap.getStrains().containsValue(newName)) {
                    StrainMap.Type newType = GeneticsManager.crossTypes(StrainMap.getStrain(tag.getInt("ID")).type(), StrainMap.getStrain(tag2.getInt("ID")).type());

                    StrainMap.addStrain(newName, newType);
                    System.out.println("Strain added");
                    strainTag.putInt("ID", StrainMap.indexOf(newName));
                    strainTag.putBoolean("Identified", true);
                }
        int newThc = GeneticsManager.crossThc(tag.getInt("THC"), tag2.getInt("THC"));
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

    }
    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        return new SeedCrosserScreenHandler(syncId, inv, this, this.propertyDelegate);
    }
    @Override
    public double getStored(EnergySide face) {
        return this.powerStored;
    }

    @Override
    public void setStored(double amount) {
        powerStored = (int) amount;
    }
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
    public double getMaxStoredPower() {
        return 10000;
    }

    @Override
    public EnergyTier getTier() {
        return EnergyTier.LOW;
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
}
