package com.skilles.cannacraft.blocks.weedCrop;

import com.skilles.cannacraft.CannacraftClient;
import com.skilles.cannacraft.config.ModConfig;
import com.skilles.cannacraft.dna.genome.Genome;
import com.skilles.cannacraft.dna.genome.gene.InfoGene;
import com.skilles.cannacraft.dna.genome.gene.TraitGene;
import com.skilles.cannacraft.registry.ModEntities;
import com.skilles.cannacraft.strain.Strain;
import com.skilles.cannacraft.strain.StrainInfo;
import com.skilles.cannacraft.util.CrossUtil;
import com.skilles.cannacraft.util.DnaUtil;
import com.skilles.cannacraft.util.MiscUtil;
import me.shedaniel.autoconfig.ConfigData;
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import static com.skilles.cannacraft.Cannacraft.log;
import static com.skilles.cannacraft.dna.genome.Enums.InfoType;
import static com.skilles.cannacraft.dna.genome.Enums.Phenotype;

// TODO: drop seedId, seedThc if male
// TODO: cross plants by collecting pollen from male
// TODO: hide THC for males (possibly completely remove thc for males, selecting for THC will be done by breeding crosses)
public class WeedCropEntity extends BlockEntity implements BlockEntityClientSerializable {

    public WeedCropEntity(BlockPos pos, BlockState state) {
        super(ModEntities.WEED_CROP_ENTITY, pos, state);
        config = CannacraftClient.config;
        try {
            config.validatePostLoad();
        } catch (ConfigData.ValidationException e) {
            e.printStackTrace();
        }
    }

    private final ModConfig config;

    private boolean identified;

    private int breedTimer = 0;

    private static final int MAX_BREED = 50;

    private int cachedLimit;

    private Genome genome;

    private Genome seedGenome;

    private StrainInfo strainInfo;

    private boolean initialized;

    private boolean seedInitialized;

    public boolean boosted;

    public void setData(Genome genome, boolean identified) {
        this.identified = identified;
        this.genome = this.seedGenome = genome;
    }

    public void setData(NbtCompound tag) {
        this.identified = tag.getBoolean("Identified");
        this.genome = this.seedGenome = new Genome(tag.getString("DNA"));
        log("Data for BE crop set" + tag);
    }

    float multiplier() {
        float multiplier = 1;
        if (hasGene(Phenotype.SPEED)) multiplier *= (1.0F + ((float) getGene(Phenotype.SPEED).value) / 2.0F); // 1: 50%, 2: 100%, 3: 150%
        multiplier *= config.getCrop().speed;
        if (boosted) multiplier *= 2;
        return multiplier;
    }

    public void startBreeding() {
        breedTimer = 1;
    }

    public void stopBreeding() {
        breedCrops(getWorld(), pos, MiscUtil.random());
        breedTimer = -1;
        getWorld().setBlockState(pos, WeedCrop.withBreeding(getCachedState(), false));
    }

    public int breedingProgress() {
        return breedTimer;
    }

    public boolean isBreeding() {
        if (breedTimer >= (MAX_BREED / multiplier())) {
            stopBreeding();
            return false;
        }
        return breedTimer > 0;
    }
    public boolean hasBred() {
        return breedTimer < 0;
    }

    public void incrementBreedTick() {
        if(isBreeding()) breedTimer++;
    }

    public boolean canBreed() {
        if (breedingProgress() != -1 && !this.isMale()) {
            int COUNT = (int) Direction.Type.HORIZONTAL.stream()
                    .filter(direction -> world.getBlockEntity(pos.offset(direction)) instanceof WeedCropEntity otherEntity
                            && otherEntity.isMale() && this.getStrain().isResource() == otherEntity.getStrain().isResource())
                    .count();
            return COUNT > 0;
        } else {
            return false;
        }
    }

    public int growLimit() {
        if(this.cachedLimit != 0) return cachedLimit;
        if(hasGene(Phenotype.YIELD)) {
            cachedLimit = getGene(Phenotype.YIELD).value + 2;
        } else {
            cachedLimit = 2;
        }
        cachedLimit += config.getCrop().yield;
        return cachedLimit;
    }

