package ru.wtrn.minecraft.mindpalace.net;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import ru.wtrn.minecraft.mindpalace.WtrnMindPalaceMod;
import ru.wtrn.minecraft.mindpalace.net.packets.StationListPacket;

public class WtrnMindPalaceModPacketHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(WtrnMindPalaceMod.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static void register() {
        INSTANCE.registerMessage(1, StationListPacket.class, StationListPacket::encode, StationListPacket::decode, StationListPacket::handle);
    }

}
