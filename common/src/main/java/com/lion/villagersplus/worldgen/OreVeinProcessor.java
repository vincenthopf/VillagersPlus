package com.lion.villagersplus.worldgen;

import com.lion.villagersplus.init.VPStructures;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.jetbrains.annotations.Nullable;

/**
 * Swaps a marker block out for a randomly drawn ore while a template is being placed.
 * <p>
 * The miner's house carries its ore seam as plain {@code chiseled_stone_bricks} so the file stays
 * readable in the structure block editor; every one of them becomes an ore here, and now and then one
 * of the raw metal blocks that ore veins are made of. Which ore a given block turns into is drawn
 * from a weighted pool, so the mix is tuned in the processor list rather than in code.
 * <p>
 * The draw is seeded from the block's world position, exactly like {@code RuleStructureProcessor}
 * does. Reaching for {@code StructurePlacementData#getRandom} instead would share the placement's own
 * random and shift every later decision that draws from it, so the same village would come out
 * differently depending on whether this processor ran.
 */
public class OreVeinProcessor extends StructureProcessor {

    // StructureProcessorType.codec() returns a MapCodec since 1.20.5, so build one directly.
    public static final MapCodec<OreVeinProcessor> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BuiltInRegistries.BLOCK.byNameCodec().fieldOf("target").forGetter(processor -> processor.target),
            WeightedList.codec(BlockState.CODEC).fieldOf("ores").forGetter(processor -> processor.ores)
    ).apply(instance, OreVeinProcessor::new));

    private final Block target;
    private final WeightedList<BlockState> ores;

    public OreVeinProcessor(Block target, WeightedList<BlockState> ores) {
        this.target = target;
        this.ores = ores;
    }

    @Override
    @Nullable
    public StructureTemplate.StructureBlockInfo processBlock(LevelReader world, BlockPos pos, BlockPos pivot,
                                                       StructureTemplate.StructureBlockInfo originalBlockInfo,
                                                       StructureTemplate.StructureBlockInfo currentBlockInfo,
                                                       StructurePlaceSettings data) {
        if (!currentBlockInfo.state().is(this.target)) {
            return currentBlockInfo;
        }

        RandomSource random = RandomSource.create(Mth.getSeed(currentBlockInfo.pos()));
        // An empty pool cannot happen through the codec, but a draw that comes back empty has to leave
        // the marker alone rather than delete it.
        return this.ores.getRandom(random)
                .map(state -> new StructureTemplate.StructureBlockInfo(currentBlockInfo.pos(), state, null))
                .orElse(currentBlockInfo);
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return VPStructures.ORE_VEIN;
    }
}
