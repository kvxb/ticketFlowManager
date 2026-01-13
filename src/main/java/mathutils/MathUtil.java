package mathutils;

public class MathUtil {
    public static double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    public static float round(float value) {
        return Math.round(value * 100.0f) / 100.0f;
    }

    public static double normalize(double baseScore, double maxValue) {
        return Math.min(100.0, (baseScore * 100.0) / maxValue);
    }

    public static double average(double value, int elements) {
        if (elements == 0)
            return -1.0;
        return value / elements;
    }
}
