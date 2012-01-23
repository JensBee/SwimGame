package swimgame;

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
    public static int randomInt(final int min, final int max) {
	return min + (int) (Math.random() * ((max - min) + 1));
    }

    /**
     * Get a zero based random int.
     * 
     * @param max
     *            Maximum int value
     * @return Random int from 0 to max
     */
    public static int randomInt(final int max) {
	return Util.randomInt(0, max);
    }
}
