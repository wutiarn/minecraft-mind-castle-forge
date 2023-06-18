package ru.wtrn.minecraft.mindpalace.util.math.vec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import org.joml.Vector3f;
import ru.wtrn.minecraft.mindpalace.util.math.base.Axis;
import ru.wtrn.minecraft.mindpalace.util.math.geo.VectorFan;

public class VectorUtils {

    public static BlockPos toBlockPos(Vec3 vec3) {
        return new BlockPos(vec3.x, vec3.y, vec3.z);
    }

    public static Vec3 toVec3(BlockPos blockPos) {
        return new Vec3(blockPos.getX(), blockPos.getY(), blockPos.getZ());
    }

    public static double getLength(Vec3 vec3) {
        return Math.sqrt(vec3.x * vec3.x + vec3.y * vec3.y + vec3.z * vec3.z);
    }

    public static double getHorizontalLength(Vec3 vec3) {
        return Math.sqrt(vec3.x * vec3.x + vec3.z * vec3.z);
    }

    public static Vector3d set(Vector3d vec, double value, Axis axis) {
        switch (axis) {
            case X -> new Vector3d(value, vec.y, vec.z);
            case Y -> new Vector3d(vec.x, value, vec.z);
            case Z -> new Vector3d(vec.x, vec.y, value);
        }
        throw new IllegalArgumentException();
    }

    public static Vec3 set(Vec3 vec, double value, Axis axis) {
        switch (axis) {
            case X:
                return new Vec3(value, vec.y, vec.z);
            case Y:
                return new Vec3(vec.x, value, vec.z);
            case Z:
                return new Vec3(vec.x, vec.y, value);
        }
        throw new IllegalArgumentException();
    }

    public static void set(MutableBlockPos vec, int value, Axis axis) {
        switch (axis) {
            case X:
                vec.setX(value);
                break;
            case Y:
                vec.setY(value);
                break;
            case Z:
                vec.setZ(value);
                break;
        }
        throw new IllegalArgumentException();
    }

    public static BlockPos set(BlockPos vec, int value, Axis axis) {
        switch (axis) {
            case X:
                return new BlockPos(value, vec.getY(), vec.getZ());
            case Y:
                return new BlockPos(vec.getX(), value, vec.getZ());
            case Z:
                return new BlockPos(vec.getX(), vec.getY(), value);
        }
        return null;
    }

    public static double get(Axis axis, Vector3d vec) {
        return get(axis, vec.x, vec.y, vec.z);
    }

    public static double get(Axis axis, Vec3 vec) {
        return get(axis, vec.x, vec.y, vec.z);
    }

    public static double get(net.minecraft.core.Direction.Axis axis, Vec3 vec) {
        return get(axis, vec.x, vec.y, vec.z);
    }

    public static float get(Axis axis, Vector3f vec) {
        return get(axis, vec.x(), vec.y(), vec.z());
    }

    public static int get(Axis axis, Vec3i vec) {
        return get(axis, vec.getX(), vec.getY(), vec.getZ());
    }

    public static float get(Axis axis, float x, float y, float z) {
        switch (axis) {
            case X:
                return x;
            case Y:
                return y;
            case Z:
                return z;
        }
        return 0;
    }

    public static double get(Axis axis, double x, double y, double z) {
        switch (axis) {
            case X:
                return x;
            case Y:
                return y;
            case Z:
                return z;
        }
        return 0;
    }

    public static int get(Axis axis, int x, int y, int z) {
        switch (axis) {
            case X:
                return x;
            case Y:
                return y;
            case Z:
                return z;
        }
        return 0;
    }

    public static float get(net.minecraft.core.Direction.Axis axis, float x, float y, float z) {
        switch (axis) {
            case X:
                return x;
            case Y:
                return y;
            case Z:
                return z;
        }
        return 0;
    }

    public static double get(net.minecraft.core.Direction.Axis axis, double x, double y, double z) {
        switch (axis) {
            case X:
                return x;
            case Y:
                return y;
            case Z:
                return z;
        }
        return 0;
    }

    public static int get(net.minecraft.core.Direction.Axis axis, int x, int y, int z) {
        switch (axis) {
            case X:
                return x;
            case Y:
                return y;
            case Z:
                return z;
        }
        return 0;
    }

    public static boolean isZero(double number) {
        return number > -VectorFan.EPSILON && number < VectorFan.EPSILON;
    }

    public static boolean isZero(float number) {
        return number > -VectorFan.EPSILON && number < VectorFan.EPSILON;
    }

    public static boolean equals(double number, double number2) {
        return number - number2 > -VectorFan.EPSILON && number - number2 < VectorFan.EPSILON;
    }

    public static boolean equals(float number, float number2) {
        return number - number2 > -VectorFan.EPSILON && number - number2 < VectorFan.EPSILON;
    }

}
