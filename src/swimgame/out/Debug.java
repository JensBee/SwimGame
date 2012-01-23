package swimgame.out;

import java.io.PrintStream;

import swimgame.player.IPlayer;

public class Debug {
    public static final int TALK = 3;
    public static final int INFO = 2;
    public static final int SYS = 1;

    public static boolean debug = false;
    public static int debugLevel = INFO;
    private static final String PREFIX = "::DBG: ";
    private static PrintStream out = System.out;

    /**
     * General String output function, without debug as prefix.
     * 
     * @param message
     *            Message to print
     */
    public static void print(final int level, final String message) {
	if (Debug.debug && (level <= debugLevel)) {
	    Debug.out.print(message);
	}
    }

    /**
     * Print a message with optional debug prefix.
     * 
     * @param prefix
     *            If true the debug prefix will be printed
     * @param message
     *            Message to print
     */
    public static void print(final int level, final boolean prefix,
	    final String message) {
	if (!debug || (level > debugLevel)) {
	    return;
	}
	if (prefix) {
	    Debug.print(level, Debug.PREFIX + message);
	} else {
	    Debug.print(level, message);
	}
    }

    public static void print(final int level, final Object obj,
	    final boolean prefix, final String message) {
	if (!debug || (level > debugLevel)) {
	    return;
	}
	if (obj.getClass().equals(IPlayer.class)) {
	    Debug.print(level, prefix, "<" + obj.toString() + "> " + message);
	} else {
	    Debug.print(level, Debug.PREFIX + "[" + obj + "] " + message);
	}
    }

    public static void print(final int level, final Object obj,
	    final String message) {
	Debug.print(level, obj, true, message);
    }

    /**
     * Print a message with linefeed and debug prefix.
     * 
     * @param message
     *            Message to print
     */
    public static void println(final int level, final String message) {
	Debug.print(level, message + "\n");
    }

    public static void println(final int level, final Object obj,
	    final String message) {
	Debug.print(level, obj, message + "\n");
    }

    /**
     * Print a single empty line
     */
    public static void nl(final int level) {
	Debug.print(level, "\n");
    }

    /**
     * Print a formatted string with debug prefix.
     * 
     * @param message
     *            Message as format string
     * @param obj
     *            Replace objects
     */
    public static void printf(final int level, final String message,
	    final Object... obj) {
	Debug.printf(level, true, message, obj);
    }

    /**
     * Print a formatted string with debug and class name prefix .
     * 
     * @param callerClass
     *            Class to print as prefix
     * @param message
     *            Message to print as format string
     * @param obj
     *            Replacement objects
     */
    public static void printf(final int level, final Object obj,
	    final String message, final Object... objects) {
	if (!debug || (level > debugLevel)) {
	    return;
	}
	if (obj.getClass().equals(IPlayer.class)) {
	    Debug.printf(level, true, "<" + obj + "> " + message, objects);
	} else {
	    Debug.printf(level, true, "[" + obj + "] " + message, objects);
	}
    }

    /**
     * Print a formatted string with debug prefix.
     * 
     * @param prefix
     *            If true the debug prefix will be printed
     * @param message
     *            Message as format string
     * @param obj
     *            Replace objects
     */
    public static void printf(final int level, final boolean prefix,
	    final String message, final Object... obj) {
	if (!debug || (level > debugLevel)) {
	    return;
	}
	if (prefix) {
	    out.printf(Debug.PREFIX + message, obj);
	} else {
	    out.printf(message, obj);
	}
    }
}
