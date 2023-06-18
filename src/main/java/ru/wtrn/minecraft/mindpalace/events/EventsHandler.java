package ru.wtrn.minecraft.mindpalace.events;

import net.minecraft.data.DataProvider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import ru.wtrn.minecraft.mindpalace.WtrnMindPalaceMod;
import ru.wtrn.minecraft.mindpalace.client.renderer.ImageFrameEntityRenderer;
import ru.wtrn.minecraft.mindpalace.commands.ImageFrameCommand;
import ru.wtrn.minecraft.mindpalace.entity.ModEntities;
import ru.wtrn.minecraft.mindpalace.tags.ModBlockTagsProvider;

public class EventsHandler {
    @Mod.EventBusSubscriber(modid = WtrnMindPalaceMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModBusEvents {
        @SubscribeEvent
        public static void registerRenderers(final EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(ModEntities.IMAGE_FRAME_ENTITY.get(),
                    ImageFrameEntityRenderer::new);
        }

        @SubscribeEvent
        public static void gatherData(GatherDataEvent event) {
            event.getGenerator().addProvider(
                    event.includeServer(),
                    (DataProvider.Factory<ModBlockTagsProvider>) output -> new ModBlockTagsProvider(
                            output,
                            event.getLookupProvider(),
                            WtrnMindPalaceMod.MOD_ID,
                            event.getExistingFileHelper()
                    )
            );
        }
    }

    @Mod.EventBusSubscriber(modid = WtrnMindPalaceMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class ClientForgeBusEvents {
        @SubscribeEvent
        public static void registerCommands(final RegisterClientCommandsEvent event) {
            ImageFrameCommand.registerClientCommands(event.getDispatcher());
        }
    }

    @Mod.EventBusSubscriber(modid = WtrnMindPalaceMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class ServerForgeBusEvents {
        @SubscribeEvent
        public static void registerCommands(final RegisterCommandsEvent event) {
            ImageFrameCommand.register(event.getDispatcher());
        }
    }
}
