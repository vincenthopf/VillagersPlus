package com.lion.villagersplus.platform.fabric;

import com.lion.villagersplus.VillagersPlus;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import java.util.function.Supplier;

public class RegistryHelperImpl {

    public static <T extends Block> Supplier<T> registerBlock(String name, Supplier<T> block) {
        var registry = Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath(VillagersPlus.MOD_ID, name), block.get());
        return () -> registry;
    }

    public static <T extends Item> Supplier<T> registerItem(String name, Supplier<T> item) {
        var registry = Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(VillagersPlus.MOD_ID, name), item.get());
        return () -> registry;
    }

    public static void registerItemGroup(ResourceKey<CreativeModeTab> registryKey, String name, String literalName, Supplier<Item> item) {
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, registryKey,
                FabricItemGroup.builder()
                        .title(Component.literal(literalName))
                        .icon(() -> new ItemStack(item.get())).build());
    }

    public static void addToItemGroup(ResourceKey<CreativeModeTab> itemGroup, Item item) {
        ItemGroupEvents.modifyEntriesEvent(itemGroup).register((content) -> content.accept(item.getDefaultInstance()));
    }

    public static <T extends BlockEntityType<?>> Supplier<T> registerBlockEntity(String name, Supplier<T> blockEntity) {
        var registry = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(VillagersPlus.MOD_ID, name), blockEntity.get());
        return () -> registry;
    }

    public static void registerParticleType(String name, SimpleParticleType particleType) {
        Registry.register(BuiltInRegistries.PARTICLE_TYPE, VillagersPlus.createStringID(name), particleType);
    }


    public static void registerScreenHandlerType(String name, MenuType<?> screenHandlerType) {
        Registry.register(BuiltInRegistries.MENU, ResourceLocation.fromNamespaceAndPath(VillagersPlus.MOD_ID, name), screenHandlerType);
    }

    /**
     * Fabric API dropped blockrenderlayer-v1 for 1.21.6+, and nothing ever called this: the client
     * initialiser sets the three cutout blocks itself. Kept as a no-op only because the platform
     * helper's signature is shared with NeoForge.
     */
    public static void registerRenderType(RenderType type, Block... blocks) {
    }

    public static <T extends SoundEvent> Supplier<T> registerSoundEvent(String name, Supplier<T> soundEvent) {
        var registry = Registry.register(BuiltInRegistries.SOUND_EVENT, ResourceLocation.fromNamespaceAndPath(VillagersPlus.MOD_ID, name), soundEvent.get());
        return () -> registry;
    }

    public static <T extends BlockEntity> void registerBlockEntityRenderer(Supplier<BlockEntityType<T>> type, BlockEntityRendererProvider<T> renderProvider) {
        BlockEntityRendererRegistry.register(type.get(), renderProvider);
    }

    public static <T extends PoiType> Supplier<T> registerPointOfInterestType(String name, Supplier<T> pointOfInterestType) {
        var registry = Registry.register(BuiltInRegistries.POINT_OF_INTEREST_TYPE, ResourceLocation.fromNamespaceAndPath(VillagersPlus.MOD_ID, name), pointOfInterestType.get());
        return () -> registry;
    }

    public static <T extends VillagerProfession> Supplier<T> registerVillagerProfession(String name, Supplier<T> villagerProfession) {
        var registry = Registry.register(BuiltInRegistries.VILLAGER_PROFESSION, ResourceLocation.fromNamespaceAndPath(VillagersPlus.MOD_ID, name), villagerProfession.get());
        return () -> registry;
    }

    public static void registerStructureProcessorType(String name, StructureProcessorType<?> structureProcessorType) {
        Registry.register(BuiltInRegistries.STRUCTURE_PROCESSOR, ResourceLocation.fromNamespaceAndPath(VillagersPlus.MOD_ID, name), structureProcessorType);
    }



}
