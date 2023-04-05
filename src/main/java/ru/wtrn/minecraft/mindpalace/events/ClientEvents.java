package ru.wtrn.minecraft.mindpalace.events;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import ru.wtrn.minecraft.mindpalace.WtrnMindPalaceMod;
import ru.wtrn.minecraft.mindpalace.block.entity.ModBlockEntities;
import ru.wtrn.minecraft.mindpalace.block.entity.renderer.ImageBlockEntityRenderer;
import ru.wtrn.minecraft.mindpalace.block.entity.renderer.ImageFrameEntityRenderer;
import ru.wtrn.minecraft.mindpalace.items.ModItems;

public class ClientEvents {
    @Mod.EventBusSubscriber(modid = WtrnMindPalaceMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ClientModBusEvents {
        @SubscribeEvent
        public static void registerRenderers(final EntityRenderersEvent.RegisterRenderers event) {
            event.registerBlockEntityRenderer(ModBlockEntities.IMAGE_BLOCK.get(),
                    ImageBlockEntityRenderer::new);
            event.registerEntityRenderer(ModItems.IMAGE_FRAME_ENTITY.get(),
                    ImageFrameEntityRenderer::new);
        }
    }
}
