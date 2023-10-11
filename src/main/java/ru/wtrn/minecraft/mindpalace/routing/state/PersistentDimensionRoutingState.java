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

    public BlockPos getStationPos(String name) {
        return stations.get(name);
    }

    public void setUserDestination(UUID userId, String dstStationName) {
        destinationByUserUUID.put(userId, dstStationName);
    }

    public String getUserDestinationStationName(UUID userId) {
        return destinationByUserUUID.get(userId);
    }
}
