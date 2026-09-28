package com.lion.villagersplus.client.renderer;

import com.lion.villagersplus.VillagersPlus;
import com.lion.villagersplus.blockentities.HorticulturistTableBlockEntity;
import com.lion.villagersplus.blocks.HorticulturistTableBlock;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.SegmentableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

public class HorticulturistTableBlockEntityRenderer implements BlockEntityRenderer<HorticulturistTableBlockEntity> {

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

    private final BlockRenderDispatcher manager;

    public HorticulturistTableBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
        this.manager = ctx.getBlockRenderDispatcher();
    }

    @Override
    public void render(HorticulturistTableBlockEntity blockEntity, float f, PoseStack matrixStack, MultiBufferSource vertexConsumerProvider, int i, int j, net.minecraft.world.phys.Vec3 cameraPos) {
        BlockState blockState = blockEntity.getBlockState();
        BlockPos pos = blockEntity.getBlockPos();
        NonNullList<ItemStack> defaultedList = blockEntity.getInventory();
        Level world = blockEntity.getLevel();

        if (!(blockState.getBlock() instanceof HorticulturistTableBlock)) {
            return;
        }

        // Bone-meal / shears size; applied per plant around its base so it stays on the soil.
        float scale = blockEntity.getPlantScale();

        if (blockState.getValue(HorticulturistTableBlock.IS_TALL_FLOWER)) {
            matrixStack.pushPose();
            Block flower = Block.byItem(defaultedList.get(0).getItem());
            if (flower instanceof DoublePlantBlock) {
                matrixStack.translate(0.0D, 0.95D, 0.0D);
                // Scale once before BOTH halves so the upper half's translate happens in
                // scaled space and the two halves stay attached.
                applyPlantScale(matrixStack, scale);
                renderTallFlower(flower.defaultBlockState().getBlock(), world, pos, matrixStack, vertexConsumerProvider, true, i, j);
                matrixStack.translate(0.0D, 1.0D, 0.0D);
                renderTallFlower(flower.defaultBlockState().getBlock(), world, pos, matrixStack, vertexConsumerProvider, false, i, j);
            } else {
                Block flowerOne = Block.byItem(defaultedList.get(0).getItem());
                if (flowerOne.defaultBlockState().is(Blocks.CACTUS)) {
                    matrixStack.scale(0.75F, 0.75F, 0.75F);
                    matrixStack.translate(0.15D, 0.15D, 0.15D);
                }

                matrixStack.translate(0.0D, 0.95D, 0.0D);
                applyPlantScale(matrixStack, scale);
                renderFlower(flowerOne, world, pos, matrixStack, vertexConsumerProvider, i, j);
            }
            matrixStack.popPose();
            return;
        }

        switch (blockState.getValue(HorticulturistTableBlock.FLOWERS)) {
            case 1 -> renderFlowerAt(defaultedList, 0, 0.0F, 0.95F, 0.0F, scale, world, pos, matrixStack, vertexConsumerProvider, i, j);
            case 2 -> {
                for (int n = 0; n < 2; n++) {
                    renderFlowerAt(defaultedList, n, twoFlowerXOffset[n], twoFlowerY[n], twoFlowerZOffset[n], scale, world, pos, matrixStack, vertexConsumerProvider, i, j);
                }
            }
            case 3 -> {
                for (int n = 0; n < 3; n++) {
                    renderFlowerAt(defaultedList, n, threeFlowerXOffset[n], threeFlowerY[n], threeFlowerZOffset[n], scale, world, pos, matrixStack, vertexConsumerProvider, i, j);
                }
            }
            case 4 -> {
                for (int n = 0; n < 4; n++) {
                    renderFlowerAt(defaultedList, n, fourFlowerXOffset[n], fourFlowerY[n], fourFlowerZOffset[n], scale, world, pos, matrixStack, vertexConsumerProvider, i, j);
                }
            }
        }
    }

    /// Scales around the centre of the plant's base (block models render 0..1 from the corner).
    private static void applyPlantScale(PoseStack matrixStack, float scale) {
        if (scale != 1.0F) {
            matrixStack.translate(0.5F * (1.0F - scale), 0.0F, 0.5F * (1.0F - scale));
            matrixStack.scale(scale, scale, scale);
        }
    }

    /// Renders one plant at an absolute X/Y/Z position, isolated in its own matrix so offsets never accumulate.
    private void renderFlowerAt(NonNullList<ItemStack> list, int slot, float x, float y, float z, float scale, Level world, BlockPos pos, PoseStack matrixStack, MultiBufferSource vertexConsumerProvider, int light, int overlay) {
        Block flower = Block.byItem(list.get(slot).getItem());
        matrixStack.pushPose();
        matrixStack.translate(x, y, z);
        applyPlantScale(matrixStack, scale);
        renderFlower(flower, world, pos, matrixStack, vertexConsumerProvider, light, overlay);
        matrixStack.popPose();
    }

    private void renderFlower(Block flower, Level world, BlockPos pos, PoseStack matrixStack, MultiBufferSource vertexConsumerProvider, int light, int overlay) {
        this.manager.renderSingleBlock(displayState(flower), matrixStack, vertexConsumerProvider, light, overlay);
    }

    /// The state a plant is drawn in while it sits in the tub.
    ///
    /// Segmented plants (pink petals, wildflowers, leaf litter) draw one quadrant of the block per
    /// segment and leave the rest empty, so a single segment lands in a corner instead of on the
    /// soil. Filling every segment covers the whole slot, like every other plant model does.
    private static BlockState displayState(Block flower) {
        if (flower instanceof SegmentableBlock segmented) {
            return flower.defaultBlockState().setValue(segmented.getSegmentAmountProperty(), SegmentableBlock.MAX_SEGMENT);
        }
        return flower.defaultBlockState();
    }


    private void renderTallFlower(Block flower, Level world, BlockPos pos, PoseStack matrixStack, MultiBufferSource vertexConsumerProvider, boolean lower, int light, int overlay) {
        if (lower) {
            this.manager.renderSingleBlock(flower.defaultBlockState().setValue(DoublePlantBlock.HALF, DoubleBlockHalf.LOWER), matrixStack, vertexConsumerProvider, light, overlay);
        } else {
            this.manager.renderSingleBlock(flower.defaultBlockState().setValue(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER), matrixStack, vertexConsumerProvider, light, overlay);
        }
    }
}
