package ru.wtrn.minecraft.mindpalace.util.math.interpolation;

import ru.wtrn.minecraft.mindpalace.util.math.vec.VecNd;

import java.util.List;

public class LinearInterpolation<T extends VecNd> extends Interpolation<T> {
    
    public LinearInterpolation(double[] times, T[] points) {
        super(times, points);
    }
    
    public LinearInterpolation(T... points) {
        super(points);
    }
    
    public LinearInterpolation(double[] times, List<T> points) {
        super(times, points);
    }
    
    public LinearInterpolation(List<T> points) {
        super(points);
    }
    
    @Override
    public double[] estimateDistance() {
        double[] data = new double[points.size()];
        for (int i = 1; i < points.size(); i++) {
            double distance = points.get(i).value.distance(points.get(i - 1).value);
            data[0] += distance;
            data[i] = distance;
        }
        return data;
    }
    
    @Override
    public double valueAt(double mu, int pointIndex, int pointIndexNext, int dim) {
        return (getValue(pointIndexNext, dim) - getValue(pointIndex, dim)) * mu + getValue(pointIndex, dim);
    }
    
}