    /**
     * Crosses name/type/thc with adjacent male. Gets highest THC male if more than one.
     * TODO: add check/randomness if ID is unknown
     */
    void breedCrops(World world, BlockPos pos, Random random) {
        if (canBreed()) {
            // Cross thc/names/type

            List<WeedCropEntity> nearCrops = new ArrayList<>();

            for (Direction direction : Direction.Type.HORIZONTAL) {
                BlockEntity blockEntity2 = world.getBlockEntity(pos.offset(direction));
                if (blockEntity2 instanceof WeedCropEntity weedBlockEntity2 && weedBlockEntity2.isMale()) {
                    nearCrops.add(weedBlockEntity2);
                }
            }
            assert !nearCrops.isEmpty();
            WeedCropEntity alphaMale = config.getCrop().randomBreed ? nearCrops.get(random.nextInt(nearCrops.size())) : nearCrops.stream().max(Comparator.comparingInt(WeedCropEntity::getThc)).get();
            // Set seed thc
            this.seedGenome.updateGene(new InfoGene(InfoType.THC, CrossUtil.crossThc(alphaMale.getThc(), this.getThc())), true);
            // this.seedThc = CrossUtil.crossThc(alphaMale.thc, this.thc);
            // Set strain
            NbtCompound myTag = this.writeNbt(new NbtCompound());
            NbtCompound maleTag = alphaMale.writeNbt(new NbtCompound());
            Strain crossedStrain = CrossUtil.crossStrains(this.getStrain(), alphaMale.getStrain());
            log("Strain 1: " + this.getStrain());
            log("Strain 2: " + alphaMale.getStrain());
            log("Name of crossed strain: " + crossedStrain.name());
            this.seedGenome.updateGene(new InfoGene(InfoType.STRAIN, crossedStrain.id()), true);
            this.markDirty();
            log("New tag: " + this.writeNbt(new NbtCompound()));

            world.setBlockState(pos, WeedCrop.withBreeding(world.getBlockState(pos), false), 2);
            world.markDirty(pos);
            world.playSound(
                    null,
                    pos,
                    SoundEvents.BLOCK_NOTE_BLOCK_BELL,
                    SoundCategory.BLOCKS,
                    0.2f,
                    2f
            );
        }
    }

    boolean isMale() {
        return this.genome.isMale();
    }

    boolean isIdentified() {
        return this.identified;
    }

    int getThc() {
        return this.strainInfo.thc();
    }

    Strain getStrain() {
        return this.strainInfo.strain();
    }

    StrainInfo getStrainInfo() {
        return this.strainInfo;
    }

    Genome getGenome() {
        return genome;
    }

    TraitGene getGene(Phenotype type) {
        return this.genome.traitMap.get(type);
    }

    boolean hasGene(Phenotype type) {
        return this.genome.traitMap.get(type).value > 0;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound tag) {
        super.writeNbt(tag);
        tag.putBoolean("Identified", identified);
        tag.putString("DNA", genome.toString());
        tag.putString("Seed DNA", seedGenome.toString());
        return tag;
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        this.identified = nbt.getBoolean("Identified");
        if (!this.initialized) {
            this.genome = new Genome(nbt.getString("DNA"));
            this.strainInfo = DnaUtil.convertStrain(genome, this.identified);
            this.initialized = true;
        }
        if (!this.seedInitialized) {
            this.seedGenome = new Genome(nbt.getString("Seed DNA"));
            this.seedInitialized = true;
        }
    }

    @Override
    public void markDirty() {
        super.markDirty();
        this.initialized = false;
        this.seedInitialized = false;
    }

    @Override
    public void fromClientTag(NbtCompound tag) {
        readNbt(tag);
    }

    @Override
    public NbtCompound toClientTag(NbtCompound tag) {
        return writeNbt(tag);
    }

    @Override
    public void sync() {
        if (!world.isClient) {
            BlockEntityClientSerializable.super.sync();
        }
    }
}
