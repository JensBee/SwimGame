package swimGame.out;

import swimGame.player.IPlayer;

public class Console {
	// interact with user?
	public static boolean ask = true;

	public static void print(final String message) {
		System.out.print(message);
	}

	public static void println(final String message) {
		Console.print(message + "\n");
	}

	public static void println(final String prefix, final String message) {
		Console.println("[" + prefix + "] " + message);
	}

	public static void println(final IPlayer player, final String message) {
		Console.println("<" + player.toString() + "> " + message);
	}

	public static Class<Console> nl() {
		Console.print("\n");
		return Console.class;
	}
}
