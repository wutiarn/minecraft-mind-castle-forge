package ru.wtrn.minecraft.mindpalace.routing.state;

import com.google.common.collect.HashBiMap;
import com.google.gson.InstanceCreator;
import net.minecraft.core.BlockPos;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public record PersistentDimensionRoutingState(
        HashBiMap<String, BlockPos> stations,
        HashMap<UUID, String> destinationByUserUUID
) {
    @SuppressWarnings("unused")
    public PersistentDimensionRoutingState(Map<String, BlockPos> stations, HashMap<UUID, String> destinationByUserUUID) {
        this(HashBiMap.create(stations), destinationByUserUUID);
    }

    public void setStationName(BlockPos pos, String name) {
        stations.put(name, pos);
    }

    public void removeStation(String name) {
        stations.remove(name);
    }

    public BlockPos getStationPos(String name) {
        return stations.get(name);
    }

    @Nullable
    public String getStationName(BlockPos name) {
        return stations.inverse().get(name);
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