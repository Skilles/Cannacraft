package com.skilles.cannacraft.blocks.weedCrop;

import com.skilles.cannacraft.registry.ModBlocks;
import com.skilles.cannacraft.registry.ModItems;
import com.skilles.cannacraft.strain.GeneTypes;
import com.skilles.cannacraft.util.MiscUtil;
import net.fabricmc.fabric.api.util.NbtType;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3f;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

import static com.skilles.cannacraft.CannacraftClient.config;
import static com.skilles.cannacraft.util.MiscUtil.*;

// TODO: use networking/scheduler, migrate tick to BE
public class WeedCrop extends PlantBlock implements BlockEntityProvider, Fertilizable {

    //public static final IntProperty STRAIN = IntProperty.of("strain", 0, 2); // maybe add custom textures per strain
    public static final IntProperty MAXAGE = IntProperty.of("maxage", 3, 9);
    public static final BooleanProperty BREEDING = BooleanProperty.of("breeding");
    public static final IntProperty AGE = IntProperty.of("age", 0, 10); // age 0-3 is 1st stage, age 4 is connector; age 5-8 is 2nd stage; age 9 is final stage flowering, age 10 is 1st stage flowering TODO: add more age
    private static final int CONNECTOR_AGE = 4;
    private static final int FIRST_BLOOM = 10;
    private static final int FINAL_BLOOM = 9;
    private static final int STAGE_ONE_MAX = 3;
    private static final int STAGE_TWO_MAX = 8;
    private static final VoxelShape[] AGE_TO_SHAPE = new VoxelShape[]{
            Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 2.0D, 16.0D),
            Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 6.0D, 16.0D),
            Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 10.0D, 16.0D),
            Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 14.0D, 16.0D),
            Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D),
            Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 2.0D, 16.0D),
            Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 6.0D, 16.0D),
            Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 10.0D, 16.0D),
            Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 14.0D, 16.0D),
            Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 14.0D, 16.0D),
            Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D),
    };

    public WeedCrop(Settings settings) {
        super(settings);
        this.setDefaultState(withMaxAge(STAGE_ONE_MAX).with(AGE, 0).with(BREEDING, false));
    }
    public BlockState withMaxAge(int age) {
        return this.getDefaultState().with(MAXAGE, age);
    }
    public static BlockState withBreeding(BlockState state, boolean breeding) { return state.with(BREEDING, breeding); }
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new WeedCropEntity(pos, state);
    }
    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if(pos.down().equals(neighborPos) && neighborState.isOf(Blocks.AIR))
            return Blocks.AIR.getDefaultState();
        if(pos.down().equals(neighborPos) && neighborState.isOf(Blocks.DIRT))
            MiscUtil.dropStack(world, pos, ModItems.WEED_SEED);
        return !state.canPlaceAt(world, pos) && neighborState.isOf(Blocks.DIRT) ? Blocks.AIR.getDefaultState() : state;
    }
    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return AGE_TO_SHAPE[state.get(this.getAgeProperty())];
    }
    @Override
    protected boolean canPlantOnTop(BlockState floor, BlockView world, BlockPos pos) {
        return floor.isOf(Blocks.FARMLAND) || floor.isOf(Blocks.GRASS_BLOCK);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> stateManager) {
        stateManager.add(AGE);
        stateManager.add(MAXAGE);
        stateManager.add(BREEDING);
    }

    public boolean isMature(BlockState state) {
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
        if (!world.isAir(pos.up()) && isMature(state)) { // if block is above and is fully grown
            if(world.getBlockState(pos.up()).isOf(this)) { // if block above is stage 2
                BlockState aboveState = world.getBlockState(pos.up());
                if(isMature(aboveState)) {// if stage 2 is fully grown
                    MiscUtil.dropStack(world, pos, ModItems.WEED_BUNDLE);
                } else { // apply growth to stage 2
                    this.applyGrowth(world, pos.up(), aboveState);
                }
            }
        } else {
            this.applyGrowth(world, pos, state);
        }
    }
    public void applyGrowth(ServerWorld world, BlockPos pos, BlockState state) {
        int i = this.getAge(state) + this.getGrowthAmount(world);
        int maxAge = getMaxAge(state);
        if (i > maxAge) {
            world.setBlockState(pos.up(), withStage(2).with(AGE,i - maxAge + 5), 2);
            copyNbt(world, pos, pos.up());
            i = maxAge;
        }
        world.setBlockState(pos, this.withAge(i).with(MAXAGE, maxAge), 2);
    }
    private int getBelow(World world, BlockPos pos) {
        int i = 1;
        if (getStage(world.getBlockState(pos)) != 1) {
            for (i = 1; world.getBlockState(pos.down(i)).isOf(this); ++i) { // i = how many stages
            }
        } else {
            i = 0;
        }
        return i;
    }

    /**
     * @return DEFAULT if next stage is final
     */
    private TriState canGrowNext(World world, BlockPos pos) {
        WeedCropEntity blockEntity = (WeedCropEntity) world.getBlockEntity(pos);
        if(blockEntity.hasGene(GeneTypes.YIELD)) // if not first stage and has yield
            return getBelow(world, pos) < blockEntity.growLimit() - 1 ? TriState.TRUE : TriState.DEFAULT; // true if below grow limit, default if next stage is final
        if (getStage(world.getBlockState(pos)) == 1) // if first stage, check if block is air
            return world.isAir(pos.up()) ? TriState.DEFAULT : TriState.FALSE;
        return TriState.FALSE; // false if not first stage and no yield
    }
    protected int getGrowthAmount(World world) {
        return MathHelper.nextInt(world.random, 1, 2);
    }
    public boolean hasRandomTicks(BlockState state) {
        if(getMaxAge(state) == STAGE_ONE_MAX) return true;
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
            newStack.setSubNbt("cannacraft:strain", trimTag(tag));
        }
        return newStack;
    }

    public int getMaxAge(BlockState state) {
        return state.get(MAXAGE);
    }
    public boolean isBreeding(BlockState state) {
        return state.get(BREEDING);
    }

    @Override
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) { // drops cannabis with BE's NBT
       boolean brokenWithShears = player.getMainHandStack().isOf(Items.SHEARS);
        if(!world.isClient) {
           dropItems(world, pos, state, brokenWithShears);
       }
        super.onBreak(world, pos, state, player);
    }
    private void dropItems(World world, BlockPos pos, BlockState state, boolean brokenWithShears) {
        int i;
        MiscUtil.dropStack(world, pos, ModItems.WEED_SEED, brokenWithShears);
        /*if (isBloomed(state)) {
            if(!((WeedCropEntity) world.getBlockEntity(pos)).isMale)
            MiscUtil.dropStack(world, pos, ModItems.WEED_BUNDLE, brokenWithShears);
        }*/
        for(i = 0; world.getBlockState(pos.up(i)).isOf(ModBlocks.WEED_CROP); i++){
            BlockState aboveState = world.getBlockState(pos.up(i));
            //WeedCropEntity aboveEntity = (WeedCropEntity) world.getBlockEntity(pos.up(i));
            if(isBloomed(aboveState)) {
                MiscUtil.dropStack(world, pos.up(i), ModItems.WEED_SEED);
                if(!((WeedCropEntity) world.getBlockEntity(pos.up(i))).isMale)
                MiscUtil.dropStack(world, pos.up(i), ModItems.WEED_BUNDLE, brokenWithShears);

                //world.breakBlock(pos.up(i), false, player);
            }
        }
    }
    /**
     * @return 1 = stage one, 2 = intermediate stage, 3 = final stage, 4 = connector
     */
    private static int getStage(BlockState state) {
        if(state.get(AGE) == CONNECTOR_AGE) return 4;
        return switch (state.get(MAXAGE)) {
            case STAGE_ONE_MAX -> 1;
            case STAGE_TWO_MAX -> 2;
            default -> 3;
        };
    }
    private BlockState withStage(int stage) {
        return switch (stage) {
            default -> this.getDefaultState();
            case 2 -> withMaxAge(8).with(AGE, 5);
            case 3 -> withMaxAge(FINAL_BLOOM).with(AGE, 5);
            case 4 -> withAge(CONNECTOR_AGE);
        };
    }
    /**
     * Blooms 1st, 2nd/medium, or final stages
     */
    private void bloomAll(World world, BlockPos pos) {
        if(getStage(world.getBlockState(pos)) != 3) {
            world.setBlockState(pos, withAge(FIRST_BLOOM));
        } else {
            world.setBlockState(pos, withAge(FINAL_BLOOM));
        }
    }
    public boolean isBloomed(BlockState state) {
        return state.get(AGE) == FIRST_BLOOM || state.get(AGE) == FINAL_BLOOM;
    }
    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        super.randomDisplayTick(state, world, pos, random);
            if (isBreeding(state)) {
                Direction direction = Direction.UP;
                Direction.Axis axis = direction.getAxis();
                int count = random.nextInt(14) + 1;
                for(int i = 0; i < count; ++i) {
                    double h = random.nextDouble() * 0.6D - 0.3D;
                    double f = axis == Direction.Axis.X ? direction.getOffsetX() * 0.52D : h;
                    double j = random.nextDouble() * 6.0D / 16.0D;
                    double k = axis == Direction.Axis.Z ? direction.getOffsetZ() * 0.52D : h;
                    float r = random.nextFloat();
                    float g = 256;
                    float b = random.nextFloat();
                    world.addParticle(new DustParticleEffect(new Vec3f(r, g, b), .7f), pos.getX() + 0.5 + f,
                            pos.getY() + j, pos.getZ() + 0.5 + k, 0, 0, 0);
                }
        }
    }
    private boolean finalGrow(BlockState state) {
        return this.getAge(state) + 1 >= STAGE_TWO_MAX;
    }
    protected void applyGrowTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        WeedCropEntity blockEntity = (WeedCropEntity) world.getBlockEntity(pos);
        assert blockEntity != null;
        assert (getStage(state) == 2 && state.get(AGE) < 5);
        int j = getAge(state);
        float f = getAvailableMoisture(this, world, pos);
        if (getStage(state) == 2) { // second stage
            if (world.getLightLevel(pos.up()) <= 4 && j < this.getMaxAge(state)) {
                TriState canGrow = canGrowNext(world, pos);
                if (random.nextFloat() < (f/14)*(blockEntity.multiplier()/2)) {
                    if (finalGrow(state) && canGrow.orElse(true)) { // if has yield, about to grow, and below grow limit
                        growStage(pos, world, canGrow);
                        world.setBlockState(pos, withStage(4), 2); // convert 2nd stage to connector
                    } else {
                        world.setBlockState(pos, state.with(AGE, j + 1), 2);
                    }
                    world.markDirty(pos);
                }
            }
        } else if (getStage(state) == 1) { // first stage
            if (j < this.getMaxAge(state) && (world.getLightLevel(pos) >= 9)) {
                if (random.nextFloat() < (f/14)*(blockEntity.multiplier()/2)) {
                    world.setBlockState(pos, state.with(AGE, j + 1), 2);
                    world.markDirty(pos);
                }
            } else if (getAge(state) == STAGE_ONE_MAX) { // onGrow
                if (blockEntity.canBreed()) { // if can breed
                    this.breedTick(world, pos, blockEntity);
                }
                TriState canGrow = canGrowNext(world, pos);
                if (canGrow.orElse(true)) { // if true OR default
                    if (random.nextFloat() < (f/14)*(blockEntity.multiplier()/2) && world.getLightLevel(pos) <= 4) {
                        growStage(pos, world, canGrow);
                        world.setBlockState(pos, withStage(4), 2); // set to connector
                        world.markDirty(pos);
                    }
                }
            }
        } else if (getStage(state) == 3){ // final stage
            if (random.nextFloat() < (f/14)*(blockEntity.multiplier()/2)) {
                if(this.getAge(state) + 1 >= FINAL_BLOOM) { // if about to flower, flower all other stages
                    for(int z = 1; z < getBelow(world, pos); z++) { // will only run if z is 1,2,3
                        BlockPos bloomPos = pos.down(z);
                        this.bloomAll(world, bloomPos);
                    }
                }
                world.setBlockState(pos, state.with(AGE, j + 1), 2);
                world.markDirty(pos);
            }
        }
    }

    private void breedTick(ServerWorld world, BlockPos pos, WeedCropEntity blockEntity) {
        if (!blockEntity.isBreeding()) { // if not currently breeding
            if (!blockEntity.hasBred()) { // if hasn't bred before, then start breeding
                world.setBlockState(pos, world.getBlockState(pos).with(BREEDING, true), 2);
                blockEntity.startBreeding();
            }
        } else { // if is currently breeding
            blockEntity.incrementBreedTick();
        }
    }

    // TODO: use realistic grow time
    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) { // grows the first stage, then grows the second stage (at night)
        this.applyGrowTick(state, world, pos, random);
        this.applySpreadTick(state, world, pos, random);
    }
    private void applySpreadTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
       if((!config.getDebug().spreadGrown || isBloomed(state)) && config.getCrop().spread && random.nextFloat() <= config.getCrop().spreadChance/100F) {
           BlockPos spreadPos = getValidSpreadPos(world, pos, random);
           if(spreadPos != null) {
               world.setBlockState(spreadPos, this.getDefaultState());
               copyNbt(world, pos, spreadPos);
           }
       }
    }

    private void growStage(BlockPos pos, ServerWorld world, TriState canGrow) {
        WeedCropEntity blockEntity = (WeedCropEntity) world.getBlockEntity(pos);
        if (canGrow.equals(TriState.DEFAULT)) { // if should grow final stage
            world.setBlockState(pos.up(), withStage(3), 2);
        } else { // grow intermediate stage
            world.setBlockState(pos.up(), withStage(2), 2);
        }
        copyNbt(world, pos, pos.up());
    }
    private void growStage(BlockPos pos, ServerWorld world) {
        growStage(pos, world, canGrowNext(world, pos));
    }
    /* BlockState flags:
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
        int k;
        if (!world.getBlockState(blockPos).isOf(Blocks.FARMLAND) && !world.getBlockState(blockPos).isOf(Blocks.GRASS_BLOCK)) {
            for (k = 0; world.getBlockState(pos.down(k)).isOf(ModBlocks.WEED_CROP); ++k) { // i = how many stages
            }
            blockPos = blockPos.down(k - 1);
        }
        /*
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
        }*/

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
        if (itemStack.hasNbt()) {
            NbtCompound tag =  itemStack.getSubNbt("cannacraft:strain");
            BlockEntity blockEntity = world.getBlockEntity(pos);
            //tag.putInt("ID", ModMisc.STRAIN.get(itemStack).getIndex()); // index 0 = null bug workaround
            if (blockEntity instanceof WeedCropEntity && tag != null && tag.contains("ID")) {
                NbtList attributes = new NbtList();
                if(tag.contains("Attributes")) attributes = tag.getList("Attributes", NbtType.COMPOUND);
                ((WeedCropEntity) blockEntity).setData(tag, attributes);
                world.markDirty(pos);
            }
        }
    }
}
