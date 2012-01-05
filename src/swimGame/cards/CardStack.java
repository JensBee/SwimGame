package swimGame.cards;

import java.util.Iterator;
import java.util.Random;

/**
 * A card stack as part of the game. This is initially the full set of cards
 * available in the game.
 * 
 * @author Jens Bertram <code@jens-bertram.net>
 * 
 */
public class CardStack {
	// one random number generator for all stacks should be enough
	private static Random random = null;
	// is this an empty stack?8
	private boolean empty = true;

	// one-based card array bounds, just for ease of use / readability
	public static final int CARDS_MAX_COLOR = 4;
	public static final int CARDS_MAX_CARD = 8;
	public static final int CARDS_MAX = 32;
	// the minimum of the reachable points
	public static final int STACKVALUE_MIN = 24; // 7 + 8 + 9
	// the maximum of the reachable points
	public static final int STACKVALUE_MAX = 31; // A + B + D
	// flags for the card-stack array
	public static final byte FLAG_HAS_CARD = 1;
	public static final byte FLAG_NO_CARD = 0;
	public static final byte FLAG_UNINITIALIZED = -1;

	// Card names for pretty printing
	private static final char SYM_DIAMOND = '♦';
	private static final char SYM_HEART = '♥';
	private static final char SYM_SPADE = '♠';
	private static final char SYM_CLUB = '♣';
	private static final String NAME_JACK = "J";
	private static final String NAME_QUEEN = "Q";
	private static final String NAME_KING = "K";
	private static final String NAME_ACE = "A";
	private static final String[][] cardStackStr = { //
			{ CardStack.SYM_DIAMOND + "7", CardStack.SYM_HEART + "7",
					CardStack.SYM_SPADE + "7", CardStack.SYM_CLUB + "7" },
			{ CardStack.SYM_DIAMOND + "8", CardStack.SYM_HEART + "8",
					CardStack.SYM_SPADE + "8", CardStack.SYM_CLUB + "8" },
			{ CardStack.SYM_DIAMOND + "9", CardStack.SYM_HEART + "9",
					CardStack.SYM_SPADE + "9", CardStack.SYM_CLUB + "9" },
			{ CardStack.SYM_DIAMOND + "10", CardStack.SYM_HEART + "10",
					CardStack.SYM_SPADE + "10", CardStack.SYM_CLUB + "10" },
			{ CardStack.SYM_DIAMOND + CardStack.NAME_JACK,
					CardStack.SYM_HEART + CardStack.NAME_JACK,
					CardStack.SYM_SPADE + CardStack.NAME_JACK,
					CardStack.SYM_CLUB + CardStack.NAME_JACK },
			{ CardStack.SYM_DIAMOND + CardStack.NAME_QUEEN,
					CardStack.SYM_HEART + CardStack.NAME_QUEEN,
					CardStack.SYM_SPADE + CardStack.NAME_QUEEN,
					CardStack.SYM_CLUB + CardStack.NAME_QUEEN },
			{ CardStack.SYM_DIAMOND + CardStack.NAME_KING,
					CardStack.SYM_HEART + CardStack.NAME_KING,
					CardStack.SYM_SPADE + CardStack.NAME_KING,
					CardStack.SYM_CLUB + CardStack.NAME_KING },
			{ CardStack.SYM_DIAMOND + CardStack.NAME_ACE,
					CardStack.SYM_HEART + CardStack.NAME_ACE,
					CardStack.SYM_SPADE + CardStack.NAME_ACE,
					CardStack.SYM_CLUB + CardStack.NAME_ACE }, };

	/** Card stack of this table */
	private byte[][] cardStack = new byte[CardStack.CARDS_MAX_CARD][CardStack.CARDS_MAX_COLOR];

	/**
	 * Empty constructor
	 */
	public CardStack() {
	}

	/**
	 * Constructor
	 * 
	 * @param filled
	 *            If true, the card stack will be initially full (i.e. all cards
	 *            are on the stack)
	 */
	public CardStack(final boolean filled) {
		if (filled == true) {
			// The card stack will be initially full
			this.cardStack = CardStack.getNewStack(true);
			this.empty = false;
		}
	}

