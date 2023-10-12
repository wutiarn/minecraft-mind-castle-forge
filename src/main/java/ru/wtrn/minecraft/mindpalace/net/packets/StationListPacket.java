package ru.wtrn.minecraft.mindpalace.net.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Supplier;

public class StationListPacket {
    public Collection<String> stations;

    public static void handle(StationListPacket packet, Supplier<NetworkEvent.Context> ctx) {

    }

    public static void encode(StationListPacket packet, FriendlyByteBuf buf) {
        buf.writeInt(packet.stations.size());
        for (String station : packet.stations) {
            buf.writeInt(station.length());
            buf.writeCharSequence(station, StandardCharsets.UTF_8);
        }
    }

    public static StationListPacket decode(FriendlyByteBuf buf) {
        int length = buf.readInt();
        ArrayList<String> stations = new ArrayList<>(length);
        for (int i = 0; i < length; i++) {
            int stationNameLength = buf.readInt();
            String stationName = buf.readCharSequence(stationNameLength, StandardCharsets.UTF_8).toString();
            stations.add(stationName);
        }
        StationListPacket packet = new StationListPacket();
        packet.stations = stations;
        return packet;
    }
}
