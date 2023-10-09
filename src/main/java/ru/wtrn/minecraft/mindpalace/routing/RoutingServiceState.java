package ru.wtrn.minecraft.mindpalace.routing;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

import java.util.*;

public class RoutingServiceState {
    private final DefaultDirectedWeightedGraph<RoutingNode, RouteRailsEdge> graph = new DefaultDirectedWeightedGraph<>(RouteRailsEdge.class);
    private final ShortestPathAlgorithm<RoutingNode, RouteRailsEdge> shortestPathFinder = new DijkstraShortestPath<>(graph);
    private final HashMap<String, BlockPos> positionsByName = new HashMap<>();
    private final HashMap<BlockPos, RoutingNode> nodesByPosition = new HashMap<>();
    private final HashMap<UUID, String> destinationByUserUUID = new HashMap<>();

    public RoutingServiceState(Collection<RoutingNode> nodes, @Nullable RoutingServiceState previous) {
        for (RoutingNode discoveredNode : nodes) {
            nodesByPosition.put(discoveredNode.pos, discoveredNode);
            graph.addVertex(discoveredNode);
            for (Map.Entry<Direction, RoutingNode.Connection> connectionEntry : discoveredNode.connections.entrySet()) {
                RoutingNode.Connection connection = connectionEntry.getValue();
                graph.addVertex(connection.peer());
                graph.addEdge(discoveredNode, connection.peer(), new RouteRailsEdge(discoveredNode, connection.peer(), connectionEntry.getKey(), connection.distance()));
            }
        }
        if (previous != null) {
            for (Map.Entry<String, RoutingNode> entry : previous.nodesByName.entrySet()) {
                setName(entry.getValue().pos, entry.getKey());
            }
        }
    }

    public void setName(BlockPos pos, String name) {
        positionsByName.put(name, pos);
    }

    public RoutingNode getByName(String name) {
        return nodesByName.get(name);
    }

    public HashMap<String, RoutingNode> getStations() {
        return nodesByName;
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
        nodesByPosition.remove(pos);
        return true;
    }

    public GraphPath<RoutingNode, RouteRailsEdge> calculateRoute(BlockPos currentPos, String targetName) {
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

    public void setUserDestination(UUID userId, String dstStationName) {
        destinationByUserUUID.put(userId, dstStationName);
    }

    public RoutingNode getUserDestination(UUID userId) {
        String stationName = destinationByUserUUID.get(userId);
        if (stationName == null) {
            return null;
        }
        return nodesByName.get(stationName);
    }

    public static class RouteRailsEdge extends DefaultWeightedEdge {
        private final RoutingNode src;
        private final RoutingNode dst;
        private Direction direction;
        private final int distance;

        public RouteRailsEdge(RoutingNode src, RoutingNode dst, Direction direction, int distance) {
            this.src = src;
            this.dst = dst;
            this.direction = direction;
            this.distance = distance;
        }

        public RoutingNode getSrc() {
            return src;
        }

        public RoutingNode getDst() {
            return dst;
        }

        public Direction getDirection() {
            return direction;
        }

        public int getDistance() {
            return distance;
        }

        @Override
        protected Object getSource() {
            return src;
        }

        @Override
        protected Object getTarget() {
            return dst;
        }

        @Override
        protected double getWeight() {
            return distance;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            RouteRailsEdge edge = (RouteRailsEdge) o;
            return Objects.equals(src, edge.src) && Objects.equals(dst, edge.dst);
        }

        @Override
        public int hashCode() {
            return Objects.hash(src, dst);
        }
    }
}