	/**
	 * Constructor
	 * 
	 * @param initialCards
	 *            An initial set of three cards to initialize this set with
	 * @throws Exception
	 *             Thrown if you specify less or more than three cards
	 */
	public CardStack(final int[] initialCards) throws Exception {
		if (initialCards.length < 5) {
			throw new Exception(
					"You must give three cards to initialize a CardStack!");
		}
		for (int i = 0; i <= 5;) {
			this.addCard(new int[] { initialCards[i], initialCards[i + 1] });
			i = i + 2;
		}
		this.empty = false;
	}

	/**
	 * Get the string representation for a card
	 * 
	 * @param card
	 *            The card defined by it's array coordinates [card, color]
	 * @return The string representation of the given card
	 * @see swimGame.cards.CardUtils#initCardStack
	 */
	public static String cardToString(final int[] card) {
		return "[" + CardStack.cardStackStr[card[0]][card[1]] + "]";
	}

	/**
	 * Get a random card out of the stack
	 * 
	 * @return Byte array representing the card
	 * @see swimGame.cards.CardUtils#initCardStack
	 */
	public int[] getRandomCard() throws Exception {
		if (this.empty) {
			throw new Exception("Unable to get a card. Stack is empty!");
		}

		if (CardStack.random == null) {
			CardStack.random = new Random();
		}
		// final boolean gotCard = false;
		final int[] card = new int[2];

		// try to find a random card that's still on the stack
		// TODO: make this aware of available cards to be more intelligent
		while (true) {
			// the card
			card[0] = CardStack.random.nextInt(CardStack.CARDS_MAX_CARD);
			// the color
			card[1] = CardStack.random.nextInt(CardStack.CARDS_MAX_COLOR);

			if (this.cardStack[card[0]][card[1]] == CardStack.FLAG_HAS_CARD) {
				// card is there .. take it
				this.cardStack[card[0]][card[1]] = CardStack.FLAG_NO_CARD;
				return card;
			}
		}
	}

	/**
	 * Add a card to this stack
	 * 
	 * @param card
	 *            The card to add
	 */
	public void addCard(final int[] card) {
		this.cardStack[card[0]][card[1]] = CardStack.FLAG_HAS_CARD;
		this.empty = false;
	}

