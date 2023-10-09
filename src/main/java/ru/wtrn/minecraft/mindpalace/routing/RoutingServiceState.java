package ru.wtrn.minecraft.mindpalace.routing;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class RoutingServiceState {
    private DefaultDirectedWeightedGraph<RoutingNode, Long> graph = new DefaultDirectedWeightedGraph<>(Long.class);
    private ShortestPathAlgorithm<RoutingNode, Long> shortestPathFinder = new DijkstraShortestPath<>(graph);
    private HashMap<String, RoutingNode> nodesByName = new HashMap<>();
    private HashMap<BlockPos, RoutingNode> nodesByPosition = new HashMap<>();

    public RoutingServiceState(Collection<RoutingNode> nodes, @Nullable RoutingServiceState previous) {
        performUpdate(nodes);
        if (previous != null) {
            for (Map.Entry<String, RoutingNode> entry : previous.nodesByName.entrySet()) {
                setName(entry.getValue().pos, entry.getKey());
            }
        }
    }

    public RoutingNode setName(BlockPos pos, String name) {
        RoutingNode node = nodesByPosition.get(pos);
        if (node == null) {
            return null;
        }
        RoutingNode existingNode = nodesByName.get(name);
        if (existingNode != null) {
            existingNode.name = null;
        }
        node.name = name;
        nodesByName.put(name, node);
        return node;
    }

    public RoutingNode getByName(String name) {
        return nodesByName.get(name);
    }

    public Collection<RoutingNode> getNodes() {
        return nodesByPosition.values();
    }

    public boolean removeNode(BlockPos pos) {
        RoutingNode node = nodesByPosition.get(pos);
        if (node == null) {
            return false;
        }
        graph.removeVertex(node);
        nodesByName.remove(node.name);
        return true;
    }

    public GraphPath<RoutingNode, Long> calculateRoute(BlockPos currentPos, String targetName) {
        RoutingNode src = nodesByPosition.get(currentPos);
        if (src == null) {
            return null;
        }
        RoutingNode dst = nodesByName.get(targetName);
        if (dst == null) {
            return null;
        }
        return shortestPathFinder.getPath(src, dst);
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
