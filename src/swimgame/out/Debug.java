package swimgame.out;

import java.io.PrintStream;

import swimgame.player.DefaultPlayer;
import swimgame.player.IPlayer;

public class Debug {
    public static boolean debug = false;
    private static final String PREFIX = "::DBG: ";
    private static PrintStream out = System.out;

    /**
     * General String output function, without debug as prefix.
     * 
     * @param message
     *            Message to print
     */
    public static void print(final String message) {
	if (Debug.debug) {
	    Debug.out.print(message);
	}
    }

    /**
     * Print a message with a player-name and debug as prefix.
     * 
     * @param player
     *            {@link IPlayer} whose name will be used as prefix
     * @param prefix
     *            If true the debug prefix will be printed
     * @param message
     *            Message to print
     */
    public static void print(final IPlayer player, final boolean prefix,
	    final String message) {
	Debug.print(true, "<" + player + "> " + message);
    }

    /**
     * Print a message with a debug and player-name as prefix.
     * 
     * @param player
     *            {@link IPlayer} whose name will be used as prefix
     * @param message
     *            Message to print
     */
    public static void print(final DefaultPlayer player, final String message) {
	Debug.print(true, "<" + player + "> " + message);
    }

    /**
     * Print a message with optional debug prefix.
     * 
     * @param prefix
     *            If true the debug prefix will be printed
     * @param message
     *            Message to print
     */
    public static void print(final boolean prefix, final String message) {
	if (prefix) {
	    Debug.print(Debug.PREFIX + message);
	} else {
	    Debug.print(message);
	}
    }

    /**
     * Print a message with linefeed and debug prefix.
     * 
     * @param message
     *            Message to print
     */
    public static void println(final String message) {
	Debug.print(message + "\n");
    }

    /**
     * Print a message with the classname and debug prefix and a linefeed.
     * 
     * @param callerClass
     *            Class to print as prefix
     * @param message
     *            Message to print
     */
    public static void println(final Class<?> callerClass, final String message) {
	Debug.println(Debug.PREFIX + "[" + callerClass + "] " + message);
    }

    /**
     * Print a message with the debug and player prefix..
     * 
     * @param player
     *            {@link IPlayer} whose name will be used as prefix
     * @param message
     *            Message to print
     */
    public static void print(final IPlayer player, final String message) {
	Debug.print(Debug.PREFIX + "<" + player + "> " + message);
    }

    /**
     * Print a message with the debug and player prefix and a linefeed.
     * 
     * @param player
     *            {@link IPlayer} whose name will be used as prefix
     * @param message
     *            Message to print
     */
    public static void println(final IPlayer player, final String message) {
	Debug.print(player, message + "\n");
    }

    /**
     * Print a single empty line
     */
    public static void nl() {
	Debug.print("\n");
    }

    /**
     * Print a formatted string with debug prefix.
     * 
     * @param message
     *            Message as format string
     * @param obj
     *            Replace objects
     */
    public static void printf(final String message, final Object... obj) {
	Debug.printf(true, message, obj);
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
    public static void printf(final Class<?> callerClass, final String message,
	    final Object... obj) {
	Debug.printf(true, "[" + callerClass + "] " + message, obj);
    }

    /**
     * Print a formatted string with debug and the player name name prefix .
     * 
     * @param player
     *            Class to print as prefix
     * @param message
     *            Message to print as format string
     * @param obj
     *            Replacement objects
     */
    public static void printf(final IPlayer player, final String message,
	    final Object... obj) {
	Debug.printf(true, "<" + player + "> " + message, obj);
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
    public static void printf(final boolean prefix, final String message,
	    final Object... obj) {
	if (!debug) {
	    return;
	}
	if (prefix) {
	    out.printf(Debug.PREFIX + message, obj);
	} else {
	    out.printf(message, obj);
	}
    }
}
