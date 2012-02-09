package cardGame.out;

/** Simple debugging console output handler. */
public class Debug {
    /** Toggles debugging. */
    private static final boolean DEBUG = true;
    /** Minimal debug level. All messages below this level will be omitted. */
    private static final Level LEVEL = Level.TALK;
    /** Output prefix. %s will be replaced with the level. */
    private static final String PREFIX = "[DBG:%s] ";

    /** Debugging levels. */
    public enum Level {
	/** All kinds of messages. */
	TALK,
	/** Modest level, informational messages. */
	INFO,
	/** Only facts to trace what's going on. */
	SYS,
	/** Just small status. */
	CORE;
    }

    /**
     * Generic debug output function.
     * 
     * @param string
     *            Message to print
     */
    private static void write(final String string) {
	Console.INSTANCE.print(string);
    }

    /**
     * Generic print function.
     * 
     * @param level
     *            The debug message level
     * @param string
     *            Message to print
     */
    private static void write(final Level level, final String string) {
	if (!DEBUG) {
	    return;
	}
	if (level.ordinal() >= LEVEL.ordinal()) {
	    Debug.write(String.format(PREFIX, level) + string);
	}
    }

    /**
     * Print a debug message.
     * 
     * @param level
     *            Message level
     * @param string
     *            Message to print
     */
    public static final void print(final Level level, final String string) {
	Debug.write(level, string);
    }

    /**
     * Print a debug message with line feed.
     * 
     * @param level
     *            Message level
     * @param string
     *            Message to print
     */
    public static final void println(final Level level, final String string) {
	Debug.write(level, string + "\n");
    }

    /**
     * Print a format string debug message.
     * 
     * @param level
     *            Message level
     * @param arguments
     *            Format string arguments
     * @param string
     *            Message to print
     */
    public static final void printf(final Level level, final String string,
	    final Object... arguments) {
	Debug.write(level, String.format(string, arguments));
    }

    /**
     * Print a format string debug message with line feed.
     * 
     * @param level
     *            Message level
     * @param arguments
     *            Format string arguments
     * @param string
     *            Message to print
     */
    public static final void printfn(final Level level, final String string,
	    final Object... arguments) {
	Debug.write(level, String.format(string + "\n", arguments));
    }
}
