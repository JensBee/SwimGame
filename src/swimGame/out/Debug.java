package swimGame.out;

import java.io.PrintStream;

import swimGame.player.Player;

public class Debug {
	public static boolean debug = false;
	private static final String PREFIX = "DBG: ";
	private static PrintStream out = System.out;

	public static void print(final String message) {
		if (!Debug.debug) {
			return;
		}
		Debug.out.print(Debug.PREFIX + message);
	}

	public static void println(final String message) {
		if (!Debug.debug) {
			return;
		}
		Debug.out.println(Debug.PREFIX + message);
	}

	public static void println(final Class callerClass, final String message) {
		if (!Debug.debug) {
			return;
		}
		Debug.out.println(Debug.PREFIX + "[" + callerClass + "] " + message);
	}

	public static void println(final Player player, final String message) {
		if (!Debug.debug) {
			return;
		}
		Debug.out.println(Debug.PREFIX + "<" + player + "> " + message);
	}
}
