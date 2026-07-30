package com.lion.villagersplus.mixin;

import com.lion.villagersplus.util.VanillaMineshaftAttachment;
import net.minecraft.structure.StructurePiece;
import net.minecraft.util.math.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Keeps the miner's shaft and its vanilla mineshaft out of the village's terrain adaptation.
 * <p>
 * {@code StructureWeightSampler} reads the adaptation from the structure <i>start</i>, not from the
 * piece, and adds every non-pool child unconditionally. Our pieces live inside a village start, so
 * they inherit {@code beard_thin} - which carves air up to twelve blocks above each piece box and
 * packs terrain below it. Underground that turns the whole mineshaft into one long cavern: the
 * corridor walls are gone before the piece even runs, so all that is left of it is the plank floor
 * {@code tryPlaceFloor} lays down over the hole and the rails on top.
 * <p>
 * {@code intersectsChunk} is the sampler's own filter and, as of 1.20.1, its only caller in the
 * entire game - so returning false is exactly "do not adapt terrain for this piece" and nothing
 * else. Vanilla mineshafts are unaffected either way: their structure declares no adaptation, so
 * {@code NONE} keeps those starts out of the sampler before a piece is ever looked at.
 */
@Mixin(StructurePiece.class)
public class StructurePieceMixin {

    @Inject(method = "intersectsChunk", at = @At("HEAD"), cancellable = true)
    private void villagersplus$skipTerrainAdaptation(ChunkPos pos, int offset, CallbackInfoReturnable<Boolean> cir) {
        if (VanillaMineshaftAttachment.isBeardExempt((StructurePiece) (Object) this)) {
            cir.setReturnValue(false);
        }
    }
}
