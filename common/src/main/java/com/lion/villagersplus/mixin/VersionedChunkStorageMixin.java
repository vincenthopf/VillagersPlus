package com.lion.villagersplus.mixin;

import com.lion.villagersplus.util.LegacyItemStacks;
import com.mojang.serialization.MapCodec;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.storage.ChunkStorage;
import net.minecraft.world.level.storage.DimensionDataStorage;

/// Updates this mod's stored inventories on the way in from an older world.
///
/// This is the one place where a chunk still carries the `DataVersion` it was written with, so the
/// conversion knows its exact starting point instead of guessing it from the shape of an entry.
///
/// A block entity is not a usable hook here. A full chunk hands its own out through
/// `keepPacked`: only an entry that was never instantiated goes to `WorldChunk.loadBlockEntity`,
/// every workstation the player has opened goes straight to `BlockEntity.createFromNbt`.
///
/// Running before vanilla's own fixer is safe because that fixer never reaches inside a block entity
/// whose id it does not know, which is the very reason this class has to exist.
@Mixin(ChunkStorage.class)
public class VersionedChunkStorageMixin {

    @Inject(method = "upgradeChunkTag", at = @At("HEAD"))
    private void villagersplus$migrateStoredItems(ResourceKey<Level> worldKey,
                                                  Supplier<DimensionDataStorage> persistentStateManagerFactory,
                                                  CompoundTag nbt,
                                                  Optional<ResourceKey<MapCodec<? extends ChunkGenerator>>> generatorCodecKey,
                                                  CallbackInfoReturnable<CompoundTag> cir) {
        LegacyItemStacks.migrateChunk(nbt, ChunkStorage.getVersion(nbt));
    }
}
