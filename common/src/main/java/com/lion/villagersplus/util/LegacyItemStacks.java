package com.lion.villagersplus.util;

import com.lion.villagersplus.VillagersPlus;
import com.mojang.serialization.Dynamic;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.datafix.fixes.References;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/// Brings this mod's stored inventories up to the current item format when an older world is read.
///
/// Vanilla's chunk fixer walks only the block entities it has a schema for and passes a modded one
/// through untouched. Its `Items` then meet a codec that reads today's fields, ignores the ones that
/// are actually there, and hands back something diminished: a stack of any size reduced to one, a
/// bucket with no fish variant, a pickaxe with no enchantments.
///
/// The conversion is Mojang's own fixer, driven by the `DataVersion` the chunk file carries. Nothing
/// here maps an old key onto a component by hand, and nothing infers the source version from the
/// shape of the data.
public final class LegacyItemStacks {

    private static final String BLOCK_ENTITIES = "block_entities";
    private static final String ITEMS = "Items";
    private static final String SLOT = "Slot";
    private static final String ID = "id";

    /// Own logger rather than the one on [VillagersPlus], whose static setup pulls in the config and
    /// would drag a bare JVM into platform code this class does not need.
    private static final Logger LOGGER = LoggerFactory.getLogger(VillagersPlus.MOD_ID);

    /// Source version the log has already been told about. A duplicate line under a race costs
    /// nothing, so this stays a plain field.
    private static volatile int announcedVersion;

    private LegacyItemStacks() {
    }

    /// Rewrites the stored items of every block entity of this mod in one chunk read from disk.
    ///
    /// `fromVersion` is the chunk's own `DataVersion`, so it is exactly what these entries were last
    /// written by, and -1 means the file carried none. The guard is the same one vanilla applies to
    /// the chunk a moment later, so a current world walks straight back out.
    public static void migrateChunk(CompoundTag chunkNbt, int fromVersion) {
        int current = SharedConstants.getCurrentVersion().dataVersion().version();
        if (fromVersion <= 0 || fromVersion >= current) {
            return;
        }

        ListTag blockEntities = chunkNbt.getListOrEmpty(BLOCK_ENTITIES);
        for (int i = 0; i < blockEntities.size(); i++) {
            CompoundTag blockEntity = blockEntities.getCompoundOrEmpty(i);
            String id = blockEntity.getStringOr(ID, "");
            if (id.startsWith(VillagersPlus.MOD_ID + ":")) {
                announce(fromVersion, current);
                migrateInventory(blockEntity, id, fromVersion, current);
            }
        }
    }

    private static void announce(int fromVersion, int current) {
        if (announcedVersion != fromVersion) {
            announcedVersion = fromVersion;
            LOGGER.info("Migrating stored items from DataVersion {} to {}", fromVersion, current);
        }
    }

    private static void migrateInventory(CompoundTag blockEntity, String id, int from, int to) {
        ListTag items = blockEntity.getListOrEmpty(ITEMS);
        StringBuilder changed = new StringBuilder();

        for (int i = 0; i < items.size(); i++) {
            CompoundTag before = items.getCompoundOrEmpty(i);
            if (before.isEmpty()) {
                continue;
            }

            CompoundTag after;
            try {
                after = update(before, from, to);
            } catch (RuntimeException e) {
                // One entry the fixer cannot read must not take the whole chunk down, but it may not
                // disappear quietly either: without the line there is no way to tell this case apart
                // from the migration never having run.
                LOGGER.error("Could not migrate {} in {}", before, id, e);
                continue;
            }

            items.set(i, after);
            if (!changed.isEmpty()) {
                changed.append(", ");
            }
            changed.append(describe(before, after));
        }

        if (!changed.isEmpty()) {
            LOGGER.info("{} at {},{},{}: {}", id,
                    blockEntity.getIntOr("x", 0), blockEntity.getIntOr("y", 0), blockEntity.getIntOr("z", 0), changed);
        }
    }

    private static CompoundTag update(CompoundTag entry, int from, int to) {
        // The slot is the inventory's own bookkeeping and means nothing to an item fixer, so it is
        // lifted out and put back afterwards rather than fed through.
        Tag slot = entry.get(SLOT);
        CompoundTag stack = entry.copy();
        stack.remove(SLOT);

        Dynamic<Tag> fixed = DataFixers.getDataFixer().update(
                References.ITEM_STACK,
                new Dynamic<>(NbtOps.INSTANCE, stack),
                from,
                to);

        CompoundTag result = (CompoundTag) fixed.getValue();
        if (slot != null) {
            result.put(SLOT, slot);
        }
        return result;
    }

    /// One slot as `slot 3 minecraft:diamond_pickaxe 1 -> 1 [minecraft:enchantments]`, so a test run
    /// shows what the fixer made of an entry rather than only that it touched one.
    private static String describe(CompoundTag before, CompoundTag after) {
        StringBuilder line = new StringBuilder()
                .append("slot ").append(before.getIntOr(SLOT, -1))
                .append(' ').append(after.getStringOr(ID, before.getStringOr(ID, "?")))
                .append(' ').append(count(before)).append(" -> ").append(count(after));

        CompoundTag components = after.getCompoundOrEmpty("components");
        if (!components.isEmpty()) {
            line.append(' ').append(components.keySet());
        }
        return line.toString();
    }

    /// `Count` before 1.20.5, `count` after. A missing field reads as one, which is what the codec
    /// would have defaulted to and exactly the loss being repaired here.
    private static int count(CompoundTag stack) {
        int componentised = stack.getIntOr("count", -1);
        return componentised >= 0 ? componentised : stack.getIntOr("Count", 1);
    }
}
