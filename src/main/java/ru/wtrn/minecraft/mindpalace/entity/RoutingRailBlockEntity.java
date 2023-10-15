package ru.wtrn.minecraft.mindpalace.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import static ru.wtrn.minecraft.mindpalace.entity.ModEntities.ROUTING_RAIL_BLOCK_ENTITY;

public class RoutingRailBlockEntity extends BlockEntity {
    public RoutingRailBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ROUTING_RAIL_BLOCK_ENTITY.get(), pPos, pBlockState);
    }
}
