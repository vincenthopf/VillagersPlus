package com.lion.villagersplus.util;

import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.util.datafix.DataFixers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Proves the conversion on a hand written pre-1.20.5 chunk, without a world and without a launcher.
///
/// Two earlier attempts at this migration were only ever judged by opening a save and looking at a
/// block, which cannot separate "the fixer is wrong" from "the code never ran" from "the data was
/// already gone". These three cases are exactly the three reported symptoms.
class LegacyItemStacksTest {

    /// The version 1.20.1 stamps into a chunk.
    private static final int MC_1_20_1 = 3465;

    @BeforeAll
    static void bootstrap() {
        SharedConstants.tryDetectVersion();
    }

    @Test
    void aStackKeepsItsSize() {
        CompoundTag coal = migrateSingleStack("{Slot:1b,id:\"minecraft:coal\",Count:32b}");

        assertEquals(32, coal.getIntOr("count", -1), "count after migration");
        assertEquals(1, coal.getIntOr("Slot", -1), "the slot has to survive the fixer");
    }

    @Test
    void aPickaxeKeepsItsEnchantments() {
        CompoundTag pickaxe = migrateSingleStack(
                "{Slot:3b,id:\"minecraft:diamond_pickaxe\",Count:1b,"
                        + "tag:{Enchantments:[{id:\"minecraft:efficiency\",lvl:5s}],Damage:12}}");

        CompoundTag enchantments = pickaxe.getCompoundOrEmpty("components")
                .getCompoundOrEmpty("minecraft:enchantments");
        assertTrue(enchantments.toString().contains("minecraft:efficiency"),
                "enchantments after migration: " + pickaxe);
    }

    /// 1.20.5 turned the stack NBT into components and 1.21.5 lifted the fish variant out of
    /// `bucket_entity_data` into three colour and pattern components of its own, so the shape the
    /// variant arrives in depends on the target version, not on how it was stored.
    @Test
    void aBucketKeepsItsFishVariant() {
        CompoundTag bucket = migrateSingleStack(
                "{Slot:0b,id:\"minecraft:tropical_fish_bucket\",Count:1b,tag:{BucketVariantTag:117506305}}");

        CompoundTag components = bucket.getCompoundOrEmpty("components");
        assertTrue(components.contains("minecraft:tropical_fish/base_color"),
                "fish variant after migration: " + bucket);
        assertTrue(components.contains("minecraft:tropical_fish/pattern"),
                "fish variant after migration: " + bucket);
    }

    @Test
    void aBucketKeepsItsAxolotlVariant() {
        CompoundTag bucket = migrateSingleStack(
                "{Slot:0b,id:\"minecraft:axolotl_bucket\",Count:1b,tag:{Variant:2,Age:0,Health:14.0f}}");

        CompoundTag components = bucket.getCompoundOrEmpty("components");
        assertTrue(components.contains("minecraft:axolotl/variant"),
                "axolotl variant after migration: " + bucket);
    }

    @Test
    void aCurrentChunkIsLeftAlone() {
        String entry = "{Slot:1b,id:\"minecraft:coal\",count:32}";
        CompoundTag chunk = chunkWith(entry);
        int current = SharedConstants.getCurrentVersion().dataVersion().version();

        LegacyItemStacks.migrateChunk(chunk, current);

        assertEquals(parse(entry), firstStack(chunk), "a current chunk must come back untouched");
    }

    /// The migration runs before vanilla's chunk fixer, so the fixer gets the chance to undo it. It
    /// does not, because it never reaches inside a block entity whose id it has no schema for, which
    /// is the whole reason this class exists. That claim is what the two failed attempts rested on,
    /// so it is checked here rather than assumed.
    @Test
    void vanillasChunkFixerLeavesTheMigratedEntriesAlone() {
        CompoundTag chunk = chunkWith("{Slot:1b,id:\"minecraft:coal\",Count:32b}");
        chunk.putInt("DataVersion", MC_1_20_1);
        int current = SharedConstants.getCurrentVersion().dataVersion().version();

        LegacyItemStacks.migrateChunk(chunk, MC_1_20_1);
        CompoundTag afterVanilla = DataFixTypes.CHUNK.update(DataFixers.getDataFixer(), chunk, MC_1_20_1, current);

        assertEquals(32, firstStack(afterVanilla).getIntOr("count", -1),
                "vanilla's fixer must not touch the entries again: " + afterVanilla);
    }

    @Test
    void aBlockEntityOfAnotherModIsLeftAlone() {
        CompoundTag chunk = parse("{block_entities:[{id:\"someothermod:crate\",x:1,y:2,z:3,"
                + "Items:[{Slot:0b,id:\"minecraft:coal\",Count:32b}]}]}");
        CompoundTag before = firstStack(chunk).copy();

        LegacyItemStacks.migrateChunk(chunk, MC_1_20_1);

        assertEquals(before, firstStack(chunk), "only this mod's block entities may be rewritten");
    }

    private static CompoundTag migrateSingleStack(String stackSnbt) {
        CompoundTag chunk = chunkWith(stackSnbt);
        LegacyItemStacks.migrateChunk(chunk, MC_1_20_1);
        return firstStack(chunk);
    }

    private static CompoundTag chunkWith(String... stackSnbt) {
        return parse("{block_entities:[{id:\"villagersplus:ore_grinder_block_entity\",x:12,y:-40,z:98,"
                + "Items:[" + String.join(",", stackSnbt) + "]}]}");
    }

    private static CompoundTag firstStack(CompoundTag chunk) {
        ListTag blockEntities = chunk.getListOrEmpty("block_entities");
        return blockEntities.getCompoundOrEmpty(0).getListOrEmpty("Items").getCompoundOrEmpty(0);
    }

    private static CompoundTag parse(String snbt) {
        try {
            return TagParser.parseCompoundFully(snbt);
        } catch (Exception e) {
            throw new AssertionError("bad test data: " + snbt, e);
        }
    }
}
