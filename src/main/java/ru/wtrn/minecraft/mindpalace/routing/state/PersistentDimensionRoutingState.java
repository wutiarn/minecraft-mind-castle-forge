package ru.wtrn.minecraft.mindpalace.routing.state;

import com.google.common.collect.HashBiMap;
import net.minecraft.core.BlockPos;
import ru.wtrn.minecraft.mindpalace.routing.RoutingNodeConnection;
import ru.wtrn.minecraft.mindpalace.util.BlockPosUtil;

import javax.annotation.Nullable;
import java.util.*;

public final class PersistentDimensionRoutingState {
    private final HashBiMap<String, String> stations;
    private final HashMap<UUID, String> destinationByUserUUID;
    private Collection<RoutingNodeConnection> connections;

    public PersistentDimensionRoutingState(
            HashBiMap<String, String> stations,
            HashMap<UUID, String> destinationByUserUUID,
            List<RoutingNodeConnection> connections
    ) {
        this.stations = stations;
        this.destinationByUserUUID = destinationByUserUUID;
        this.connections = connections;
    }

    public void setStationName(BlockPos pos, String name) {
        stations.put(name, BlockPosUtil.blockPosToString(pos));
    }

    public boolean removeStation(String name) {
        return stations.remove(name) != null;
    }

    public BlockPos getStationPos(String name) {
        String found = stations.get(name);
        if (found == null) {
            return null;
        }
        return BlockPosUtil.blockPosFromString(found);
    }

    @Nullable
    public String getStationName(BlockPos pos) {
        return stations.inverse().get(BlockPosUtil.blockPosToString(pos));
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

    public Map<String, String> getStations() {
        return stations;
    }

    public Collection<RoutingNodeConnection> getConnections() {
        return connections;
    }

    public void setConnections(Collection<RoutingNodeConnection> connections) {
        this.connections = connections;
    }
}