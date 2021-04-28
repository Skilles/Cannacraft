package com.skilles.cannacraft.blocks.weedCrop;

import com.skilles.cannacraft.registry.ModItems;
import com.skilles.cannacraft.registry.ModMisc;
import com.skilles.cannacraft.strain.GeneticsManager;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3f;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.skilles.cannacraft.strain.StrainMap.*;

public class WeedCrop extends PlantBlock implements BlockEntityProvider, Fertilizable { // custom crop block implementation (very WIP)

    //public static final IntProperty STRAIN = IntProperty.of("strain", 0, 2); // maybe add custom textures per strain
    public static final IntProperty MAXAGE = IntProperty.of("maxage", 0, 7);
    public static final BooleanProperty BREEDING = BooleanProperty.of("breeding");
    public static final IntProperty AGE = Properties.AGE_7;
    private static final VoxelShape[] AGE_TO_SHAPE = new VoxelShape[]{Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 2.0D, 16.0D), Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 4.0D, 16.0D), Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 6.0D, 16.0D), Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 8.0D, 16.0D), Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 10.0D, 16.0D), Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 12.0D, 16.0D), Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 14.0D, 16.0D), Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D)};

    public WeedCrop(Settings settings) {
        super(settings);
        this.setDefaultState(withMaxAge(7).with(AGE, 0).with(BREEDING, false));
    }
    public BlockState withMaxAge(int age) {
        return this.getDefaultState().with(MAXAGE, age);
    }
    public BlockState withBreeding(BlockState state, boolean breeding) { return state.with(BREEDING, breeding); }
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new WeedCropEntity(pos, state);
    }
    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        return !state.canPlaceAt(world, pos) && !state.isOf(this) ? Blocks.AIR.getDefaultState() : state;
    }
    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return AGE_TO_SHAPE[state.get(this.getAgeProperty())];
    }
    @Override
    protected boolean canPlantOnTop(BlockState floor, BlockView world, BlockPos pos) {
        BlockPos blockPos = pos.down();
        //if(world.getBlockState(blockPos).isOf(this)) return true;
        return floor.isOf(Blocks.FARMLAND);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> stateManager) {
        stateManager.add(AGE);
        stateManager.add(MAXAGE);
        stateManager.add(BREEDING);
    }
    protected static NbtCompound trimTag(NbtCompound tag) {
        return trimTag(tag, 0);
    }
    private static NbtCompound trimTag(NbtCompound tag, int flag){
        NbtCompound newTag = tag;
        if(tag != null) {
        newTag.remove("id");
        newTag.remove("x");
        newTag.remove("y");
        newTag.remove("z");
        newTag.putInt("THC", newTag.getInt("Seed THC"));
        newTag.remove("Seed THC");
        if(flag == 1) newTag.putInt("ID", newTag.getInt("Seed ID"));
        newTag.remove("Seed ID");
        }
        return newTag;
    }

    public boolean isMature(BlockState state) {
        //if(!state.get(MATURE)) return false;
        return (state.get((this.getAgeProperty())) >= getMaxAge(state));
    }

    @Override
    public boolean isFertilizable(BlockView world, BlockPos pos, BlockState state, boolean isClient) {
        return hasRandomTicks(state);
    }

    @Override
    public boolean canGrow(World world, Random random, BlockPos pos, BlockState state) {
        return true;
    }
    @Override
    public void grow(ServerWorld world, Random random, BlockPos pos, BlockState state) {
        System.out.println("grow method");
        if(world.isAir(pos.up())) { // if block above is air
            if(getMaxAge(state) ==  7) { // if block is 1st stage
                if (state.get((this.getAgeProperty())) >= getMaxAge(state)) { // fully grown, set above to stage 2
                    world.setBlockState(pos.up(), withMaxAge(5), 2);
                    world.getBlockEntity(pos.up()).readNbt(world.getBlockEntity(pos).writeNbt(new NbtCompound()));
                    world.markDirty(pos.up());
                } else {
                    this.applyGrowth(world, pos, state, 7);
                }
            } else {
                this.applyGrowth(world, pos, state, 5);
            }
        } else if(state.get((this.getAgeProperty())) >= getMaxAge(state)) { // if block is above and is fully grown
            if(world.getBlockState(pos.up()).isOf(this)) { // if block above is stage 2
                BlockState aboveState = world.getBlockState(pos.up());
                if(world.getBlockState(pos.up()).get(AGE) >= world.getBlockState(pos.up()).get(MAXAGE)) {// if stage 2 is fully grown
                    NbtCompound tag = world.getBlockEntity(pos).writeNbt(new NbtCompound());
                    ItemStack itemStack = new ItemStack(ModItems.WEED_FRUIT);
                    itemStack.putSubTag("cannacraft:strain", trimTag(tag));
                    dropStack(world, pos, itemStack);
                } else { // apply growth to stage 2
                    this.applyGrowth(world, pos.up(), aboveState, 5);
                }
            }
        }
    }


    public void applyGrowth(World world, BlockPos pos, BlockState state, int maxAge) {
        System.out.println("apply growth method");
        int i = this.getAge(state) + this.getGrowthAmount(world);
        int j = maxAge;
        if (i > j) {
            i = j;
        }
        world.setBlockState(pos, this.withAge(i).with(MAXAGE, getMaxAge(state)), 2);
    }

    protected int getGrowthAmount(World world) {
        return MathHelper.nextInt(world.random, 2, 5);
    }
    public boolean hasRandomTicks(BlockState state) {
        if(getMaxAge(state) == 7) return true;
        return !this.isMature(state);
    }
    protected int getAge(BlockState state) {
        return state.get(this.getAgeProperty());
    }
    public BlockState withAge(int age) {
        return this.getDefaultState().with(this.getAgeProperty(), age);
    }
    public IntProperty getAgeProperty() {
        return AGE;
    }

    @Override
    public ItemStack getPickStack(BlockView world, BlockPos pos, BlockState state) {
        ItemStack newStack = new ItemStack(ModItems.WEED_SEED);
        NbtCompound tag = world.getBlockEntity(pos).writeNbt(new NbtCompound());
        if (tag != null) {
            newStack.putSubTag("cannacraft:strain", trimTag(tag));
        }
        return newStack;
    }

    public int getMaxAge(BlockState state) {
        return state.get(MAXAGE);
    }
    public boolean isBreeding(BlockState state) {
        return state.get(BREEDING);
    }

    /* List<ItemStack> implementation (for future drop modifiers)
        NbtCompound tag;
        boolean twoStacks;
        @Override
        public List<ItemStack> getDroppedStacks(BlockState state, LootContext.Builder builder) {
            List<ItemStack> itemStackList = Lists.newArrayList();
            ItemStack newStack = new ItemStack(ModItems.WEED_FRUIT);
            itemStackList.add(newStack);
            if (tag != null) {
                newStack.putSubTag("cannacraft:strain", trimTag(tag));
            } else {
                System.out.println("Error: NULLTAG");
            }
            if(twoStacks) itemStackList.add(newStack);
            return itemStackList;
        }

        @Override
        public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
            if(state.get(MATURE)) {
                ItemStack newStack = new ItemStack(ModItems.WEED_FRUIT);
                tag = world.getBlockEntity(pos).writeNbt(new NbtCompound());
                if(world.getBlockState(pos.up()).isOf(this)) {
                    world.breakBlock(pos.up(), false, player);
                    twoStacks = true;
                } else {
                    twoStacks = false;
                }
            }
            super.onBreak(world, pos, state, player);
        }
        */
    @Override
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) { // drops cannabis with BE's NBT
       boolean brokenWithShears = false;
       if(player.getMainHandStack().isOf(Items.SHEARS)) brokenWithShears = true;
        if(getAge(state) == getMaxAge(state)) {
            ItemStack newStack = new ItemStack(ModItems.WEED_FRUIT);
            ItemStack seedStack = new ItemStack(ModItems.WEED_SEED);
            NbtCompound tag = world.getBlockEntity(pos).writeNbt(new NbtCompound());
            if (tag != null) {
                tag.putInt("THC", tag.getInt("Seed THC"));
                seedStack.putSubTag("cannacraft:strain", trimTag(tag, 1));
                newStack.putSubTag("cannacraft:strain", trimTag(tag));
            } else {
                System.out.println("Error: NULLTAG");
            }
            if(world.getBlockState(pos.up()).isOf(this)) {
                if(world.getBlockState(pos.up()).get(AGE) >= world.getBlockState(pos.up()).get(MAXAGE)) {
                    ItemStack itemStack = newStack.copy();
                    if (!tag.getBoolean("Male") && brokenWithShears) dropStack(world, pos, itemStack);
                }
                world.breakBlock(pos.up(), false, player);
            }
            if(!tag.getBoolean("Male") && brokenWithShears) dropStack(world, pos, newStack);
            dropStack(world, pos, seedStack);
        }
        super.onBreak(world, pos, state, player);
    }

    /**
     * Crosses name/type/thc with adjacent male. Gets random male if more than one. THC will not change if male THC is lower than female.
     */
    private void breedCrops(World world, BlockPos pos, Random random){
        NbtCompound ogTag = world.getBlockEntity(pos).writeNbt(new NbtCompound());
        if(!ogTag.getBoolean("Male")) {
            // Cross thc/names/type
            List<String> stringArray = new ArrayList<>();
            List<Type> typeArray = new ArrayList<>();
            int id = ogTag.getInt("ID");
            int thc = ogTag.getInt("Seed THC");
            int COUNT = 0;
            int maleId = 0;
            List<Integer> thcValues = new ArrayList<>();
            for (Direction direction : Direction.Type.HORIZONTAL) {
                BlockEntity blockEntity2 = world.getBlockEntity(pos.offset(direction));
                if (blockEntity2 instanceof WeedCropEntity) {
                    NbtCompound tag = blockEntity2.writeNbt(new NbtCompound());

                    if (tag.getBoolean("Male")) {
                        if (thc < tag.getInt("Seed THC")) {
                            thcValues.add(tag.getInt("Seed THC")); // adds iterative highest thc values
                            maleId = tag.getInt("ID"); // highest thc male id
                            thc = tag.getInt("Seed THC"); // highest thc value
                        }
                        stringArray.add(getStrain(tag.getInt("Seed THC")).name()); // all names of surrounding males
                        typeArray.add(getStrain(tag.getInt("Seed THC")).type());
                        COUNT++;
                    }
                }
            }
            if (COUNT > 0) {
                if (maleId == 0) {
                    thc = ogTag.getInt("Seed THC");
                    maleId = indexOf(stringArray.get(GeneticsManager.random().nextInt(stringArray.size() - 1))); // id is random male
                }
                // Set thc
                ogTag.putInt("Seed THC", GeneticsManager.crossThc(thc, ogTag.getInt("THC")));
                System.out.println("THC: " + ogTag.getInt("Seed THC"));
                // Set name/type
                int randId = random.nextInt(stringArray.size());
                String name1 = getStrain(id).name();
                String name2 = getStrain(maleId).name();
                Type type1 = getStrain(id).type();
                Type type2 = typeArray.get(randId);
                String crossedName = GeneticsManager.crossStrains(name1, name2);
                if (!isPresent(crossedName)) addStrain(crossedName, GeneticsManager.crossTypes(type1, type2));
                ogTag.putInt("Seed ID", indexOf(crossedName));

                // Save nbt
                world.getBlockEntity(pos).readNbt(ogTag);
                world.markDirty(pos);

                ogTag = world.getBlockEntity(pos).writeNbt(new NbtCompound());

                System.out.println("New tag: " + ogTag);
                world.setBlockState(pos, world.getBlockState(pos).with(BREEDING, true), 2);
            }
        }
    }
    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        super.randomDisplayTick(state, world, pos, random);
            if (isBreeding(state)) {
                Direction direction = Direction.UP;
                Direction.Axis axis = direction.getAxis();
                double h = random.nextDouble() * 0.6D - 0.3D;
                double i = axis == Direction.Axis.X ? direction.getOffsetX() * 0.52D : h;
                double j = random.nextDouble() * 6.0D / 16.0D;
                double k = axis == Direction.Axis.Z ? direction.getOffsetZ() * 0.52D : h;
                float r = random.nextFloat();
                float g = 256;
                float b = random.nextFloat();
                world.addParticle(new DustParticleEffect(new Vec3f(r, g, b), .7f), pos.getX() + 0.5 + i,
                        pos.getY() + j, pos.getZ() + 0.5 + k, 0, 0, 0);

        }
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) { // grows the first stage, then grows the second stage
        if (world.isAir(pos.up())) {
            int i;

            for (i = 1; world.getBlockState(pos.down(i)).isOf(this); ++i) {
            }
            int j = getAge(state);
            if (j < this.getMaxAge(state) && (world.getBaseLightLevel(pos, 0) >= 9)) {
                float f = getAvailableMoisture(this, world, pos);
                if (random.nextInt((int) (25.0F / f) + 1) == 0) {
                    world.setBlockState(pos, state.with(AGE, j + 1), 2);
                }
            } else if (j == 7) { // onGrow
                breedCrops(world, pos, random);
                if (world.isAir(pos.up())) { // if block above is air
                    if (getMaxAge(state) == 7) { // if block is 1st stage
                        float f = getAvailableMoisture(this, world, pos);
                        if (random.nextInt((int) (25.0F / f) + 1) == 0) {
                            world.setBlockState(pos.up(), withMaxAge(5), 2);
                            NbtCompound tag = world.getBlockEntity(pos).writeNbt(new NbtCompound());
                            world.setBlockState(pos, withBreeding(state, false), 2);
                            world.getBlockEntity(pos).readNbt(tag);
                            world.getBlockEntity(pos.up()).readNbt(tag);
                            world.markDirty(pos.up());
                            world.markDirty(pos);
                        }
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
    protected static float getAvailableMoisture(Block block, BlockView world, BlockPos pos) {
        float f = 1.0F;
        BlockPos blockPos = pos.down();

        for(int i = -1; i <= 1; ++i) {
            for(int j = -1; j <= 1; ++j) {
                float g = 0.0F;
                BlockState blockState = world.getBlockState(blockPos.add(i, 0, j));
                if (blockState.isOf(Blocks.FARMLAND)) {
                    g = 1.0F;
                    if (blockState.get(FarmlandBlock.MOISTURE) > 0) {
                        g = 3.0F;
                    }
                }

                if (i != 0 || j != 0) {
                    g /= 4.0F;
                }

                f += g;
            }
        }

        BlockPos blockPos2 = pos.north();
        BlockPos blockPos3 = pos.south();
        BlockPos blockPos4 = pos.west();
        BlockPos blockPos5 = pos.east();
        boolean bl = world.getBlockState(blockPos4).isOf(block) || world.getBlockState(blockPos5).isOf(block);
        boolean bl2 = world.getBlockState(blockPos2).isOf(block) || world.getBlockState(blockPos3).isOf(block);
        if (bl && bl2) {
            f /= 2.0F;
        } else {
            boolean bl3 = world.getBlockState(blockPos4.north()).isOf(block) || world.getBlockState(blockPos5.north()).isOf(block) || world.getBlockState(blockPos5.south()).isOf(block) || world.getBlockState(blockPos4.south()).isOf(block);
            if (bl3) {
                f /= 2.0F;
            }
        }

        return f;
    }

    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        if (itemStack.hasTag()) {
            NbtCompound tag =  itemStack.getSubTag("cannacraft:strain");
            BlockEntity blockEntity = world.getBlockEntity(pos);
            tag.putInt("ID", ModMisc.STRAIN.get(itemStack).getIndex()); // index 0 = null bug workaround
            if (blockEntity instanceof WeedCropEntity && tag != null && tag.contains("ID")) {
                ((WeedCropEntity) blockEntity).setData(tag.getInt("ID"), tag.getInt("THC"), tag.getBoolean("Identified"), tag.getBoolean("Male"));
                world.markDirty(pos);
            }
        }
    }
}
