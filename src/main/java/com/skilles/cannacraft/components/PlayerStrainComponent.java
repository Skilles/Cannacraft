package com.skilles.cannacraft.components;

import com.skilles.cannacraft.strain.Strain;
import com.skilles.cannacraft.util.StrainUtil;
import dev.onyxstudios.cca.api.v3.component.ComponentV3;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.entity.PlayerComponent;
import net.minecraft.nbt.NbtCompound;

public class PlayerStrainComponent implements EntityInterface, ComponentV3, AutoSyncedComponent, PlayerComponent<PlayerStrainComponent> {
    Strain strain;



    @Override
    public void readFromNbt(NbtCompound tag) {

    }

    @Override
    public void writeToNbt(NbtCompound tag) {

    }

    @Override
    public void setStrain(int index) {
        strain = StrainUtil.getStrain(index);
    }

    @Override
    public Strain getStrain() {
        return strain;
    }
}
