package com.lion.villagersplus.init;

import com.lion.villagersplus.platform.RegistryHelper;
import com.lion.villagersplus.worldgen.OreVeinProcessor;
import com.mojang.serialization.MapCodec;

public class VPStructures {

    /**
     * Held as a constant because {@link OreVeinProcessor#getType()} has to hand back the very instance
     * that was registered - Forge only puts it into the registry later, off the mod event bus.
     */
    public static final MapCodec<OreVeinProcessor> ORE_VEIN = OreVeinProcessor.CODEC;

    static {
        RegistryHelper.registerStructureProcessorType("ore_vein", ORE_VEIN);
    }

    public static void init() {

    }
}
