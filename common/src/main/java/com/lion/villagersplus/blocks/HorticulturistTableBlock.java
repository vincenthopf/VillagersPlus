package com.lion.villagersplus.blocks;

import com.mojang.serialization.MapCodec;
import com.lion.villagersplus.blockentities.HorticulturistTableBlockEntity;
import com.lion.villagersplus.init.VPTags;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;

public class HorticulturistTableBlock extends WorkstationBlock {
    public static final IntegerProperty FLOWERS;
    public static final BooleanProperty IS_TALL_FLOWER;
    /** Light the tub gives off, driven by what is planted in it. Read by the block's luminance. */
    public static final IntegerProperty LIGHT;

    /**
     * What one torchflower is worth in the tub. It gives off no light where it grows, and the tub
     * holds four, so this is a quarter of the way to full brightness: 4, 8, 12, 15.
     */
    private static final int TORCHFLOWER_LIGHT = 4;

    /** Brightest a tub can get, whatever is packed into it. */
    private static final int MAX_LIGHT = 15;

    /** BlockWithEntity requires a codec as of 1.20.5; this block has no state beyond its settings. */
    public static final MapCodec<HorticulturistTableBlock> CODEC = simpleCodec(HorticulturistTableBlock::new);

    @Override
    protected MapCodec<? extends HorticulturistTableBlock> codec() {
        return CODEC;
    }

    public HorticulturistTableBlock(Properties settings) {
        super(settings);
        this.registerDefaultState(this.stateDefinition.any().setValue(FLOWERS, 0).setValue(IS_TALL_FLOWER, false).setValue(LIGHT, 0));
    }

    /**
     * Runs every planted block's own ambient effect - a firefly bush's fireflies, an eyeblossom's
     * particles, whatever a future plant brings - so the tub does not have to know about any of
     * them. Anchored one block up, where the plants are actually drawn.
     */
    @Override
    public void animateTick(BlockState state, Level world, BlockPos pos, net.minecraft.util.RandomSource random) {
        if (state.getValue(FLOWERS) == 0 || !(world.getBlockEntity(pos) instanceof HorticulturistTableBlockEntity blockEntity)) {
            return;
        }

        BlockPos plantPos = pos.above();
        for (ItemStack stack : blockEntity.getInventory()) {
            if (stack.isEmpty()) {
                continue;
            }
            Block plant = Block.byItem(stack.getItem());
            if (plant != Blocks.AIR) {
                plant.animateTick(plant.defaultBlockState(), world, plantPos, random);
            }
        }
    }

    /** Every plant adds its own light, so a tub packed with them is brighter than one holding a single one. */
    private static int lightFor(HorticulturistTableBlockEntity blockEntity) {
        int light = 0;
        for (ItemStack stack : blockEntity.getInventory()) {
            if (!stack.isEmpty()) {
                light += plantLight(Block.byItem(stack.getItem()));
            }
        }
        return Math.min(light, MAX_LIGHT);
    }

    /**
     * What one plant contributes. A torchflower lights the tub even though it glows nowhere else;
     * every other plant contributes whatever light it already emits as a block, so a glowing plant
     * needs no entry here.
     */
    private static int plantLight(Block plant) {
        if (plant == Blocks.TORCHFLOWER) {
            return TORCHFLOWER_LIGHT;
        }
        return plant.defaultBlockState().getLightEmission();
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new HorticulturistTableBlockEntity(pos, state);
    }

