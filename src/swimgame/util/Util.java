package swimgame.util;

public class Util {
    /**
     * Get a random int in a given range.
     * 
     * @param min
     *            Minimum int value
     * @param max
     *            Maximum int value
     * @return Random int in the given range
     */
    public static int getRandomInt(final int min, final int max) {
	return min + (int) (Math.random() * ((max - min) + 1));
    }

    /**
     * Get a zero based random int.
     * 
     * @param max
     *            Maximum int value
     * @return Random int from 0 to max
     */
    public static int getRandomInt(final int max) {
	return Util.getRandomInt(0, max);
    }

    /**
     * Calculate with default minimum fixed to zero.
     * 
     * @param max
     *            Maximum value
     * @param value
     *            Current value
     * @return Normalized value
     */
    public static double normalize(final double max, final double value) {
	return Util.normalize(0, max, value);
    }

    /**
     * Calculate a normalized value.
     * 
     * @param min
     *            Minimum value
     * @param max
     *            Maximum value
     * @param value
     *            Current value
     * @return Normalized value
     */
    public static double normalize(final double min, final double max,
	    final double value) {
	return new Double(((value - min) / (max - min)) * 10);
    }
}
