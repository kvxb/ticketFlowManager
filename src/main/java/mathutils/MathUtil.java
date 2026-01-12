package mathutils;

public class MathUtil {
    public static double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
    public static float round(float value) {
        return Math.round(value * 100.0f) / 100.0f;
    }
}
