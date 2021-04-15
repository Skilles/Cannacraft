package com.skilles.cannacraft.items;

import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;

public class BlockStrainComponent extends BaseStrainComponent implements AutoSyncedComponent {
    private final Object provider;
    public BlockStrainComponent(Object provider) {
        super(provider);
        this.provider = provider;
    }
}
