package com.lion.villagersplus.client.renderer;

import com.lion.villagersplus.VillagersPlus;
import com.lion.villagersplus.blockentities.HorticulturistTableBlockEntity;
import com.lion.villagersplus.blocks.HorticulturistTableBlock;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.SegmentableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class HorticulturistTableBlockEntityRenderer implements BlockEntityRenderer<HorticulturistTableBlockEntity, HorticulturistTableBlockEntityRenderer.RenderState> {

    // Absolute X/Z positions per plant, measured from the block origin. Each plant renders inside
    // its own push()/pop(), so these never accumulate across plants.
    public static float[] twoFlowerXOffset = new float[]{
                VillagersPlus.CONFIG.first_flower_in_two_X,
                VillagersPlus.CONFIG.second_flower_in_two_X};
    public static float[] twoFlowerZOffset = new float[]{
                VillagersPlus.CONFIG.first_flower_in_two_Z,
                VillagersPlus.CONFIG.second_flower_in_two_Z};

    public static float[] threeFlowerXOffset = new float[]{
                VillagersPlus.CONFIG.first_flower_in_three_X,
                VillagersPlus.CONFIG.second_flower_in_three_X,
                VillagersPlus.CONFIG.third_flower_in_three_X};
    public static float[] threeFlowerZOffset = new float[]{
                VillagersPlus.CONFIG.first_flower_in_three_Z,
                VillagersPlus.CONFIG.second_flower_in_three_Z,
                VillagersPlus.CONFIG.third_flower_in_three_Z};

    public static float[] fourFlowerXOffset = new float[]{
                VillagersPlus.CONFIG.first_flower_in_four_X,
                VillagersPlus.CONFIG.second_flower_in_four_X,
                VillagersPlus.CONFIG.third_flower_in_four_X,
                VillagersPlus.CONFIG.forth_flower_in_four_X};
    public static float[] fourFlowerZOffset = new float[]{
                VillagersPlus.CONFIG.first_flower_in_four_Z,
                VillagersPlus.CONFIG.second_flower_in_four_Z,
                VillagersPlus.CONFIG.third_flower_in_four_Z,
                VillagersPlus.CONFIG.forth_flower_in_four_Z};

    // Slight per-plant height so a fuller tub steps down gently. Index = plant count - 1.
    private static final float[] twoFlowerY = new float[]{0.95F, 0.95F};
    private static final float[] threeFlowerY = new float[]{0.95F, 0.90F, 0.90F};
    private static final float[] fourFlowerY = new float[]{0.95F, 0.90F, 0.90F, 0.85F};

    private static final BlockDisplayContext DISPLAY_CONTEXT = BlockDisplayContext.create();

    private final BlockModelResolver blockModelResolver;

    public HorticulturistTableBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
        this.blockModelResolver = ctx.blockModelResolver();
    }

    public static class RenderState extends BlockEntityRenderState {
        public boolean valid;
        public boolean tall;
        public boolean doublePlant;
        public boolean cactus;
        public int flowers;
        public float scale = 1.0F;
        public final BlockModelRenderState[] plants = {
                new BlockModelRenderState(), new BlockModelRenderState(), new BlockModelRenderState(), new BlockModelRenderState()};
        public final BlockModelRenderState upperHalf = new BlockModelRenderState();
    }

    @Override
    public RenderState createRenderState() {
        return new RenderState();
    }

    @Override
    public void extractRenderState(HorticulturistTableBlockEntity blockEntity, RenderState state, float partialTicks,
                                   Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
        BlockState blockState = blockEntity.getBlockState();
        state.valid = blockState.getBlock() instanceof HorticulturistTableBlock;
        for (BlockModelRenderState plant : state.plants) {
            plant.clear();
        }
        state.upperHalf.clear();
        if (!state.valid) {
            return;
        }

        NonNullList<ItemStack> defaultedList = blockEntity.getInventory();
        state.scale = blockEntity.getPlantScale();
        state.tall = blockState.getValue(HorticulturistTableBlock.IS_TALL_FLOWER);
        state.flowers = blockState.getValue(HorticulturistTableBlock.FLOWERS);

        if (state.tall) {
            Block flower = Block.byItem(defaultedList.get(0).getItem());
            state.doublePlant = flower instanceof DoublePlantBlock;
            state.cactus = flower.defaultBlockState().is(Blocks.CACTUS);
            if (state.doublePlant) {
                this.blockModelResolver.update(state.plants[0], flower.defaultBlockState().setValue(DoublePlantBlock.HALF, DoubleBlockHalf.LOWER), DISPLAY_CONTEXT);
                this.blockModelResolver.update(state.upperHalf, flower.defaultBlockState().setValue(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER), DISPLAY_CONTEXT);
            } else {
                this.blockModelResolver.update(state.plants[0], displayState(flower), DISPLAY_CONTEXT);
            }
            return;
        }

        int count = Math.min(state.flowers, state.plants.length);
        for (int n = 0; n < count; n++) {
            Block flower = Block.byItem(defaultedList.get(n).getItem());
            this.blockModelResolver.update(state.plants[n], displayState(flower), DISPLAY_CONTEXT);
        }
    }

    @Override
    public void submit(RenderState state, PoseStack matrixStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (!state.valid) {
            return;
        }
        float scale = state.scale;
        int light = state.lightCoords;

        if (state.tall) {
            matrixStack.pushPose();
            if (state.doublePlant) {
                matrixStack.translate(0.0D, 0.95D, 0.0D);
                applyPlantScale(matrixStack, scale);
                state.plants[0].submit(matrixStack, collector, light, OverlayTexture.NO_OVERLAY, 0);
                matrixStack.translate(0.0D, 1.0D, 0.0D);
                state.upperHalf.submit(matrixStack, collector, light, OverlayTexture.NO_OVERLAY, 0);
            } else {
                if (state.cactus) {
                    matrixStack.scale(0.75F, 0.75F, 0.75F);
                    matrixStack.translate(0.15D, 0.15D, 0.15D);
                }
                matrixStack.translate(0.0D, 0.95D, 0.0D);
                applyPlantScale(matrixStack, scale);
                state.plants[0].submit(matrixStack, collector, light, OverlayTexture.NO_OVERLAY, 0);
            }
            matrixStack.popPose();
            return;
        }

        switch (state.flowers) {
            case 1 -> submitPlantAt(state, 0, 0.0F, 0.95F, 0.0F, matrixStack, collector);
            case 2 -> {
                for (int n = 0; n < 2; n++) {
                    submitPlantAt(state, n, twoFlowerXOffset[n], twoFlowerY[n], twoFlowerZOffset[n], matrixStack, collector);
                }
            }
            case 3 -> {
                for (int n = 0; n < 3; n++) {
                    submitPlantAt(state, n, threeFlowerXOffset[n], threeFlowerY[n], threeFlowerZOffset[n], matrixStack, collector);
                }
            }
            case 4 -> {
                for (int n = 0; n < 4; n++) {
                    submitPlantAt(state, n, fourFlowerXOffset[n], fourFlowerY[n], fourFlowerZOffset[n], matrixStack, collector);
                }
            }
            default -> {
            }
        }
    }

    private static void applyPlantScale(PoseStack matrixStack, float scale) {
        if (scale != 1.0F) {
            matrixStack.translate(0.5F * (1.0F - scale), 0.0F, 0.5F * (1.0F - scale));
            matrixStack.scale(scale, scale, scale);
        }
    }

    private static void submitPlantAt(RenderState state, int slot, float x, float y, float z, PoseStack matrixStack, SubmitNodeCollector collector) {
        matrixStack.pushPose();
        matrixStack.translate(x, y, z);
        applyPlantScale(matrixStack, state.scale);
        state.plants[slot].submit(matrixStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        matrixStack.popPose();
    }

    private static BlockState displayState(Block flower) {
        if (flower instanceof SegmentableBlock segmented) {
            return flower.defaultBlockState().setValue(segmented.getSegmentAmountProperty(), SegmentableBlock.MAX_SEGMENT);
        }
        return flower.defaultBlockState();
    }
}
