package ru.wtrn.minecraft.mindpalace.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.jgrapht.GraphPath;
import ru.wtrn.minecraft.mindpalace.entity.RoutingRailBlockEntity;
import ru.wtrn.minecraft.mindpalace.routing.RouteRailsEdge;
import ru.wtrn.minecraft.mindpalace.routing.RoutingService;
import ru.wtrn.minecraft.mindpalace.util.MinecartUtil;
import ru.wtrn.minecraft.mindpalace.util.RailTraverser;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class RoutingRailBlock extends RailBlock implements EntityBlock {
    public RoutingRailBlock() {
        super(Properties.of().noCollission().strength(0.7F).sound(SoundType.METAL));
    }

    @Override
    public void onMinecartPass(BlockState state, Level level, BlockPos pos, AbstractMinecart cart) {
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
                RoutingService.INSTANCE.setUserDestination(player.getUUID(), null, level);
                ejectPlayer(cart, pos);
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
            Direction cartDirection = cart.getMotionDirection();
            BlockPos targetBlockPos = pos.relative(cartDirection);

            BlockState targetBlockState = level.getBlockState(targetBlockPos);
            if (targetBlockState.getTags().noneMatch(BlockTags.RAILS::equals)) {
                Set<RouteRailsEdge> edges = RoutingService.INSTANCE.getBlockOutgoingEdges(pos, level);
                RouteRailsEdge longestEdge = edges.stream()
                        .filter(it -> {
                            if (it.getDirection() == null) {
                                return false;
                            }
                            return it.getDirection() != cartDirection.getOpposite();
                        })
                        .max(Comparator.comparing(RouteRailsEdge::getDistance))
                        .orElse(null);
                if (longestEdge == null) {
                    ejectPlayer(cart, pos);
                    player.sendSystemMessage(Component.literal("There's no route ahead"));
                    return;
                }
                targetDirection = longestEdge.getDirection();
            } else {
                targetDirection = cartDirection;
            }
        } else {
            List<RouteRailsEdge> edgeList = path.getEdgeList();
            // edgeList is checked for emptiness above, so firstEdge is never null
            RouteRailsEdge firstEdge = edgeList.get(0);
            if (firstEdge.getDirection() == null) {
                performBridgeTeleport(edgeList, level, cart, player);
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

        Vec3 travelVector = Vec3.atLowerCornerOf(targetDirection.getNormal());
        cart.move(MoverType.SELF, travelVector);
        cart.setDeltaMovement(travelVector);
    }

    private void performBridgeTeleport(List<RouteRailsEdge> edgeList, Level level, AbstractMinecart cart, ServerPlayer player) {
        RouteRailsEdge firstEdge = edgeList.get(0);
        player.sendSystemMessage(Component.literal("Bridge teleports are not yet implemented"));
        ejectPlayer(cart, firstEdge.getSrc());
    }

    private void ejectPlayer(AbstractMinecart cart, BlockPos pos) {
        ServerPlayer player = getPlayerPassenger(cart);
        if (player != null) {
            player.teleportTo(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
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
