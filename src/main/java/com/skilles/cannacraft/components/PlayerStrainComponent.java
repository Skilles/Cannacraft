package com.skilles.cannacraft.components;

import com.skilles.cannacraft.strain.Strain;
import com.skilles.cannacraft.strain.StrainMap;
import dev.onyxstudios.cca.api.v3.component.ComponentV3;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.entity.PlayerComponent;
import net.minecraft.nbt.CompoundTag;

public class PlayerStrainComponent implements EntityInterface, ComponentV3, AutoSyncedComponent, PlayerComponent<PlayerStrainComponent> {
    Strain strain;


    @Override
    public void readFromNbt(CompoundTag tag) {

    }

    @Override
    public void writeToNbt(CompoundTag tag) {

    }

    @Override
    public Strain getStrain() {
        return strain;
    }

    @Override
    public void setStrain(int index) {
        strain = StrainMap.getStrain(index);
    }
}
