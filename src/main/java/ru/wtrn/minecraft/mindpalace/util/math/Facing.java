package ru.wtrn.minecraft.mindpalace.util.math;

import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.AABB;


public enum Facing {

    DOWN(Axis.Y, false, new Vec3i(0, -1, 0), -1) {

        @Override
        public Facing opposite() {
            return Facing.UP;
        }

        @Override
        public Direction toVanilla() {
            return Direction.DOWN;
        }

        @Override
        public double get(AABB bb) {
            return bb.minY;
        }

        @Override
        public com.mojang.math.Axis rotation() {
            return com.mojang.math.Axis.YN;
        }

        @Override
        public AABB set(AABB bb, double value) {
            return bb.setMinY(value);
        }

    },
    UP(Axis.Y, true, new Vec3i(0, 1, 0), -1) {

        @Override
        public Facing opposite() {
            return Facing.DOWN;
        }

        @Override
        public Direction toVanilla() {
            return Direction.UP;
        }

        @Override
        public double get(AABB bb) {
            return bb.maxY;
        }

        @Override
        public com.mojang.math.Axis rotation() {
            return com.mojang.math.Axis.YP;
        }

        @Override
        public AABB set(AABB bb, double value) {
            return bb.setMaxY(value);
        }

    },
    NORTH(Axis.Z, false, new Vec3i(0, 0, -1), 2) {

        @Override
        public Facing opposite() {
            return SOUTH;
        }

        @Override
        public Direction toVanilla() {
            return Direction.NORTH;
        }

        @Override
        public double get(AABB bb) {
            return bb.minZ;
        }

        @Override
        public com.mojang.math.Axis rotation() {
            return com.mojang.math.Axis.ZN;
        }

        @Override
        public AABB set(AABB bb, double value) {
            return bb.setMinZ(value);
        }

    },
    SOUTH(Axis.Z, true, new Vec3i(0, 0, 1), 0) {

        @Override
        public Facing opposite() {
            return Facing.NORTH;
        }

        @Override
        public Direction toVanilla() {
            return Direction.SOUTH;
        }

        @Override
        public double get(AABB bb) {
            return bb.maxZ;
        }

        @Override
        public com.mojang.math.Axis rotation() {
            return com.mojang.math.Axis.ZP;
        }

        @Override
        public AABB set(AABB bb, double value) {
            return bb.setMaxZ(value);
        }

    },
    WEST(Axis.X, false, new Vec3i(-1, 0, 0), 1) {

        @Override
        public Facing opposite() {
            return Facing.EAST;
        }

        @Override
        public Direction toVanilla() {
            return Direction.WEST;
        }

        @Override
        public double get(AABB bb) {
            return bb.minX;
        }

        @Override
        public com.mojang.math.Axis rotation() {
            return com.mojang.math.Axis.XN;
        }

        @Override
        public AABB set(AABB bb, double value) {
            return bb.setMinX(value);
        }

    },
    EAST(Axis.X, true, new Vec3i(1, 0, 0), 3) {

        @Override
        public Facing opposite() {
            return Facing.WEST;
        }

        @Override
        public Direction toVanilla() {
            return Direction.EAST;
        }

        @Override
        public double get(AABB bb) {
            return bb.maxX;
        }

        @Override
        public com.mojang.math.Axis rotation() {
            return com.mojang.math.Axis.XP;
        }

        @Override
        public AABB set(AABB bb, double value) {
            return bb.setMaxX(value);
        }
    };

    public static final Facing[] VALUES = new Facing[] { DOWN, UP, NORTH, SOUTH, WEST, EAST };
    public static final Facing[] HORIZONTA_VALUES = new Facing[] { SOUTH, WEST, NORTH, EAST };

    public static final String[] FACING_NAMES = new String[] { "down", "up", "north", "south", "west", "east" };
    public static final String[] HORIZONTAL_FACING_NAMES = new String[] { "north", "south", "west", "east" };

    public static Facing get(int index) {
        return VALUES[index];
    }

    public static Facing get(Direction direction) {
        if (direction == null)
            return null;
        return switch (direction) {
            case DOWN -> Facing.DOWN;
            case UP -> Facing.UP;
            case NORTH -> Facing.NORTH;
            case SOUTH -> Facing.SOUTH;
            case WEST -> Facing.WEST;
            case EAST -> Facing.EAST;
        };
    }

    public static Facing get(Axis axis, boolean positive) {
        return switch (axis) {
            case X -> positive ? Facing.EAST : Facing.WEST;
            case Y -> positive ? Facing.UP : Facing.DOWN;
            case Z -> positive ? Facing.SOUTH : Facing.NORTH;
            default -> throw new IllegalArgumentException();
        };
    }

    public static Facing getHorizontal(int index) {
        return HORIZONTA_VALUES[index];
    }

    /** gets the direction from the first position to the second. It assumes the positions are next to each other.
     *
     * @param pos
     * @param second
     * @return */
    public static Facing direction(Vec3i pos, Vec3i second) {
        if (pos.getX() == second.getX())
            if (pos.getY() == second.getY())
                if (pos.getZ() == second.getZ() + 1)
                    return Facing.SOUTH;
                else
                    return Facing.NORTH;
            else if (pos.getY() == second.getY() + 1)
                return Facing.UP;
            else
                return Facing.DOWN;
        else if (pos.getX() == second.getX() + 1)
            return Facing.EAST;
        return Facing.WEST;
    }

    public final String name;
    public final Axis axis;
    public final boolean positive;
    public final Vec3i normal;
    public final NormalPlane plane;
    public final int horizontalIndex;

    private Facing(Axis axis, boolean positive, Vec3i normal, int horizontalIndex) {
        this.name = name().toLowerCase();
        this.axis = axis;
        this.positive = positive;
        this.normal = normal;
        this.plane = new NormalPlane(this);
        this.horizontalIndex = horizontalIndex;
    }

    public int offset() {
        return positive ? 1 : -1;
    }

    public int offset(Axis axis) {
        if (this.axis == axis)
            return offset();
        return 0;
    }

    public Component translate() {
        return Component.translatable("facing." + name);
    }

    public abstract Facing opposite();

    public abstract Direction toVanilla();

    public Axis one() {
        return axis.one();
    }

    public Axis two() {
        return axis.two();
    }

    public Axis getUAxis() {
        return switch (axis) {
            case X -> Axis.Z;
            case Y -> Axis.X;
            case Z -> Axis.X;
            default -> null;
        };
    }

    public Axis getVAxis() {
        return switch (axis) {
            case X -> Axis.Y;
            case Y -> Axis.Z;
            case Z -> Axis.Y;
            default -> null;
        };
    }

    public float getU(float x, float y, float z) {
        return switch (axis) {
            case X -> z;
            case Y -> x;
            case Z -> x;
            default -> 0;
        };
    }

    public float getV(float x, float y, float z) {
        return switch (axis) {
            case X -> y;
            case Y -> z;
            case Z -> y;
            default -> 0;
        };
    }

    public abstract double get(AABB bb);

    public abstract AABB set(AABB bb, double value);

    public abstract com.mojang.math.Axis rotation();

}
