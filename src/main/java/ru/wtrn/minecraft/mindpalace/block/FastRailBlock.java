package ru.wtrn.minecraft.mindpalace.block;

import net.minecraft.world.level.block.PoweredRailBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;

public class FastRailBlock extends PoweredRailBlock {
    public FastRailBlock() {
        super(BlockBehaviour.Properties.of(Material.DECORATION).noCollission().strength(0.7F).sound(SoundType.METAL), true);
    }
}
