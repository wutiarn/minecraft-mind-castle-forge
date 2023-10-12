package ru.wtrn.minecraft.mindpalace.net.packets;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.wtrn.minecraft.mindpalace.commands.argument.StationNameArgumentType;
import ru.wtrn.minecraft.mindpalace.events.EventsHandler;
import ru.wtrn.minecraft.mindpalace.net.ModPacketHandler;
import ru.wtrn.minecraft.mindpalace.routing.RoutingService;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.function.Supplier;

public class StationListPacket {
    private static final Logger logger = LoggerFactory.getLogger(EventsHandler.class);
    public Collection<String> stations;

    public StationListPacket(Collection<String> stations) {
        this.stations = stations;
    }

    public static void sendStationsToPlayer(ServerPlayer player) {
        Map<String, BlockPos> stations = RoutingService.INSTANCE.getStations(player.level());
        StationListPacket packet = new StationListPacket(stations.keySet());
        ModPacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    public static void sendStationsToLevel(Level level, Collection<String> stations) {
        StationListPacket packet = new StationListPacket(stations);
        ModPacketHandler.INSTANCE.send(PacketDistributor.DIMENSION.with(level::dimension), packet);
    }

    public static void handle(StationListPacket packet, Supplier<NetworkEvent.Context> ctx) {
        if (FMLEnvironment.dist != Dist.CLIENT) {
            return;
        }
        logger.info("Received stations list: {}", packet.stations);
        StationNameArgumentType.setStationNames(packet.stations);
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
        return new StationListPacket(stations);
    }
}
