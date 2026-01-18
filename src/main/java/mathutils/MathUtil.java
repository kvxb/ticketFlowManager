package mathutils;

/**
 * Utility class for mathematical operations.
 */
public final class MathUtil {

    private static final double DOUBLE_ROUNDING_FACTOR = 100.0;
    private static final float FLOAT_ROUNDING_FACTOR = 100.0f;
    private static final double MAX_NORMALIZED_SCORE = 100.0;

    /**
     * Private constructor to prevent instantiation of utility class.
     */
    private MathUtil() {
    }

    /**
     * Rounds a double value to two decimal places.
     *
     * @param value the value to round
     * @return the rounded value
     */
    public static double round(final double value) {
        return Math.round(value * DOUBLE_ROUNDING_FACTOR) / DOUBLE_ROUNDING_FACTOR;
    }

    /**
     * Rounds a float value to two decimal places.
     *
     * @param value the value to round
     * @return the rounded value
     */
    public static float round(final float value) {
        return Math.round(value * FLOAT_ROUNDING_FACTOR) / FLOAT_ROUNDING_FACTOR;
    }

    /**
     * Normalizes a score based on a maximum value to a scale of 0-100.
     *
     * @param baseScore the score to normalize
     * @param maxValue  the maximum possible value for the base score
     * @return the normalized score, capped at 100.0
     */
    public static double normalize(final double baseScore, final double maxValue) {
        return Math.min(MAX_NORMALIZED_SCORE,
                (baseScore * MAX_NORMALIZED_SCORE) / maxValue);
    }

    /**
     * Calculates the average value.
     *
     * @param value    the sum of values
     * @param elements the number of elements
     * @return the average, or 0 if elements is 0
     */
    public static double average(final double value, final int elements) {
        if (elements == 0) {
            return 0;
        }
        return value / elements;
    }
}
