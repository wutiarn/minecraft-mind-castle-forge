package ru.wtrn.minecraft.mindpalace.items;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.CreativeModeTabEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import ru.wtrn.minecraft.mindpalace.WtrnMindPalaceMod;

@Mod.EventBusSubscriber(modid = WtrnMindPalaceMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModCreativeModeTab {
    public static CreativeModeTab MIND_PALACE_TAB;

    @SubscribeEvent
    public static void registerCreativeModeTabs(CreativeModeTabEvent.Register event) {
        MIND_PALACE_TAB = event.registerCreativeModeTab(new ResourceLocation(WtrnMindPalaceMod.MOD_ID, "mind_palace_tab"),
                builder -> builder.icon(() -> new ItemStack(Items.PAINTING)).title(Component.literal("Mind Palace")).build());
    }
}
