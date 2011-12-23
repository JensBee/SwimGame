package swimGame.out;

public class Console {
	public static void print(final String message) {
		System.out.print(message);
	}

	public static void println(final String message) {
		System.out.println(message);
	}

	public static void println(final String prefix, final String message) {
		System.out.println("[" + prefix + "] " + message);
	}
}
