package ru.wtrn.minecraft.mindpalace.routing;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

import java.util.*;

public class RoutingServiceState {
    private final DefaultDirectedWeightedGraph<BlockPos, RouteRailsEdge> graph = new DefaultDirectedWeightedGraph<>(RouteRailsEdge.class);
    private final ShortestPathAlgorithm<BlockPos, RouteRailsEdge> shortestPathFinder = new DijkstraShortestPath<>(graph);
    private final HashMap<String, BlockPos> stations;
    private final HashMap<UUID, String> destinationByUserUUID;

    public RoutingServiceState(Collection<RoutingNode> nodes, HashMap<String, BlockPos> stations, HashMap<UUID, String> destinationByUserUUID) {
        for (RoutingNode discoveredNode : nodes) {
            graph.addVertex(discoveredNode.pos);
            for (Map.Entry<Direction, RoutingNode.Connection> connectionEntry : discoveredNode.connections.entrySet()) {
                RoutingNode.Connection connection = connectionEntry.getValue();
                graph.addVertex(connection.peer().pos);
                graph.addEdge(discoveredNode.pos, connection.peer().pos, new RouteRailsEdge(discoveredNode.pos, connection.peer().pos, connectionEntry.getKey(), connection.distance()));
            }
        }
        this.stations = stations;
        this.destinationByUserUUID = destinationByUserUUID;
    }

    public void setName(BlockPos pos, String name) {
        stations.put(name, pos);
    }

    public BlockPos getStationPos(String name) {
        return stations.get(name);
    }

    public HashMap<String, BlockPos> getStations() {
        return stations;
    }

    public HashMap<UUID, String> getDestinationByUserUUID() {
        return destinationByUserUUID;
    }

    public Collection<BlockPos> getNodes() {
        return graph.vertexSet();
    }

    public void removeNode(BlockPos pos) {
        graph.removeVertex(pos);
    }

    public GraphPath<BlockPos, RouteRailsEdge> calculateRoute(BlockPos src, String dstName) {
        BlockPos dst = stations.get(dstName);
        if (dst == null) {
            return null;
        }
        return shortestPathFinder.getPath(src, dst);
    }

    public void setUserDestination(UUID userId, String dstStationName) {
        destinationByUserUUID.put(userId, dstStationName);
    }

    public String getUserDestinationStationName(UUID userId) {
        return destinationByUserUUID.get(userId);
    }

    public static class RouteRailsEdge extends DefaultWeightedEdge {
        private final BlockPos src;
        private final BlockPos dst;
        private final Direction direction;
        private final int distance;

        public RouteRailsEdge(BlockPos src, BlockPos dst, Direction direction, int distance) {
            this.src = src;
            this.dst = dst;
            this.direction = direction;
            this.distance = distance;
        }

        public BlockPos getSrc() {
            return src;
        }

        public BlockPos getDst() {
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
