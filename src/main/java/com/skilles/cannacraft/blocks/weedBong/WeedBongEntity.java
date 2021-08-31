package com.skilles.cannacraft.blocks.weedBong;

import com.skilles.cannacraft.registry.ModEntities;
import com.skilles.cannacraft.strain.StrainInfo;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import static com.skilles.cannacraft.Cannacraft.log;

public class WeedBongEntity extends BlockEntity {


    public WeedBongEntity(BlockPos pos, BlockState state) {
        super(ModEntities.WEED_BONG_ENTITY, pos, state);
    }

    int timer = 0;
    int hitTimer = 0;
    int cooldownTimer = 0;
    static final int MAX_LIT = 1000;

    int hits;
    int clicks;
    StrainInfo packedInfo;
    boolean packed;
    boolean lit;
    boolean startedHitting;
    boolean cooldown;

    public static void tick(World world, BlockPos blockPos, BlockState blockState, WeedBongEntity be) {
        if(be.packed && be.lit && !world.isClient) {
            Direction bowlDirection = blockState.get(WeedBong.FACING);
            if(be.timer % 10 == 0) ((ServerWorld) world).spawnParticles(ParticleTypes.SMOKE, blockPos.getX() + 0.5 + bowlDirection.getOffsetX() * 0.25, blockPos.getY() + 0.33, blockPos.getZ() + 0.5 + bowlDirection.getOffsetZ() * 0.25, 1, 0, 0, 0, 0);

            if(be.startedHitting && ++be.hitTimer >= 20) {
                be.hitTimer = 0;
                be.startedHitting = false;
                be.clicks = 0;
                log("Finished hit timer");
            }
            if(!be.startedHitting && be.clicks != 0) {
                be.clicks = 0;
            }
            if(be.cooldown && ++be.cooldownTimer >= 100) {
                be.cooldownTimer = 0;
                be.cooldown = false;
                log("Finished cooldown timer");
            }
            if(++be.timer >= MAX_LIT) {
                be.timer = 0;
                be.lit = false;
            }
        }
    }
    void startHitting() {
        startedHitting = true;
        clicks = 0;
    }


}
