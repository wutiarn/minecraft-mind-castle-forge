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
    Map<Direction, RoutingNode> connections = new HashMap<>();

    public RoutingNode(BlockPos pos) {
        this.pos = pos;
    }

    public void addConnection(Direction direction, RoutingNode node) {
        RoutingNode existingConnection = connections.get(direction);
        if (existingConnection != null && existingConnection.pos != node.pos) {
            logger.warn("Skipping connection override attempt for direction {}. Existing connection: {}, new: {}", direction, existingConnection.pos, node.pos);
            return;
        }
        connections.put(direction, node);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("%s/%s/%s".formatted(pos.getX(), pos.getY(), pos.getZ()));
        for (Map.Entry<Direction, RoutingNode> entry : connections.entrySet()) {
            RoutingNode node = entry.getValue();
            sb.append(", %s: %s/%s/%s".formatted(entry.getKey(), node.pos.getX(), node.pos.getY(), node.pos.getZ()));
        }
        return sb.toString();
    }
}
