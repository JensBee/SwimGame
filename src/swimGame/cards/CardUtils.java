package swimGame.cards;

/**
 * Utility functions for card handling
 * 
 * @author Jens Bertram <code@jens-bertram.net>
 * 
 */
public class CardUtils {
	/**
	 * Card names for pretty printing
	 */
	private final String[][] cardStackStrings;

	/**
	 * Constructor
	 */
	public CardUtils() {
		this.cardStackStrings = new String[8][4];
		// build the cards string representation
		for (int card = 0; card < 8; card++) {
			for (int color = 0; color < 4; color++) {
				// build the stack string representation
				String cardString = "";

				// card color
				switch (color) {
				case 0:
					cardString += "♦";
					break;
				case 1:
					cardString += "♥";
					break;
				case 2:
					cardString += "♠";
					break;
				case 3:
					cardString += "♣";
					break;
				}
				// card name
				final int cardValue = card + 7;
				if (cardValue <= 10) {
					cardString += String.valueOf(cardValue);
				}
				switch (cardValue) {
				case 11:
					cardString += "B";
					break;
				case 12:
					cardString += "D";
					break;
				case 13:
					cardString += "K";
					break;
				case 14:
					cardString += "A";
					break;
				}

				this.cardStackStrings[card][color] = cardString;
			}
		}
	}

	/**
	 * Build a card stack
	 * 
	 * The card stack array will look like this:
	 * 
	 * <pre>
	 *   7 8 9 10 B D K A 
	 * ♦ . . . .  . . . .
	 * ♥ . . . .  . . . . 
	 * ♠ . . . .  . . . . 
	 * ♣ . . . .  . . . .
	 * </pre>
	 * 
	 * To win a game basically a right aligned horizontal line or a right
	 * aligned vertical line is what we want.
	 */
	private byte[][] initCardStack(final byte[][] cardStack) {
		// fill the card-stack
		for (int card = 0; card < 8; card++) {
			for (int color = 0; color < 4; color++) {
				// build the stack byte array
				cardStack[card][color] = 1;
			}
		}
		return cardStack;
	}

	/**
	 * Get a full card stack
	 * 
	 * @param full
	 * @return
	 */
	public byte[][] getStack(final boolean full) {
		byte[][] cardStack = new byte[8][4];
		if (full) {
			cardStack = this.initCardStack(cardStack);
		}
		return cardStack;
	}

	/**
	 * Get the string representation for a card
	 * 
	 * @param card
	 *            The card defined by it's array coordinates
	 * @return The string representation of the given card
	 * @see swimGame.cards.CardUtils#initCardStack
	 */
	public String cardToString(final int[] card) {
		return "[" + this.cardStackStrings[card[0]][card[1]] + "]";
	}

	/**
	 * Searches in an array of repeating patterns, if a value exists. For
	 * example the array (10,3,5,3,6,9) consists of two sequences (10,3,5) and
	 * (3,6,9). If you want to know if position two of a sequence is 6 then you
	 * must specify length as 3 (for the sequence-length) and pos as 2 (for the
	 * position in a sequence).
	 * 
	 * <b>The position parameter is one-based counted.</b>
	 * 
	 * @param array
	 *            The array to search in
	 * @param length
	 *            The length of a sequence in the given array
	 * @param pos
	 *            The position to look at in a sequence
	 * @param needle
	 *            An array of bytes to search for (in sequence)
	 * @return
	 */
	public static byte[] peekArray(byte[] array, int length, int pos,
			byte needle[]) {
		byte foundAt[] = new byte[array.length / length];
		byte foundItems = 0;
		for (int i = pos - 1; i < array.length;) {
			if (array[i] == needle[0]) {
				boolean matches = false;
				for (int k = 1; (k < needle.length) && (k < array.length); k++) {
					if (array[i + k] != needle[k]) {
						matches = false;
						break;
					}
					matches = true;
				}
			}
			i = i + length;
		}
		return foundAt;
	}
}
