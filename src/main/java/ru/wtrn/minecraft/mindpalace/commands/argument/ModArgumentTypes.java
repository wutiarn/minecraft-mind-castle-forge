package ru.wtrn.minecraft.mindpalace.commands.argument;

import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import ru.wtrn.minecraft.mindpalace.WtrnMindPalaceMod;

public class ModArgumentTypes {

    public static void register(IEventBus eventBus) {
        StationNameArgumentSerializer stationNameArgumentInfo = new StationNameArgumentSerializer();

        ArgumentTypeInfos.registerByClass(StationNameArgumentType.class, stationNameArgumentInfo);

        DeferredRegister<ArgumentTypeInfo<?, ?>> registry = DeferredRegister.create(ForgeRegistries.COMMAND_ARGUMENT_TYPES, WtrnMindPalaceMod.MOD_ID);
        registry.register("station_name", () -> stationNameArgumentInfo);

        registry.register(eventBus);
    }
}
