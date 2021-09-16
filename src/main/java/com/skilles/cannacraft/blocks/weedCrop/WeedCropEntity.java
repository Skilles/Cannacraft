package com.skilles.cannacraft.blocks.weedCrop;

import com.skilles.cannacraft.CannacraftClient;
import com.skilles.cannacraft.config.ModConfig;
import com.skilles.cannacraft.dna.genome.Genome;
import com.skilles.cannacraft.dna.genome.Meiosis;
import com.skilles.cannacraft.dna.genome.gene.TraitGene;
import com.skilles.cannacraft.registry.BlockEntities;
import com.skilles.cannacraft.strain.Strain;
import com.skilles.cannacraft.strain.StrainInfo;
import com.skilles.cannacraft.util.DnaUtil;
import com.skilles.cannacraft.util.MiscUtil;
import me.shedaniel.autoconfig.ConfigData;
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
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
import static com.skilles.cannacraft.dna.genome.Enums.Phenotype;

// TODO: drop seedId, seedThc if male
// TODO: cross plants by collecting pollen from male

public class WeedCropEntity extends BlockEntity implements BlockEntityClientSerializable {

    public WeedCropEntity(BlockPos pos, BlockState state) {
        super(BlockEntities.CROP, pos, state);
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

    private int seedCount = 2;

    private Genome genome;

    private List<Genome> seedGenomes;

    private StrainInfo strainInfo;

    private boolean initialized;

    private boolean seedInitialized;

    public boolean boosted;

    public void setData(Genome genome, boolean identified) {
        this.identified = identified;
        this.genome = genome;
        this.strainInfo = DnaUtil.convertStrain(genome, identified);
        this.initialized = true;
        this.seedGenomes = new ArrayList<>();
        this.seedGenomes.add(this.genome);
    }

    public void setData(NbtCompound tag) {
        this.identified = tag.getBoolean("Identified");
        this.genome = new Genome(tag.getString("DNA"));
        this.strainInfo = DnaUtil.convertStrain(this.genome, identified);
        this.initialized = true;
        this.seedGenomes = new ArrayList<>();
        this.seedGenomes.add(this.genome);
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
        if (isBreeding()) breedTimer++;
    }

    public boolean canBreed() {
        if (breedingProgress() != -1 && !this.isMale()) {
            int COUNT = (int) Direction.Type.HORIZONTAL.stream()
                    .filter(direction -> world.getBlockEntity(pos.offset(direction)) instanceof WeedCropEntity otherEntity
                            && otherEntity.isMale()
                            && this.getStrain().isResource() == otherEntity.getStrain().isResource())
                    .count();
            return COUNT > 0;
        } else {
            return false;
        }
    }

    public int growLimit() {
        if (this.cachedLimit != 0) return cachedLimit;
        if (hasGene(Phenotype.YIELD)) {
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
            this.seedGenomes = Meiosis.crossGenome(this.genome, alphaMale.genome, this.seedCount, true);
            this.seedInitialized = true;
            StrainInfo info = DnaUtil.convertStrain(this.seedGenomes.get(0), false);
            // Set strain
            log("Strain 1: " + this.getStrain());
            log("Strain 2: " + alphaMale.getStrain());
            log("Name of crossed strain: " + info.strain().name());
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

    public Genome getGenome() {
        return genome;
    }

    public List<Genome> seedGenomes() {
        return this.seedGenomes;
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
        if (genome == null) {
            this.readNbt(tag);
        }
        tag.putString("DNA", genome.toString());
        NbtList seedGenomeNbt = new NbtList();
        for (Genome genome : this.seedGenomes) {
            seedGenomeNbt.add(NbtString.of(genome.toString()));
        }
        tag.put("Seed DNA", seedGenomeNbt);
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
            this.seedGenomes = new ArrayList<>();
            for (NbtElement nbtElement : nbt.getList("Seed DNA", NbtElement.STRING_TYPE)) {
                String dnaString = nbtElement.asString();
                this.seedGenomes.add(new Genome(dnaString));
            }
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
