package com.skilles.cannacraft.blocks.weedCrop;

import com.skilles.cannacraft.items.Seed;
import com.skilles.cannacraft.registry.ModBlocks;
import com.skilles.cannacraft.registry.ModItems;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public class WeedCrop extends CropBlock implements BlockEntityProvider {

    //public static final IntProperty STRAIN = IntProperty.of("strain", 0, 2); // maybe add custom textures per strain
    public static final IntProperty MAXAGE = IntProperty.of("maxage", 0, 7);

    public WeedCrop(Settings settings) {
        super(settings);
        this.setDefaultState(withMaxAge(7).with(AGE, 0));
    }
    public BlockState withMaxAge(int age) {
        return this.getDefaultState().with(MAXAGE, age);
    }
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new WeedCropEntity(pos, state);
    }
    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> stateManager) {
        stateManager.add(AGE);
        stateManager.add(MAXAGE);
    }

    @Override
    public boolean isMature(BlockState state) {
        if (getMaxAge(state) == 7){
            return false;
        } else if (getMaxAge(state) == 5) {
            return (state.get((this.getAgeProperty())) >= getMaxAge(state));
        }
        return false;
    }

    @Override
    public void grow(ServerWorld world, Random random, BlockPos pos, BlockState state) {
        super.grow(world, random, pos, state);
        if(state.get(AGE) == state.get(MAXAGE)) {
            Seed seedItem = (Seed) this.getSeedsItem().asItem();
            NbtCompound tag = world.getBlockEntity(pos).writeNbt(new NbtCompound());
            tag.remove("id");
            tag.remove("x");
            tag.remove("y");
            tag.remove("z");
            ItemStack itemStack = new ItemStack(seedItem);
            itemStack.putSubTag("cannacraft:strain", tag);
            dropStack(world, pos, itemStack);
        }
    }

    @Override
    public void applyGrowth(World world, BlockPos pos, BlockState state) {
        int i = this.getAge(state) + this.getGrowthAmount(world);
        int j = this.getMaxAge(state);
        if (i > j) {
            i = j;
        }
        world.setBlockState(pos, this.withAge(i).with(MAXAGE, getMaxAge(state)), 2);
    }

    @Override
    protected ItemConvertible getSeedsItem() {
        return ModItems.SEED;
    }

    public int getMaxAge(BlockState state) {
        return state.get(MAXAGE);
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (world.isAir(pos.up())) {
            int i;
            for (i = 1; world.getBlockState(pos.down(i)).isOf(this); ++i) {
            }
            if (i < 3) {
                int j = state.get(AGE);
                if (j == getMaxAge(state) && i < 2) {
                    if(world.isAir(pos.up())) {
                        world.setBlockState(pos.up(), withMaxAge(5), 2);
                        if(world.getBlockState(pos.up()).isOf(ModBlocks.WEED_CROP)) { // null check
                            world.getBlockEntity(pos.up()).readNbt(world.getBlockEntity(pos).writeNbt(new NbtCompound()));
                            world.markDirty(pos.up());
                        }
                    }
                } else if (j < this.getMaxAge(state) && (world.getBaseLightLevel(pos, 0) >= 9)) {
                        float f = getAvailableMoisture(this, world, pos);
                        if (random.nextInt((int) (25.0F / f) + 1) == 0) {
                            world.setBlockState(pos, state.with(AGE, j + 1), 2);
                        }
                }
            }
        }
    }
    /** BlockState flags:
1 NOTIFY_NEIGHBORS
2 NOTIFY_LISTENERS
3 NOTIFY_ALL
4 NO_REDRAW
8 REDRAW_ON_MAIN_THREAD
16 FORCE_STATE
32 SKIP_DROPS
64 MOVED
128 SKIP_LIGHTING_UPDATES
*/

    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        if (itemStack.hasTag()) {
            NbtCompound tag =  itemStack.getSubTag("cannacraft:strain");
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof WeedCropEntity && tag != null && tag.contains("ID")) {
                ((WeedCropEntity) blockEntity).setData(tag.getInt("ID"), tag.getInt("THC"), tag.getBoolean("Identified"));
                world.markDirty(pos);
            }
        }
    }
}
