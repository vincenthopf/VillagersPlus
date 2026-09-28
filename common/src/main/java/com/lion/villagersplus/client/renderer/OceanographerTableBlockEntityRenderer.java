package com.lion.villagersplus.client.renderer;

import com.lion.villagersplus.blockentities.OceanographerTableBlockEntity;
import com.lion.villagersplus.blocks.OceanographerTableBlock;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CoralFanBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class OceanographerTableBlockEntityRenderer implements BlockEntityRenderer<OceanographerTableBlockEntity> {
    private final BlockRenderDispatcher manager;
    private final EntityRenderDispatcher entityRenderDispatcher;

    // Per-facing coral placement. Immutable and selected locally per render call: the renderer
    // instance is shared by ALL aquarium block entities, so mutating shared fields per facing
    // (as this used to do) leaked one aquarium's offsets into the next one rendered that frame.
    private static final float[] Y_OFFSET_NORTH = {0.45F, 0.75F, 0.3F, 0.45F};
    private static final float[] Y_OFFSET_SOUTH = {0.75F, 0.45F, 0.45F, 0.3F};
    private static final float[] Y_OFFSET_EAST = {0.45F, 0.3F, 0.45F, 0.75F};
    private static final float[] Y_OFFSET_WEST = {0.3F, 0.45F, 0.75F, 0.45F};

    private static final float[] X_OFFSET_DEFAULT = {0.06F, 1.5F, 1.3F, 0.1F};
    private static final float[] Z_OFFSET_DEFAULT = {0.06F, 1.5F, 0.3F, 1.45F};
    private static final float[] X_OFFSET_FAN_DEFAULT = {0.2F, 1.25F, 1.2F, 0.2F};
    private static final float[] Z_OFFSET_FAN_DEFAULT = {0.17F, 1.25F, 0.3F, 1.2F};

    private static final float[] X_OFFSET_EAST = {0.06F, 1.5F, 1.3F, 0.015F};
    private static final float[] Z_OFFSET_EAST = {0.06F, 1.5F, 0.3F, 1.5F};
    private static final float[] X_OFFSET_FAN_EAST = {0.2F, 1.25F, 1.2F, 0.115F};
    private static final float[] Z_OFFSET_FAN_EAST = {0.17F, 1.25F, 0.3F, 1.3F};

    private static final float[] X_OFFSET_WEST = {0.06F, 1.5F, 1.55F, 0.1F};
    private static final float[] Z_OFFSET_WEST = {0.06F, 1.5F, 0.15F, 1.45F};
    private static final float[] X_OFFSET_FAN_WEST = {0.2F, 1.25F, 1.45F, 0.2F};
    private static final float[] Z_OFFSET_FAN_WEST = {0.17F, 1.25F, 0.15F, 1.2F};

    /** Reused for block model rendering; the model renderer re-seeds it per face anyway. */
    private final RandomSource renderRandom = RandomSource.create();

    public OceanographerTableBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
        this.manager = ctx.getBlockRenderDispatcher();
        this.entityRenderDispatcher = ctx.getEntityRenderer();
    }

    @Override
    public void render(OceanographerTableBlockEntity blockEntity, float f, PoseStack matrixStack, MultiBufferSource vertexConsumerProvider, int i, int j, net.minecraft.world.phys.Vec3 cameraPos) {
        BlockState blockState = blockEntity.getBlockState();
        BlockPos pos = blockEntity.getBlockPos();
        Level world = blockEntity.getLevel();
        NonNullList<ItemStack> defaultedList = blockEntity.getInventory();

        if (blockState.getBlock() instanceof OceanographerTableBlock) {
            float[] yOffset;
            float[] xOffset = X_OFFSET_DEFAULT;
            float[] zOffset = Z_OFFSET_DEFAULT;
            float[] xOffsetFan = X_OFFSET_FAN_DEFAULT;
            float[] zOffsetFan = Z_OFFSET_FAN_DEFAULT;
            switch (blockState.getValue(OceanographerTableBlock.FACING)) {
                case EAST -> {
                    yOffset = Y_OFFSET_EAST;
                    xOffset = X_OFFSET_EAST;
                    zOffset = Z_OFFSET_EAST;
                    xOffsetFan = X_OFFSET_FAN_EAST;
                    zOffsetFan = Z_OFFSET_FAN_EAST;
                }
                case WEST -> {
                    yOffset = Y_OFFSET_WEST;
                    xOffset = X_OFFSET_WEST;
                    zOffset = Z_OFFSET_WEST;
                    xOffsetFan = X_OFFSET_FAN_WEST;
                    zOffsetFan = Z_OFFSET_FAN_WEST;
                }
                case SOUTH -> yOffset = Y_OFFSET_SOUTH;
                default -> yOffset = Y_OFFSET_NORTH;
            }

            float coralScale = blockEntity.getCoralScale();
            for (int it = 0; it < 4; it++) {
                // Skipped before the model path: an empty slot would otherwise cost a lookup, an
                // offset and a full render pass over air's empty model, per slot per aquarium per
                // frame.
                if (defaultedList.get(it).isEmpty()) {
                    continue;
                }
                matrixStack.pushPose();
                Block coral = Block.byItem(defaultedList.get(it).getItem());
                Vec3 offset = coral.defaultBlockState().getOffset(pos);
                matrixStack.scale(0.4F, 0.4F, 0.4F);
                if (coral instanceof CoralFanBlock) {
                    matrixStack.translate(-offset.x + xOffsetFan[it], -offset.y + yOffset[it], -offset.z + zOffsetFan[it]);
                } else {
                    matrixStack.translate(-offset.x + xOffset[it], -offset.y + yOffset[it], -offset.z + zOffset[it]);
                }
                // Bone-meal / shears size, scaled around the centre of the coral's base.
                if (coralScale != 1.0F) {
                    matrixStack.translate(0.5F * (1.0F - coralScale), 0.0F, 0.5F * (1.0F - coralScale));
                    matrixStack.scale(coralScale, coralScale, coralScale);
                }
                renderCoral(coral, world, pos, matrixStack, vertexConsumerProvider, i, j);
                matrixStack.popPose();
            }

            // A fish is drawn by the aquarium it currently swims in, not by the one that owns it -
            // see OceanographerTableBlockEntity#handOffFishToHost. So this block draws its own fish
            // only while it is still in here, plus any fish handed over by its neighbours.
            if (blockEntity.drawsOwnFish()) {
                renderFish(blockEntity, 0, 0, 0, f, matrixStack, vertexConsumerProvider, i);
            }
            for (OceanographerTableBlockEntity guest : blockEntity.getGuestFish()) {
                // The list is pruned on tick; re-check here so a fish that moved on between the
                // tick and this frame is not drawn twice.
                if (!guest.isFishInBlock(pos)) {
                    continue;
                }
                BlockPos guestPos = guest.getBlockPos();
                renderFish(guest, guestPos.getX() - pos.getX(), guestPos.getY() - pos.getY(), guestPos.getZ() - pos.getZ(),
                        f, matrixStack, vertexConsumerProvider, i);
            }
        }
    }

    /**
     * Draws {@code owner}'s fish. The entity is created and ticked by the block entity (persistent
     * animation state), so here we only place and orient it along its interpolated path.
     *
     * <p>{@code dx/dy/dz} is the offset from the block doing the drawing to the fish's owner; the
     * tank-local swim position is added on top. It is zero whenever a block draws its own fish.
     */
    private void renderFish(OceanographerTableBlockEntity owner, int dx, int dy, int dz, float f,
                            PoseStack matrixStack, MultiBufferSource vertexConsumerProvider, int light) {
        Entity fish = owner.getDisplayFish();
        if (fish == null) {
            return;
        }
        matrixStack.pushPose();

        // Fish-food / diet-food size: scale the model, and shrink the swim circle for bigger
        // animals so they stay inside the tub.
        float scale = owner.getFishScale();
        float g = 0.53125F;
        float hh = Math.max(fish.getBbWidth(), fish.getBbHeight());
        if ((double) hh > 1.0D) {
            g /= hh;
        }
        g *= scale;

        float roll = owner.getRoll(f);

        // Position comes from the block entity: a circle in a single tub, or a free
        // wander path across all connected aquarium blocks (tank-local coordinates).
        float bob = Mth.sin(owner.getAnimAge(f) * 0.1F) / 2.0F + 0.5F;
        bob += bob * bob;
        matrixStack.translate((double) (dx + owner.getFishX(f)),
                (double) (dy + owner.getFishY(f) + bob * 0.05F),
                (double) (dz + owner.getFishZ(f)));

        // Nose up/down while travelling vertically: pitch around the fish's lateral
        // axis (conjugate by the body yaw, which the entity renderer applies itself).
        float pitch = owner.getFishPitch(f);
        if (pitch != 0.0F && fish instanceof LivingEntity fishLiving) {
            float bodyYaw = Mth.rotLerp(f, fishLiving.yBodyRotO, fishLiving.yBodyRot);
            matrixStack.mulPose(Axis.YP.rotationDegrees(-bodyYaw));
            matrixStack.mulPose(Axis.XP.rotationDegrees(pitch));
            matrixStack.mulPose(Axis.YP.rotationDegrees(bodyYaw));
        }

        Vector3f vec3f = new Vector3f(0.5F, 1.0F, 0.5F);
        vec3f.normalize();
        matrixStack.mulPose((new Quaternionf()).rotationAxis(hh * 0.017453292F, vec3f));
        matrixStack.scale(g, g, g);
        matrixStack.translate(0.0D, -0.2F, 0.0D);

        // Axolotl "tumble" - a playful barrel roll around its own axis.
        if (roll != 0.0F) {
            matrixStack.mulPose(Axis.XP.rotationDegrees(roll));
        }
        if (fish instanceof Axolotl) {
            matrixStack.scale(0.8F, 0.8F, 0.8F);
        }

        // The ticked entity's bodyYaw drives the heading; f interpolates the animation.
        // Shadows off for the same reason vanilla turns them off when it draws an entity
        // outside the world: EntityRenderDispatcher casts the shadow at the entity's *world*
        // position, and the display fish is never placed, so it sits at (0, 0, 0). Near
        // spawn that stacked a shadow blob at the origin for every aquarium in range.
        this.entityRenderDispatcher.setRenderShadow(false);
        this.entityRenderDispatcher.render(fish, 0.0D, 0.0D, 0.0D, f, matrixStack, vertexConsumerProvider, light);
        this.entityRenderDispatcher.setRenderShadow(true);

        matrixStack.popPose();
    }

    /**
     * The tank itself is chunk geometry and stays visible as far as the chunk does, but everything
     * this renderer draws inside it - corals and fish - is gated by
     * {@code BlockEntityRenderDispatcher.render} on {@link #shouldRender}, which defaults to
     * 64 blocks. At that line the aquarium keeps its glass and empties out in one step. Give the
     * contents the reach the block itself has instead.
     */
    @Override
    public int getViewDistance() {
        return 128;
    }

    /// In a multi-block tank the fish may swim into neighbouring blocks, so per-section culling has
    /// to be skipped or it vanishes at chunk section borders.
    ///
    /// Unconditional, because the block entity is not passed in: opting out only for aquariums that
    /// actually hold a fish cannot be expressed here. That errs on the right side, since the
    /// alternative makes fish disappear.
    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    private void renderCoral(Block coral, Level world, BlockPos pos, PoseStack matrixStack, MultiBufferSource vertexConsumerProvider, int light, int overlay) {
        this.manager.renderSingleBlock(coral.defaultBlockState(), matrixStack, vertexConsumerProvider, light, overlay);
    }
}