	/**
	 * Returns a string representation of the cards available in this stack
	 */
	@Override
	public String toString() {
		if (this.empty) {
			return "";
		}
		String cards = "";
		for (int card = 0; card < CardStack.CARDS_MAX_CARD; card++) {
			for (int color = 0; color < CardStack.CARDS_MAX_COLOR; color++) {
				// check if we own this card
				if (this.cardStack[card][color] == CardStack.FLAG_HAS_CARD) {
					cards += CardStack.cardToString(new int[] { card, color });
				}
			}
		}
		return cards;
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
	private static byte[][] initCardStack(final byte[][] cardStack) {
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
	public static byte[][] getNewStack(final boolean full) {
		byte[][] cardStack = new byte[8][4];
		if (full) {
			cardStack = CardStack.initCardStack(cardStack);
		}
		return cardStack;
	}

	public static String getAsString(int[] cards) {
		// if ((cards.length % 2) != 0) {
		// throw new IllegalArgumentException(
		// "The array you specified is not a sequence of touples.");
		// }
		// for (int i = 0; i < cards.length; i++) {
		// this.addCard(new int[] { cards[i], cards[i + 1] });
		// i = i + 1;
		// }
		return null;
	}

	/**
	 * Clear out the current card-stack
	 */
	public void clear() {
		this.cardStack = new byte[CardStack.CARDS_MAX_CARD][CardStack.CARDS_MAX_COLOR];
		this.empty = true;
	}

	/**
	 * Get the byte array for this card-stack
	 * 
	 * @return The byte array used by this card-stack
	 */
	public byte[][] getArray() {
		return this.cardStack.clone();
	}

	/**
	 * Remove a card from this stack
	 * 
	 * @param card
	 *            The card to remove
	 */
	public void removeCard(final int[] card) {
		this.cardStack[card[0]][card[1]] = CardStack.FLAG_NO_CARD;
	}

	/**
	 * Get all cards belonging to the given color
	 * 
	 * @param color
	 * @return
	 * @throws IllegalArgumentException
	 */
	public byte[] getCardsByColor(byte color) throws IllegalArgumentException {
		if ((color < 0) || (color > CardStack.CARDS_MAX_COLOR)) {
			throw new IllegalArgumentException(String.format(
					"Card color %d out of bounds (%d-%d)", color, 0,
					CardStack.CARDS_MAX_COLOR));
		}

		// we have three cards max..
		byte[] cards = new byte[] { CardStack.FLAG_UNINITIALIZED,
				CardStack.FLAG_UNINITIALIZED, CardStack.FLAG_UNINITIALIZED };
		// ..track them
		int cardCount = 0;

		// go through cards..
		for (byte card = 0; card < CardStack.CARDS_MAX_CARD; card++) {
			// ..to check if we own it..
			if (this.cardStack[card][color] == CardStack.FLAG_NO_CARD) {
				// ..no we don't - check next
				continue;
			}
			cards[cardCount++] = card;

			if (cardCount > 3) {
				break;
			}
		}
		return cards;
	}

	/**
	 * Get all cards belonging to the given type
	 * 
	 * @param cardType
	 * @return
	 * @throws IllegalArgumentException
	 */
	public byte[] getCardsByType(byte cardType) throws IllegalArgumentException {
		if ((cardType < 0) || (cardType > CardStack.CARDS_MAX_CARD)) {
			throw new IllegalArgumentException(String.format(
					"Card type %d out of bounds (%d-%d)", cardType, 0,
					CardStack.CARDS_MAX_CARD));
		}

		// we have three cards max..
		byte[] cards = new byte[] { CardStack.FLAG_UNINITIALIZED,
				CardStack.FLAG_UNINITIALIZED, CardStack.FLAG_UNINITIALIZED };
		// ..track them
		int cardCount = 0;

		// go through cards..
		for (byte color = 0; color < CardStack.CARDS_MAX_COLOR; color++) {
			// ..to check if we own it..
			if (this.cardStack[cardType][color] == CardStack.FLAG_NO_CARD) {
				// ..no we don't - check next
				continue;
			}
			cards[cardCount++] = cardType;

			if (cardCount > 3) {
				break;
			}
		}
		return cards;
	}

	/**
	 * Get a column (colors for a type) from the card-stack
	 * 
	 * @param cardType
	 * @return
	 */
	public byte[] getColumn(int cardType) {
		if ((cardType < 0) || (cardType > CardStack.CARDS_MAX_CARD)) {
			throw new IllegalArgumentException(String.format(
					"Card type %d out of bounds (%d-%d)", cardType, 0,
					CardStack.CARDS_MAX_CARD));
		}
		byte[] col = new byte[CardStack.CARDS_MAX_COLOR];
		for (int i = 0; i < CardStack.CARDS_MAX_COLOR; i++) {
			col[i] = this.cardStack[cardType][i];
		}
		return col;
	}

	/**
	 * Get a row (types by color) from the card-stack
	 * 
	 * @param color
	 * @return
	 */
	public byte[] getRow(int color) {
		if ((color < 0) || (color > CardStack.CARDS_MAX_COLOR)) {
			throw new IllegalArgumentException(String.format(
					"Card color %d out of bounds (%d-%d)", color, 0,
					CardStack.CARDS_MAX_COLOR));
		}
		byte[] row = new byte[CardStack.CARDS_MAX_CARD];
		for (int i = 0; i < CardStack.CARDS_MAX_CARD; i++) {
			row[i] = this.cardStack[i][color];
		}
		return row;
	}

	/**
	 * Iterator that steps from top left to bottom right through the stack-array
	 * 
	 * @author Jens Bertram <code@jens-bertram.net>
	 * 
	 */
	public class StackIterator implements Iterator<Integer> {
		int row;
		int col;

		public StackIterator() {
			this.row = 0;
			this.col = -1;
		}

		@Override
		public boolean hasNext() {
			// last row
			if (this.row == (CardStack.CARDS_MAX_COLOR - 1)) {
				// before last col?
				if (this.col < (CardStack.CARDS_MAX_CARD - 1)) {
					// there are cols left
					return true;
				}
				return false;
			}
			return true;
		}

		public boolean hasNextRow() {
			int oldCol = this.col;
			// move col pointer temporary to the end
			this.col = CardStack.CARDS_MAX_CARD - 1;
			// check if there's something next
			boolean hasNext = this.hasNext();
			// undo changes
			this.col = oldCol;

			return hasNext;
		}

		@Override
		public Integer next() throws IllegalStateException {
			if (this.hasNext()) {
				if (this.col < (CardStack.CARDS_MAX_CARD - 1)) {
					this.col++;
				} else {
					this.col = 0;
					this.row++;
				}

				return Integer
						.valueOf(CardStack.this.cardStack[this.col][this.row]);
			}
			throw new IllegalStateException("You tried to step out of bounds.");
		}

		/**
		 * Get the first element of the next row
		 * 
		 * @return
		 * @throws IllegalStateException
		 */
		public Integer nextRow() throws IllegalStateException {
			this.col = CardStack.CARDS_MAX_CARD - 1;
			return this.next();
		}

		@Override
		public void remove() throws IllegalStateException {
			throw new IllegalStateException("Operation not supported.");
		}

		/**
		 * Get the current card type
		 * 
		 * @return
		 */
		public int getCard() {
			return this.col;
		}

		/**
		 * Get the color of the current card
		 * 
		 * @return
		 */
		public int getColor() {
			return this.row;
		}
	}

	/**
	 * Add cards from an integer sequence [(card,color),..]
	 * 
	 * @param data
	 */
	public void addCards(int[] data) throws IllegalArgumentException {
		if ((data.length % 2) != 0) {
			throw new IllegalArgumentException(
					"The array you specified is not a sequence of touples.");
		}
		for (int i = 0; i < data.length; i++) {
			this.addCard(new int[] { data[i], data[i + 1] });
			i = i + 1;
		}
	}

	/**
	 * Sets a custom value instead of the predefined flags for a card
	 */
	public void setCardValue(int[] aCard, byte value) {
		this.cardStack[aCard[0]][aCard[1]] = value;
	}

	public StringBuffer dumpStack() {
		StringBuffer dumpStr = new StringBuffer();

		String separator = "\n-+----+----+----+----+----+----+----+----+";
		String content = "\n%s|%+4d|%+4d|%+4d|%+4d|%+4d|%+4d|%+4d|%+4d|";

		dumpStr.append(String.format(
				" |   7|   8|   9|  10|   %s|   %s|   %s|   %s|",
				CardStack.NAME_JACK, CardStack.NAME_QUEEN, CardStack.NAME_KING,
				CardStack.NAME_ACE));
		dumpStr.append(separator);
		dumpStr.append(String.format(content, CardStack.SYM_DIAMOND,
				this.cardStack[1][0], this.cardStack[1][0],
				this.cardStack[2][0], this.cardStack[3][0],
				this.cardStack[4][0], this.cardStack[5][0],
				this.cardStack[6][0], this.cardStack[7][0]));
		dumpStr.append(String.format(content, CardStack.SYM_HEART,
				this.cardStack[1][1], this.cardStack[1][1],
				this.cardStack[2][1], this.cardStack[3][1],
				this.cardStack[4][1], this.cardStack[5][1],
				this.cardStack[6][1], this.cardStack[7][1]));
		dumpStr.append(String.format(content, CardStack.SYM_SPADE,
				this.cardStack[1][2], this.cardStack[1][2],
				this.cardStack[2][2], this.cardStack[3][2],
				this.cardStack[4][2], this.cardStack[5][2],
				this.cardStack[6][2], this.cardStack[7][2]));
		dumpStr.append(String.format(content, CardStack.SYM_CLUB,
				this.cardStack[1][3], this.cardStack[1][3],
				this.cardStack[2][3], this.cardStack[3][3],
				this.cardStack[4][3], this.cardStack[5][3],
				this.cardStack[6][3], this.cardStack[7][3]));
		dumpStr.append(separator);
		return dumpStr;
	}
}