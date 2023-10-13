package ru.wtrn.minecraft.mindpalace.util;

import net.minecraft.core.BlockPos;

import java.util.Arrays;

public class BlockPosUtil {
    public static String blockPosToString(BlockPos pos) {
        return pos.getX() + "/" + pos.getY() + "/" + pos.getZ();
    }

    public static BlockPos blockPosFromString(String posStr) {
        int[] split = Arrays.stream(posStr.split("/")).mapToInt(Integer::parseInt).toArray();
        return new BlockPos(split[0], split[1], split[2]);
    }
}
