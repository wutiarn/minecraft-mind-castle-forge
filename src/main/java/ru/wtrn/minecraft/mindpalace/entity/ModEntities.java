package ru.wtrn.minecraft.mindpalace.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import ru.wtrn.minecraft.mindpalace.WtrnMindPalaceMod;
import ru.wtrn.minecraft.mindpalace.items.ImageFrame;
import ru.wtrn.minecraft.mindpalace.items.ImageFrameItem;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, WtrnMindPalaceMod.MOD_ID);

    public static final RegistryObject<EntityType<ImageFrame>> IMAGE_FRAME_ENTITY = ENTITY_TYPES.register("image_frame_entity",
            () -> {
                EntityType.Builder<ImageFrame> builder = EntityType.Builder.<ImageFrame>of(ImageFrame::new, MobCategory.MISC)
                        .sized(0.5F, 0.5F).clientTrackingRange(10).updateInterval(Integer.MAX_VALUE);
                return builder.build("image_frame_entity");
            });


    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }

}
