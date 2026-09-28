package com.lion.villagersplus.platform;

import java.util.function.Supplier;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;

public class RegistryHelper {

    public static <T extends Block> Supplier<T> registerBlock(String name, Supplier<T> block) {
        return com.lion.villagersplus.platform.fabric.RegistryHelperImpl.registerBlock(name, block);
    }

    public static <T extends BlockEntityType<?>> Supplier<T> registerBlockEntity(String name, Supplier<T> blockEntity) {
        return com.lion.villagersplus.platform.fabric.RegistryHelperImpl.registerBlockEntity(name, blockEntity);
    }

    public static void registerItemGroup(ResourceKey<CreativeModeTab> registryKey, String name, String literalName, Supplier<Item> item) {
        com.lion.villagersplus.platform.fabric.RegistryHelperImpl.registerItemGroup(registryKey, name, literalName, item);
    }

    public static <T extends Item> Supplier<T> registerItem(String name, Supplier<T> item) {
        return com.lion.villagersplus.platform.fabric.RegistryHelperImpl.registerItem(name, item);
    }

    public static void addToItemGroup(ResourceKey<CreativeModeTab> itemGroup, Item item) {
        com.lion.villagersplus.platform.fabric.RegistryHelperImpl.addToItemGroup(itemGroup, item);
    }

    public static void registerParticleType(String name, SimpleParticleType particleType) {
        com.lion.villagersplus.platform.fabric.RegistryHelperImpl.registerParticleType(name, particleType);
    }

    public static void registerRenderType(RenderType type, Block... blocks) {
        com.lion.villagersplus.platform.fabric.RegistryHelperImpl.registerRenderType(type, blocks);
    }

    public static void registerScreenHandlerType(String name, MenuType<?> screenHandlerType) {
        com.lion.villagersplus.platform.fabric.RegistryHelperImpl.registerScreenHandlerType(name, screenHandlerType);
    }

    public static <T extends BlockEntity> void registerBlockEntityRenderer(Supplier<BlockEntityType<T>> type, BlockEntityRendererProvider<T> renderProvider) {
        com.lion.villagersplus.platform.fabric.RegistryHelperImpl.registerBlockEntityRenderer(type, renderProvider);
    }

    public static <T extends PoiType> Supplier<T> registerPointOfInterestType(String name, Supplier<T> pointOfInterestType) {
        return com.lion.villagersplus.platform.fabric.RegistryHelperImpl.registerPointOfInterestType(name, pointOfInterestType);
    }

    public static <T extends VillagerProfession> Supplier<T> registerVillagerProfession(String name, Supplier<T> villagerProfession) {
        return com.lion.villagersplus.platform.fabric.RegistryHelperImpl.registerVillagerProfession(name, villagerProfession);
    }

    public static void registerStructureProcessorType(String name, StructureProcessorType<?> structureProcessorType) {
        com.lion.villagersplus.platform.fabric.RegistryHelperImpl.registerStructureProcessorType(name, structureProcessorType);
    }
}
