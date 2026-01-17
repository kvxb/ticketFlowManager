package mathutils;

public class MathUtil {
    public static double round(final double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    public static float round(final float value) {
        return Math.round(value * 100.0f) / 100.0f;
    }

    public static double normalize(final double baseScore, final double maxValue) {
        return Math.min(100.0, (baseScore * 100.0) / maxValue);
    }

    public static double average(final double value, final int elements) {
        if (elements == 0)
            return 0;
        return value / elements;
    }
}
