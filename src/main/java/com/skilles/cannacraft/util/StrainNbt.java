package com.skilles.cannacraft.util;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;

import java.util.stream.Stream;

public interface StrainNbt {
    String ID = "ID";

    static Stream<NbtCompound> stream(NbtList list) {
        return list.stream().filter(tag -> tag instanceof NbtCompound).map(tag -> (NbtCompound) tag);
    }
}