    @Override
    public boolean isCollisionShapeFullBlock(BlockState state, BlockGetter world, BlockPos pos) {
        return false;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack itemStack, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {

        if (!(world.getBlockEntity(pos) instanceof HorticulturistTableBlockEntity blockEntity)) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }

        boolean holdingTall = itemStack.is(VPTags.TALL_PLANTABLE_ITEMS);
        boolean holdingSmall = itemStack.is(VPTags.SMALL_PLANTABLE_ITEMS);
        boolean holdingBoneMeal = itemStack.is(Items.BONE_MEAL);
        boolean holdingShears = itemStack.is(Items.SHEARS);

        // Bone meal grows the tub's plants, shears trim them back down. Handled (and consumed as
        // a use) BEFORE the removal fall-through below so neither item ever pulls a plant out.
        if ((holdingBoneMeal || holdingShears) && state.getValue(FLOWERS) > 0) {
            if (!world.isClientSide()) {
                float max = state.getValue(IS_TALL_FLOWER)
                        ? HorticulturistTableBlockEntity.MAX_TALL_PLANT_SCALE
                        : HorticulturistTableBlockEntity.MAX_PLANT_SCALE;
                float delta = holdingBoneMeal ? HorticulturistTableBlockEntity.PLANT_SCALE_STEP
                                              : -HorticulturistTableBlockEntity.PLANT_SCALE_STEP;
                if (blockEntity.adjustPlantScale(delta, max)) {
                    if (holdingBoneMeal) {
                        if (!player.getAbilities().instabuild) {
                            itemStack.shrink(1);
                        }
                    } else {
                        itemStack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));
                    }
                    world.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
                }
            } else {
                world.playSound(null, pos, holdingBoneMeal ? SoundEvents.BONE_MEAL_USE : SoundEvents.SHEEP_SHEAR, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            return InteractionResult.SUCCESS;
        }

        if (state.getValue(FLOWERS) < 4) {
            if (holdingTall && state.getValue(FLOWERS) == 0) {
                blockEntity.insertFlower(itemStack, state.getValue(FLOWERS));

                if (!world.isClientSide()) {
                    world.setBlock(pos, state.setValue(FLOWERS, 4).setValue(IS_TALL_FLOWER, true).setValue(LIGHT, lightFor(blockEntity)), 3);
                    world.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
                }

                if (world.isClientSide) {
                    world.playSound(null, pos, SoundEvents.CROP_PLANTED, SoundSource.BLOCKS, 1.0F, 1.0F);
                }

                return InteractionResult.SUCCESS;
            } else if (holdingSmall) {
                blockEntity.insertFlower(itemStack, state.getValue(FLOWERS));

                if (!world.isClientSide()) {
                    world.setBlock(pos, state.setValue(FLOWERS, state.getValue(FLOWERS) + 1).setValue(IS_TALL_FLOWER, false).setValue(LIGHT, lightFor(blockEntity)), 3);
                    world.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
                }

                if (world.isClientSide) {
                    world.playSound(null, pos, SoundEvents.CROP_PLANTED, SoundSource.BLOCKS, 1.0F, 1.0F);
                }
                return InteractionResult.SUCCESS;
            }
        }

        // Right-clicking without a plantable in hand returns the top-most plant, one per use.
        if (!holdingTall && !holdingSmall && state.getValue(FLOWERS) > 0) {
            boolean tall = state.getValue(IS_TALL_FLOWER);
            int topSlot = tall ? 0 : state.getValue(FLOWERS) - 1;

            if (!world.isClientSide()) {
                ItemStack removed = blockEntity.removeFlower(topSlot);
                if (removed.isEmpty()) {
                    return InteractionResult.TRY_WITH_EMPTY_HAND;
                }
                player.getInventory().placeItemBackInInventory(removed);
                int remaining = tall ? 0 : state.getValue(FLOWERS) - 1;
                world.setBlock(pos, state.setValue(FLOWERS, remaining).setValue(IS_TALL_FLOWER, false).setValue(LIGHT, lightFor(blockEntity)), 3);
                world.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
            }

            if (world.isClientSide) {
                world.playSound(null, pos, SoundEvents.CROP_PLANTED, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel world, BlockPos pos, boolean moved) {
        // Since 1.21.6 this only fires when the block really changed and only on the server, so
        // the old isOf(newState) guard is gone along with the newState parameter.
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof HorticulturistTableBlockEntity) {
            Containers.dropContents(world, pos, (HorticulturistTableBlockEntity)blockEntity);
        }

        super.affectNeighborsAfterRemoval(state, world, pos, moved);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level world, BlockPos pos) {
        return state.getValue(FLOWERS);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FLOWERS, IS_TALL_FLOWER, LIGHT);
    }

    static {
        IS_TALL_FLOWER = BooleanProperty.create("is_tall_flower");
        FLOWERS = IntegerProperty.create("flowers", 0, 4);
        LIGHT = IntegerProperty.create("light", 0, 15);
    }
}
