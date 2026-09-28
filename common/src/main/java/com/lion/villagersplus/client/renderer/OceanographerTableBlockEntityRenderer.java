package com.lion.villagersplus.client.renderer;

import com.lion.villagersplus.blockentities.OceanographerTableBlockEntity;
import com.lion.villagersplus.blocks.OceanographerTableBlock;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CoralFanBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

public class OceanographerTableBlockEntityRenderer implements BlockEntityRenderer<OceanographerTableBlockEntity, OceanographerTableBlockEntityRenderer.RenderState> {
    private static final BlockDisplayContext DISPLAY_CONTEXT = BlockDisplayContext.create();

    private final BlockModelResolver blockModelResolver;
    private final EntityRenderDispatcher entityRenderDispatcher;

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

    public OceanographerTableBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
        this.blockModelResolver = ctx.blockModelResolver();
        this.entityRenderDispatcher = ctx.entityRenderer();
    }

    public static class CoralState {
        public final BlockModelRenderState model = new BlockModelRenderState();
        public boolean present;
        public boolean fan;
        public Vec3 offset = Vec3.ZERO;
    }

    public static class FishState {
        public EntityRenderState entity;
        public float x;
        public float y;
        public float z;
        public float g;
        public float hh;
        public float roll;
        public float pitch;
        public float bodyYaw;
        public boolean living;
        public boolean axolotl;
    }

    public static class RenderState extends BlockEntityRenderState {
        public boolean valid;
        public float[] yOffset = Y_OFFSET_NORTH;
        public float[] xOffset = X_OFFSET_DEFAULT;
        public float[] zOffset = Z_OFFSET_DEFAULT;
        public float[] xOffsetFan = X_OFFSET_FAN_DEFAULT;
        public float[] zOffsetFan = Z_OFFSET_FAN_DEFAULT;
        public float coralScale = 1.0F;
        public final CoralState[] corals = {new CoralState(), new CoralState(), new CoralState(), new CoralState()};
        public final List<FishState> fish = new ArrayList<>();
    }

    @Override
    public RenderState createRenderState() {
        return new RenderState();
    }

    @Override
    public void extractRenderState(OceanographerTableBlockEntity blockEntity, RenderState state, float f,
                                   Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, f, cameraPosition, breakProgress);
        BlockState blockState = blockEntity.getBlockState();
        BlockPos pos = blockEntity.getBlockPos();
        state.fish.clear();
        state.valid = blockState.getBlock() instanceof OceanographerTableBlock;
        if (!state.valid) {
            return;
        }

        state.xOffset = X_OFFSET_DEFAULT;
        state.zOffset = Z_OFFSET_DEFAULT;
        state.xOffsetFan = X_OFFSET_FAN_DEFAULT;
        state.zOffsetFan = Z_OFFSET_FAN_DEFAULT;
        switch (blockState.getValue(OceanographerTableBlock.FACING)) {
            case EAST -> {
                state.yOffset = Y_OFFSET_EAST;
                state.xOffset = X_OFFSET_EAST;
                state.zOffset = Z_OFFSET_EAST;
                state.xOffsetFan = X_OFFSET_FAN_EAST;
                state.zOffsetFan = Z_OFFSET_FAN_EAST;
            }
            case WEST -> {
                state.yOffset = Y_OFFSET_WEST;
                state.xOffset = X_OFFSET_WEST;
                state.zOffset = Z_OFFSET_WEST;
                state.xOffsetFan = X_OFFSET_FAN_WEST;
                state.zOffsetFan = Z_OFFSET_FAN_WEST;
            }
            case SOUTH -> state.yOffset = Y_OFFSET_SOUTH;
            default -> state.yOffset = Y_OFFSET_NORTH;
        }

        state.coralScale = blockEntity.getCoralScale();
        NonNullList<ItemStack> defaultedList = blockEntity.getInventory();
        for (int it = 0; it < 4; it++) {
            CoralState coralState = state.corals[it];
            coralState.present = !defaultedList.get(it).isEmpty();
            if (!coralState.present) {
                coralState.model.clear();
                continue;
            }
            Block coral = Block.byItem(defaultedList.get(it).getItem());
            coralState.fan = coral instanceof CoralFanBlock;
            coralState.offset = coral.defaultBlockState().getOffset(pos);
            this.blockModelResolver.update(coralState.model, coral.defaultBlockState(), DISPLAY_CONTEXT);
        }

        if (blockEntity.drawsOwnFish()) {
            extractFish(state, blockEntity, 0, 0, 0, f);
        }
        for (OceanographerTableBlockEntity guest : blockEntity.getGuestFish()) {
            if (!guest.isFishInBlock(pos)) {
                continue;
            }
            BlockPos guestPos = guest.getBlockPos();
            extractFish(state, guest, guestPos.getX() - pos.getX(), guestPos.getY() - pos.getY(), guestPos.getZ() - pos.getZ(), f);
        }
    }

    private void extractFish(RenderState state, OceanographerTableBlockEntity owner, int dx, int dy, int dz, float f) {
        Entity fish = owner.getDisplayFish();
        if (fish == null) {
            return;
        }
        FishState fishState = new FishState();
        float scale = owner.getFishScale();
        float g = 0.53125F;
        float hh = Math.max(fish.getBbWidth(), fish.getBbHeight());
        if ((double) hh > 1.0D) {
            g /= hh;
        }
        fishState.g = g * scale;
        fishState.hh = hh;
        fishState.roll = owner.getRoll(f);
        float bob = Mth.sin(owner.getAnimAge(f) * 0.1F) / 2.0F + 0.5F;
        bob += bob * bob;
        fishState.x = dx + owner.getFishX(f);
        fishState.y = dy + owner.getFishY(f) + bob * 0.05F;
        fishState.z = dz + owner.getFishZ(f);
        fishState.pitch = owner.getFishPitch(f);
        if (fish instanceof LivingEntity fishLiving) {
            fishState.living = true;
            fishState.bodyYaw = Mth.rotLerp(f, fishLiving.yBodyRotO, fishLiving.yBodyRot);
        }
        fishState.axolotl = fish instanceof Axolotl;
        fishState.entity = this.entityRenderDispatcher.extractEntity(fish, f);
        fishState.entity.lightCoords = state.lightCoords;
        fishState.entity.shadowPieces.clear();
        state.fish.add(fishState);
    }

    @Override
    public void submit(RenderState state, PoseStack matrixStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (!state.valid) {
            return;
        }
        float coralScale = state.coralScale;
        for (int it = 0; it < 4; it++) {
            CoralState coral = state.corals[it];
            if (!coral.present) {
                continue;
            }
            matrixStack.pushPose();
            Vec3 offset = coral.offset;
            matrixStack.scale(0.4F, 0.4F, 0.4F);
            if (coral.fan) {
                matrixStack.translate(-offset.x + state.xOffsetFan[it], -offset.y + state.yOffset[it], -offset.z + state.zOffsetFan[it]);
            } else {
                matrixStack.translate(-offset.x + state.xOffset[it], -offset.y + state.yOffset[it], -offset.z + state.zOffset[it]);
            }
            if (coralScale != 1.0F) {
                matrixStack.translate(0.5F * (1.0F - coralScale), 0.0F, 0.5F * (1.0F - coralScale));
                matrixStack.scale(coralScale, coralScale, coralScale);
            }
            coral.model.submit(matrixStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            matrixStack.popPose();
        }

        for (FishState fish : state.fish) {
            submitFish(fish, matrixStack, collector, camera);
        }
    }

    private void submitFish(FishState fish, PoseStack matrixStack, SubmitNodeCollector collector, CameraRenderState camera) {
        matrixStack.pushPose();
        matrixStack.translate((double) fish.x, (double) fish.y, (double) fish.z);
        if (fish.pitch != 0.0F && fish.living) {
            matrixStack.rotate(Axis.YP.rotationDegrees(-fish.bodyYaw));
            matrixStack.rotate(Axis.XP.rotationDegrees(fish.pitch));
            matrixStack.rotate(Axis.YP.rotationDegrees(fish.bodyYaw));
        }
        Vector3f vec3f = new Vector3f(0.5F, 1.0F, 0.5F);
        vec3f.normalize();
        matrixStack.rotate((new Quaternionf()).rotationAxis(fish.hh * 0.017453292F, vec3f));
        matrixStack.scale(fish.g, fish.g, fish.g);
        matrixStack.translate(0.0D, -0.2F, 0.0D);
        if (fish.roll != 0.0F) {
            matrixStack.rotate(Axis.XP.rotationDegrees(fish.roll));
        }
        if (fish.axolotl) {
            matrixStack.scale(0.8F, 0.8F, 0.8F);
        }
        this.entityRenderDispatcher.submit(fish.entity, camera, 0.0D, 0.0D, 0.0D, matrixStack, collector);
        matrixStack.popPose();
    }

    @Override
    public int getViewDistance() {
        return 128;
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }
}
