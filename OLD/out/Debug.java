package swimgame.out;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;

import cardGame.player.CardPlayer;

public class Debug {
    /** Debug level for a talkish level messages. */
    public static final int TALK = 3;
    /** Debug level for information level messages. */
    public static final int INFO = 2;
    /** Debug level for system level messages. */
    public static final int SYS = 1;

    /**
     * On release this should be set to
     * 
     * <pre>
     * static final boolean debug = false;
     * </pre>
     * 
     * so the compiler will remove all debugging parts.
     */
    // CHECKSTYLE:OFF
    public static boolean debug = false;
    public static int debugLevel = INFO;
    // CHECKSTYLE:ON
    /** Debug message prefix. */
    private static final String PREFIX = "::DBG: ";
    private static final boolean USE_FILE = true;
    private static final File LOGFILE = new File("debug.log");
    /** Output stream for debugging messages. */
    private static PrintStream out = null;// = System.out;

    /**
     * General String output function, without debug as prefix.
     * 
     * @param level
     *            The debugging level
     * @param message
     *            Message to print
     */
    public static void print(final int level, final String message) {
	if (Debug.debug && (level <= debugLevel)) {
	    if (out == null) {
		try {
		    if (USE_FILE) {
			out = new PrintStream(LOGFILE);
		    } else {
			out = System.out;
		    }
		} catch (FileNotFoundException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}
	    }
	    Debug.out.print(message);
	}
    }

    /**
     * Print a message with optional debug prefix.
     * 
     * @param level
     *            The debugging level
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

    /**
     * 
     * @param level
     *            The debugging level
     * @param obj
     *            Object as prefix
     * @param prefix
     *            Debugging prefix
     * @param message
     *            Message to print
     */
    public static void print(final int level, final Object obj,
	    final boolean prefix, final String message) {
	if (!debug || (level > debugLevel)) {
	    return;
	}
	if (obj.getClass().equals(CardPlayer.class)) {
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
	if (obj.getClass().equals(CardPlayer.class)) {
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

	if (out == null) {
	    try {
		if (USE_FILE) {
		    out = new PrintStream(LOGFILE);
		} else {
		    out = System.out;
		}
	    } catch (FileNotFoundException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	}

	if (prefix) {
	    out.printf(Debug.PREFIX + message, obj);
	} else {
	    out.printf(message, obj);
	}
    }
}
