package ru.wtrn.minecraft.mindpalace.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class ImageBlockEntity extends BlockEntity {
    public ImageBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(ModBlockEntities.IMAGE_BLOCK.get(), blockPos, blockState);
    }
}
