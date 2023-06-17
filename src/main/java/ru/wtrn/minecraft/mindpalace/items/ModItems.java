package ru.wtrn.minecraft.mindpalace.items;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import ru.wtrn.minecraft.mindpalace.WtrnMindPalaceMod;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, WtrnMindPalaceMod.MOD_ID);

    public static final RegistryObject<ImageFrameItem> IMAGE_FRAME_ITEM = ITEMS.register("image_frame_item",
            () -> new ImageFrameItem(new Item.Properties().stacksTo(1)));


    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }

}
