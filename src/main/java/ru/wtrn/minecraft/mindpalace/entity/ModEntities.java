package ru.wtrn.minecraft.mindpalace.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import ru.wtrn.minecraft.mindpalace.WtrnMindPalaceMod;

import static ru.wtrn.minecraft.mindpalace.block.ModBlocks.ROUTING_RAIL_BLOCK;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, WtrnMindPalaceMod.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, WtrnMindPalaceMod.MOD_ID);

    public static final RegistryObject<EntityType<ImageFrame>> IMAGE_FRAME_ENTITY = ENTITY_TYPES.register("image_frame_entity",
            () -> {
                EntityType.Builder<ImageFrame> builder = EntityType.Builder.<ImageFrame>of(ImageFrame::new, MobCategory.MISC)
                        .sized(0.5F, 0.5F).clientTrackingRange(10).updateInterval(Integer.MAX_VALUE);
                return builder.build("image_frame_entity");
            });
    public static final RegistryObject<EntityType<FastMinecart>> FAST_MINECART_ENTITY = ENTITY_TYPES.register("fast_minecart_entity",
            () -> {
                EntityType.Builder<FastMinecart> builder = EntityType.Builder.<FastMinecart>of(FastMinecart::new, MobCategory.MISC)
                        .sized(0.98F, 0.7F).clientTrackingRange(8);
                return builder.build("fast_minecart_entity");
            });

    public static final RegistryObject<BlockEntityType<RoutingRailBlockEntity>> ROUTING_RAIL_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("routing_rail_block_entity",
            () -> {
                BlockEntityType.Builder<RoutingRailBlockEntity> builder = BlockEntityType.Builder.of(RoutingRailBlockEntity::new, ROUTING_RAIL_BLOCK.get());
                return builder.build(null);
            });


    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
        BLOCK_ENTITY_TYPES.register(eventBus);
    }

}
