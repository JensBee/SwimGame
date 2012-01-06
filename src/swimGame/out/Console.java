package swimGame.out;

public class Console {
	public static void print(final String message) {
		System.out.print(message);
	}

	public static void println(final String message) {
		System.out.println(message);
	}

	public static void println(final String prefix, final String message) {
		Console.println("[" + prefix + "] " + message);
	}

	public static void print(final String prefix, final String message) {
		Console.print("[" + prefix + "] " + message);
	}

	public static Class<Console> nl() {
		Console.print("\n");
		return Console.class;
	}
}
