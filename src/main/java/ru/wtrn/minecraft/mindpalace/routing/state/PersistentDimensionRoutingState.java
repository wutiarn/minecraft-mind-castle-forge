package ru.wtrn.minecraft.mindpalace.routing.state;

import com.google.common.collect.HashBiMap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import net.minecraft.core.BlockPos;
import ru.wtrn.minecraft.mindpalace.routing.RoutingNodeConnection;
import ru.wtrn.minecraft.mindpalace.util.BlockPosUtil;

import javax.annotation.Nullable;
import java.util.*;

public final class PersistentDimensionRoutingState {
    private final HashBiMap<String, String> stations = HashBiMap.create();
    private final HashMap<UUID, String> destinationByUserUUID = new HashMap<>();
    private Collection<RoutingNodeConnection> connections = new ArrayList<>();
    private final SetMultimap<String, String> bridgedStations = Multimaps.newSetMultimap(new HashMap<>(), HashSet::new);
    private final HashMap<String, String> launchBlockDestinationStations = new HashMap<>();

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

    public void addBridge(String firstStation, String secondStation) {
        bridgedStations.put(firstStation, secondStation);
        bridgedStations.put(secondStation, firstStation);
    }

    public void removeBridge(String firstStation, String secondStation) {
        bridgedStations.remove(firstStation, secondStation);
        bridgedStations.remove(secondStation, firstStation);
    }

    public SetMultimap<String, String> getBridgedStations() {
        return bridgedStations;
    }

    public void setLaunchBlockDestinationStation(BlockPos pos, String destinationStation) {
        String posStr = BlockPosUtil.blockPosToString(pos);
        if (destinationStation == null) {
            launchBlockDestinationStations.remove(posStr);
        }
        launchBlockDestinationStations.put(posStr, destinationStation);
    }

    @Nullable
    public String getDestinationForLaunchBlock(BlockPos pos) {
        return launchBlockDestinationStations.get(BlockPosUtil.blockPosToString(pos));
    }
}