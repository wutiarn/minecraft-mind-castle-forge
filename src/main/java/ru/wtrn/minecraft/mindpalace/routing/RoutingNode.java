package ru.wtrn.minecraft.mindpalace.routing;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class RoutingNode {
    private final Logger logger = LoggerFactory.getLogger(RoutingNode.class);
    BlockPos pos;
    String name;
    Map<Direction, Connection> connections = new HashMap<>();

    public RoutingNode(BlockPos pos) {
        this.pos = pos;
    }

    public void addConnection(Direction direction, Connection connection) {
        Connection existingConnection = connections.get(direction);
        if (existingConnection != null && existingConnection.peer.pos != connection.peer.pos) {
            logger.warn("Skipping connection override attempt for direction {}. Existing connection: {}, new: {}", direction, existingConnection.peer.pos, connection.peer.pos);
            return;
        }
        connections.put(direction, connection);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("%s/%s/%s".formatted(pos.getX(), pos.getY(), pos.getZ()));
        for (Map.Entry<Direction, Connection> entry : connections.entrySet()) {
            Connection connection = entry.getValue();
            RoutingNode peer = connection.peer;
            sb.append(", %s: %s %s/%s/%s".formatted(entry.getKey(), connection.distance, peer.pos.getX(), peer.pos.getY(), peer.pos.getZ()));
        }
        return sb.toString();
    }

    public record Connection(RoutingNode peer, int distance) {
    }
}
