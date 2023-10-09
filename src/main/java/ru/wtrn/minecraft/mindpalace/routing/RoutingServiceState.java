package ru.wtrn.minecraft.mindpalace.routing;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class RoutingServiceState {
    private DefaultDirectedWeightedGraph<RoutingNode, Long> graph = new DefaultDirectedWeightedGraph<>(Long.class);
    private HashMap<String, RoutingNode> nodesByName = new HashMap<>();
    private HashMap<BlockPos, RoutingNode> nodesByPosition = new HashMap<>();

    public RoutingServiceState(Collection<RoutingNode> nodes) {
        performUpdate(nodes);
    }

    public boolean setName(BlockPos pos, String name) {
        RoutingNode node = nodesByPosition.get(pos);
        if (node == null) {
            return false;
        }
        RoutingNode existingNode = nodesByName.get(name);
        if (existingNode != null) {
            existingNode.name = null;
        }
        node.name = name;
        nodesByName.put(name, node);
        return true;
    }

    public RoutingNode getByName(String name) {
        return nodesByName.get(name);
    }

    public Collection<RoutingNode> getNodes() {
        return nodesByPosition.values();
    }

    public boolean deleteNode(BlockPos pos) {
        RoutingNode node = nodesByPosition.get(pos);
        if (node == null) {
            return false;
        }
        graph.removeVertex(node);
        nodesByName.remove(node.name);
        return true;
    }

    public void performUpdate(Collection<RoutingNode> nodes) {
        for (RoutingNode discoveredNode : nodes) {
            nodesByPosition.put(discoveredNode.pos, discoveredNode);
            graph.addVertex(discoveredNode);
            for (Map.Entry<Direction, RoutingNode.Connection> connectionEntry : discoveredNode.connections.entrySet()) {
                RoutingNode.Connection connection = connectionEntry.getValue();
                graph.addVertex(connection.peer());
                graph.addEdge(discoveredNode, connection.peer(), (long) connection.distance());
            }
        }
    }
}
