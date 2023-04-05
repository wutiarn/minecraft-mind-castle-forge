package ru.wtrn.minecraft.mindpalace.util.math;

public class NormalPlane {
    
    public final Vec3f normal;
    public final Vec3f origin;
    
    public NormalPlane(Vec3f origin, Vec3f normal) {
        this.origin = origin;
        this.normal = new Vec3f(normal);
        this.normal.normalize();
    }
    
    public NormalPlane(Facing facing) {
        this.origin = new Vec3f();
        this.normal = new Vec3f();
        normal.set(facing.axis, facing.offset());
    }
    
    public NormalPlane(Axis axis, float value, Facing facing) {
        this.origin = new Vec3f();
        origin.set(axis, value);
        this.normal = new Vec3f();
        normal.set(facing.axis, facing.offset());
    }
    
    public Boolean isInFront(Vec3f vec) {
        Vec3f temp = new Vec3f(vec);
        temp.sub(origin);
        float result = normal.dot(temp);
        if (result < 0 ? (result > -VectorFan.EPSILON) : (result < VectorFan.EPSILON))
            return null;
        return result > 0;
    }
    
    public boolean cuts(VectorFan strip) {
        boolean front = false;
        boolean back = false;
        for (int i = 0; i < strip.count(); i++) {
            Boolean result = isInFront(strip.get(i));
            
            if (result == null)
                return true;
            
            if (result)
                front = true;
            if (!result)
                back = true;
            
            if (front && back)
                return true;
        }
        return false;
    }
    
    public Vec3f intersect(Vec3f start, Vec3f end) {
        Vec3f lineOrigin = start;
        Vec3f lineDirection = new Vec3f(end);
        lineDirection.sub(lineOrigin);
        lineDirection.normalize();
        
        if (normal.dot(lineDirection) == 0)
            return null;
        
        float t = (normal.dot(origin) - normal.dot(lineOrigin)) / normal.dot(lineDirection);
        Vec3f point = new Vec3f(lineDirection);
        point.scale(t);
        point.add(lineOrigin);
        return point;
    }
    
    public Vec3f intersect(Ray3f ray) {
        if (normal.dot(ray.direction) == 0)
            return null;
        
        float t = (normal.dot(origin) - normal.dot(ray.origin)) / normal.dot(ray.direction);
        Vec3f point = new Vec3f(ray.direction);
        point.scale(t);
        point.add(ray.origin);
        return point;
    }
    
    public Float project(Axis one, Axis two, Axis axis, float valueOne, float valueTwo) {
        Vec3f lineOrigin = new Vec3f();
        lineOrigin.set(one, valueOne);
        lineOrigin.set(two, valueTwo);
        
        Vec3f lineDirection = new Vec3f();
        lineDirection.set(axis, 1);
        
        if (normal.dot(lineDirection) == 0)
            return null;
        
        float t = (normal.dot(origin) - normal.dot(lineOrigin)) / normal.dot(lineDirection);
        return lineOrigin.get(axis) + lineDirection.get(axis) * t;
    }
    
    @Override
    public String toString() {
        return "[o:" + origin + ",n:" + normal + "]";
    }
    
}
