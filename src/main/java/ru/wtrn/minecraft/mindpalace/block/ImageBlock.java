package ru.wtrn.minecraft.mindpalace.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import ru.wtrn.minecraft.mindpalace.block.entity.ImageBlockEntity;

public class ImageBlock extends BaseEntityBlock {
    protected ImageBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new ImageBlockEntity(blockPos, blockState);
    }
}
