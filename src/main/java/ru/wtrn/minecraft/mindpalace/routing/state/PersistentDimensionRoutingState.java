package ru.wtrn.minecraft.mindpalace.routing.state;

import com.google.common.collect.HashBiMap;
import net.minecraft.core.BlockPos;
import ru.wtrn.minecraft.mindpalace.routing.RoutingNodeConnection;

import javax.annotation.Nullable;
import java.util.*;

public final class PersistentDimensionRoutingState {
    private final HashBiMap<String, BlockPos> stations;
    private final HashMap<UUID, String> destinationByUserUUID;
    private Collection<RoutingNodeConnection> connections;

    public PersistentDimensionRoutingState(
            HashBiMap<String, BlockPos> stations,
            HashMap<UUID, String> destinationByUserUUID,
            List<RoutingNodeConnection> connections
    ) {
        this.stations = stations;
        this.destinationByUserUUID = destinationByUserUUID;
        this.connections = connections;
    }

    public void setStationName(BlockPos pos, String name) {
        stations.put(name, pos);
    }

    public boolean removeStation(String name) {
        return stations.remove(name) != null;
    }

    public BlockPos getStationPos(String name) {
        return stations.get(name);
    }

    @Nullable
    public String getStationName(BlockPos pos) {
        return stations.inverse().get(pos);
    }

    public void setUserDestination(UUID userId, String dstStationName) {
        if (dstStationName == null) {
            destinationByUserUUID.remove(userId);
        }
        destinationByUserUUID.put(userId, dstStationName);
    }

    public String getUserDestinationStationName(UUID userId) {
        return destinationByUserUUID.get(userId);
    }

    public Map<String, BlockPos> getStations() {
        return stations;
    }

    public Collection<RoutingNodeConnection> getConnections() {
        return connections;
    }

    public void setConnections(Collection<RoutingNodeConnection> connections) {
        this.connections = connections;
    }
}