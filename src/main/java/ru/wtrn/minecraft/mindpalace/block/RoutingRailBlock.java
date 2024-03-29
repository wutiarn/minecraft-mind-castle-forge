package ru.wtrn.minecraft.mindpalace.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RailBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.jgrapht.GraphPath;
import ru.wtrn.minecraft.mindpalace.entity.RoutingRailBlockEntity;
import ru.wtrn.minecraft.mindpalace.routing.RouteRailsEdge;
import ru.wtrn.minecraft.mindpalace.routing.RoutingService;
import ru.wtrn.minecraft.mindpalace.util.MinecartUtil;
import ru.wtrn.minecraft.mindpalace.util.RailTraverser;
import ru.wtrn.minecraft.mindpalace.util.math.vec.VectorUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class RoutingRailBlock extends RailBlock implements EntityBlock {
    public RoutingRailBlock() {
        super(Properties.of().noCollission().strength(0.7F).sound(SoundType.METAL));
    }

    @Override
    public RailShape getRailDirection(BlockState state, BlockGetter world, BlockPos pos, @Nullable AbstractMinecart cart) {
        if (cart != null && Math.abs(cart.getDeltaMovement().x) > 0) {
            return RailShape.EAST_WEST;
        }
        return RailShape.NORTH_SOUTH;
    }

    @Override
    public void onMinecartPass(BlockState state, Level level, BlockPos pos, AbstractMinecart cart) {
        if (level.isClientSide()) {
            return;
        }
        ServerLevel serverLevel = (ServerLevel) level;
        ServerPlayer player = getPlayerPassenger(cart);
        if (player == null) {
            cart.kill();
            return;
        }

        String destinationStation = RoutingService.INSTANCE.getUserDestinationStation(player.getUUID(), level);

        GraphPath<BlockPos, RouteRailsEdge> path = null;
        if (destinationStation != null) {
            path = RoutingService.INSTANCE.calculateRoute(pos, destinationStation, level);
            if (path == null) {
                player.sendSystemMessage(Component.literal("Failed to calculate path to station " + destinationStation));
                ejectPlayer(cart, pos);
                return;
            }

            List<RouteRailsEdge> edgeList = path.getEdgeList();
            if (edgeList.isEmpty()) {
                player.sendSystemMessage(Component.literal("You arrived to " + destinationStation));
                ejectPlayer(cart, pos);
                RoutingService.INSTANCE.setUserDestination(player.getUUID(), null, level);
                return;
            }
        }

        StringBuilder statusBuilder = new StringBuilder();
        String stationName = RoutingService.INSTANCE.getStationName(level, pos);
        if (stationName != null) {
            statusBuilder.append("This is ").append(stationName).append(". ");
        }

        Direction targetDirection;
        if (path == null) {
            Direction cartDirection = VectorUtils.toHorizontalDirection(cart.getDeltaMovement());
            if (cartDirection == null || level.getBlockState(pos.relative(cartDirection)).getTags().noneMatch(BlockTags.RAILS::equals)) {
                Set<RouteRailsEdge> edges = RoutingService.INSTANCE.getBlockOutgoingEdges(pos, level);
                RouteRailsEdge nextEdge = edges.stream()
                        .filter(it -> {
                            if (it.getDirection() == null) {
                                return false;
                            }
                            if (cartDirection != null) {
                                return it.getDirection() != cartDirection.getOpposite();
                            }
                            return true;
                        })
                        .max(Comparator.comparing(it -> it.getDistance() * -1))
                        .orElse(null);
                if (nextEdge == null) {
                    List<RouteRailsEdge> bridgeEdges = edges.stream().filter(it -> it.getDirection() == null).toList();
                    if (bridgeEdges.size() == 1) {
                        performBridgeTeleport(List.of(bridgeEdges.get(0)), serverLevel, cart, player);
                        return;
                    }
                    ejectPlayer(cart, pos);
                    player.sendSystemMessage(Component.literal("There's no route ahead"));
                    return;
                }
                targetDirection = nextEdge.getDirection();
            } else {
                targetDirection = cartDirection;
            }
        } else {
            List<RouteRailsEdge> edgeList = path.getEdgeList();
            // edgeList is checked for emptiness above, so firstEdge is never null
            RouteRailsEdge firstEdge = edgeList.get(0);
            if (firstEdge.getDirection() == null) {
                performBridgeTeleport(edgeList, serverLevel, cart, player);
                return;
            }

            int blocksRemaining = 0;
            for (RouteRailsEdge edge : edgeList) {
                if (edge.getDirection() == null) {
                    // Ignore bridge (teleport) edge weights
                    continue;
                }
                blocksRemaining += edge.getDistance();
            }
            statusBuilder.append("%s blocks remaining until destination (%s)".formatted(blocksRemaining, destinationStation));

            targetDirection = firstEdge.getDirection();
        }

        if (!statusBuilder.isEmpty()) {
            player.sendSystemMessage(Component.literal(statusBuilder.toString()));
        }

        Vec3 currentDeltaMovement = cart.getDeltaMovement();
        if (VectorUtils.getHorizontalDistance(currentDeltaMovement) < 0.01) {
            // Just spawned minecart / teleported. Set yaw.
            player.teleportTo(serverLevel, pos.getX(), pos.getY(), pos.getZ(), targetDirection.toYRot(), player.getXRot());
            cart = MinecartUtil.spawnAndRide(level, player, pos);
        }

        Vec3 travelVector = Vec3.atLowerCornerOf(targetDirection.getNormal());
        cart.move(MoverType.SELF, travelVector);
        cart.setDeltaMovement(travelVector);
    }

    private void performBridgeTeleport(List<RouteRailsEdge> edgeList, ServerLevel level, AbstractMinecart cart, ServerPlayer player) {
        RouteRailsEdge firstEdge = edgeList.get(0);
        BlockPos src = firstEdge.getSrc();
        String startStation = RoutingService.INSTANCE.getStationName(level, src);
        StringBuilder sb = new StringBuilder("Using bridge ").append(startStation);

        RouteRailsEdge lastBridgeEdge = null;
        for (RouteRailsEdge edge : edgeList) {
            if (edge.getDirection() != null) {
                break;
            }
            lastBridgeEdge = edge;

            String stationName = RoutingService.INSTANCE.getStationName(level, edge.getDst());
            sb.append(" -> ").append(stationName);
        }

        player.sendSystemMessage(Component.literal(sb.toString()));
        BlockPos dst = Objects.requireNonNull(lastBridgeEdge).getDst();

        // Recreate minecart at new destination to avoid glitches
        player.teleportTo(dst.getX() + 0.5, dst.getY(), dst.getZ() + 0.5);
        cart.kill();
        MinecartUtil.spawnAndRide(level, player, dst);
    }

    private void ejectPlayer(AbstractMinecart cart, BlockPos pos) {
        ServerPlayer player = getPlayerPassenger(cart);
        if (player != null) {
            player.teleportTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        }
        cart.kill();
    }

    private ServerPlayer getPlayerPassenger(AbstractMinecart cart) {
        for (Entity passenger : cart.getPassengers()) {
            if (passenger instanceof ServerPlayer player) {
                return player;
            }
        }
        return null;
    }

    @Override
    public boolean isFlexibleRail(BlockState state, BlockGetter world, BlockPos pos) {
        return false;
    }

    @Override
    public boolean canMakeSlopes(BlockState state, BlockGetter level, BlockPos pos) {
        return false;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new RoutingRailBlockEntity(pPos, pState);
    }

    @Override
    public void onPlace(BlockState pState, Level pLevel, BlockPos pPos, BlockState pOldState, boolean pIsMoving) {
        super.onPlace(pState, pLevel, pPos, pOldState, pIsMoving);
        RoutingService.INSTANCE.onRoutingRailPlaced(pPos, pLevel);
    }


    @Override
    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
        super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
        RoutingService.INSTANCE.onRoutingRailRemoved(pPos, pLevel);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide() || hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }

        if (player.isShiftKeyDown()) {
            String stationName = RoutingService.INSTANCE.getStationName(level, pos);
            if (stationName != null) {
                player.sendSystemMessage(Component.literal("Station: %s".formatted(stationName)));
            }
            long startTimestamp = System.currentTimeMillis();

            Direction direction = player.getDirection();
            RailTraverser.NextBlock neighbour = findNeighbourRoutingRail(pos, direction, level);
            String foundNeighbourDetails = null;
            Integer distance = null;
            if (neighbour != null) {
                foundNeighbourDetails = neighbour.pos.toString();
                distance = neighbour.traversedBlocksCount;
            }

            long duration = System.currentTimeMillis() - startTimestamp;

            player.sendSystemMessage(Component.literal("Direction %s. Found: %s. Distance: %s. Duration: %sms".formatted(direction, foundNeighbourDetails, distance, duration)));

            return InteractionResult.PASS;
        }

        String destinationForLaunchBlock = RoutingService.INSTANCE.getDestinationForLaunchBlock(pos, level);
        if (destinationForLaunchBlock != null) {
            boolean destinationSet = RoutingService.INSTANCE.setUserDestination(player.getUUID(), destinationForLaunchBlock, level);
            if (!destinationSet) {
                player.sendSystemMessage(Component.literal("Failed to set destination to %s by launch block".formatted(destinationForLaunchBlock)));
                return InteractionResult.PASS;
            }
            player.sendSystemMessage(Component.literal("Destination station set to %s by launch block".formatted(destinationForLaunchBlock)));
        }

        MinecartUtil.spawnAndRide(level, player, pos);

        return InteractionResult.PASS;
    }

    @Nullable
    public RailTraverser.NextBlock findNeighbourRoutingRail(BlockPos pPos, Direction direction, Level level) {
        RailTraverser railTraverser = new RailTraverser(pPos, direction, level);
        RailTraverser.NextBlock result = null;
        for (RailTraverser.NextBlock nextBlock : railTraverser) {
            if (nextBlock.block instanceof RoutingRailBlock) {
                result = nextBlock;
                break;
            }
        }
        return result;
    }
}
