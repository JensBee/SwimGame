package swimGame.out;

import java.io.PrintStream;

import swimGame.player.DefaultPlayer;
import swimGame.player.IPlayer;

public class Debug {
	public static boolean debug = false;
	private static final String PREFIX = "::DBG: ";
	private static PrintStream out = System.out;

	public static void print(final DefaultPlayer player, boolean prefix,
			final String message) {
		Debug.print(true, "<" + player + "> " + message);
	}

	public static void print(final DefaultPlayer player, final String message) {
		Debug.print(true, "<" + player + "> " + message);
	}

	public static void print(boolean prefix, final String message) {
		if (prefix) {
			Debug.print(Debug.PREFIX + message);
		}
	}

	public static void print(final String message) {
		if (Debug.debug) {
			Debug.out.print(message);
		}
	}

	public static void println(final String message) {
		Debug.print(message + "\n");
	}

	public static void println(final Class<?> callerClass, final String message) {
		Debug.println(Debug.PREFIX + "[" + callerClass + "] " + message);
	}

	public static void print(final IPlayer player, final String message) {
		Debug.print(Debug.PREFIX + "<" + player + "> " + message);
	}

	public static void println(final IPlayer player, final String message) {
		Debug.print(player, message + "\n");
	}

	public static void nl() {
		Debug.print("\n");
	}
}
