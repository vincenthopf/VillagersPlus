package com.lion.villagersplus.platform;

import net.minecraft.structure.processor.StructureProcessorType;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.village.VillagerProfession;
import net.minecraft.world.poi.PointOfInterestType;

import java.util.function.Supplier;

public class RegistryHelper {

    public static <T extends Block> Supplier<T> registerBlock(String name, Supplier<T> block) {
        return com.lion.villagersplus.platform.fabric.RegistryHelperImpl.registerBlock(name, block);
    }

    public static <T extends BlockEntityType<?>> Supplier<T> registerBlockEntity(String name, Supplier<T> blockEntity) {
        return com.lion.villagersplus.platform.fabric.RegistryHelperImpl.registerBlockEntity(name, blockEntity);
    }

    public static void registerItemGroup(RegistryKey<ItemGroup> registryKey, String name, String literalName, Supplier<Item> item) {
        com.lion.villagersplus.platform.fabric.RegistryHelperImpl.registerItemGroup(registryKey, name, literalName, item);
    }

    public static <T extends Item> Supplier<T> registerItem(String name, Supplier<T> item) {
        return com.lion.villagersplus.platform.fabric.RegistryHelperImpl.registerItem(name, item);
    }

    public static void addToItemGroup(RegistryKey<ItemGroup> itemGroup, Item item) {
        com.lion.villagersplus.platform.fabric.RegistryHelperImpl.addToItemGroup(itemGroup, item);
    }

    public static void registerParticleType(String name, SimpleParticleType particleType) {
        com.lion.villagersplus.platform.fabric.RegistryHelperImpl.registerParticleType(name, particleType);
    }

    public static void registerRenderType(RenderLayer type, Block... blocks) {
        com.lion.villagersplus.platform.fabric.RegistryHelperImpl.registerRenderType(type, blocks);
    }

    public static void registerScreenHandlerType(String name, ScreenHandlerType<?> screenHandlerType) {
        com.lion.villagersplus.platform.fabric.RegistryHelperImpl.registerScreenHandlerType(name, screenHandlerType);
    }

    public static <T extends BlockEntity> void registerBlockEntityRenderer(Supplier<BlockEntityType<T>> type, BlockEntityRendererFactory<T> renderProvider) {
        com.lion.villagersplus.platform.fabric.RegistryHelperImpl.registerBlockEntityRenderer(type, renderProvider);
    }

    public static <T extends PointOfInterestType> Supplier<T> registerPointOfInterestType(String name, Supplier<T> pointOfInterestType) {
        return com.lion.villagersplus.platform.fabric.RegistryHelperImpl.registerPointOfInterestType(name, pointOfInterestType);
    }

    public static <T extends VillagerProfession> Supplier<T> registerVillagerProfession(String name, Supplier<T> villagerProfession) {
        return com.lion.villagersplus.platform.fabric.RegistryHelperImpl.registerVillagerProfession(name, villagerProfession);
    }

    public static void registerStructureProcessorType(String name, StructureProcessorType<?> structureProcessorType) {
        com.lion.villagersplus.platform.fabric.RegistryHelperImpl.registerStructureProcessorType(name, structureProcessorType);
    }
}
