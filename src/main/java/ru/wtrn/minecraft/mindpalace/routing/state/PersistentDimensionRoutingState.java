package ru.wtrn.minecraft.mindpalace.routing.state;

import net.minecraft.core.BlockPos;

import java.util.HashMap;
import java.util.UUID;

public record PersistentDimensionRoutingState(
        HashMap<String, BlockPos> stations,
        HashMap<UUID, String> destinationByUserUUID
) {
    public void setStationName(BlockPos pos, String name) {
        stations.put(name, pos);
    }

    public void removeStation(String name) {
        stations.remove(name);
    }

    public BlockPos getStationPos(String name) {
        return stations.get(name);
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
}
