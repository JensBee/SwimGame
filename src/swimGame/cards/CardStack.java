package swimGame.cards;

import java.util.Arrays;
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
	public static final int STACK_SIZE = (CardStack.CARDS_MAX_CARD * CardStack.CARDS_MAX_COLOR);
	// the minimum of the reachable points
	public static final int STACKVALUE_MIN = 24; // 7 + 8 + 9
	// the maximum of the reachable points
	public static final int STACKVALUE_MAX = 31; // A + B + D
	// flags for the card-stack array
	public static final byte FLAG_HAS_CARD = 1;
	public static final byte FLAG_NO_CARD = 0;
	public static final byte FLAG_UNINITIALIZED = -1;

	// Card names for pretty printing
	public static final char[] CARD_SYMBOLS = { '♦', '♥', '♠', '♣' };
	public static final String[] CARD_NAMES = { "7", "8", "9", "10", "J", "Q",
			"K", "A" };
	private static String[] cardStackStr;

	// Card stack of this table
	private byte[] cardStack = new byte[CardStack.STACK_SIZE];

	// card counter for this stack
	private byte cardsCount = 0;

	private void buildStringArray() {
		CardStack.cardStackStr = new String[CardStack.CARDS_MAX];
		int idx = 0;
		for (int color = 0; color < CardStack.CARDS_MAX_COLOR; color++) {
			for (int card = 0; card < CardStack.CARDS_MAX_CARD; card++) {
				CardStack.cardStackStr[idx++] = CardStack.CARD_SYMBOLS[color]
						+ CardStack.CARD_NAMES[card];
			}
		}
	}

	/**
	 * Empty constructor
	 */
	public CardStack() {
		this.buildStringArray();
	}

	/**
	 * Constructor
	 * 
	 * @param filled
	 *            If true, the card stack will be initially full (i.e. all cards
	 *            are on the stack)
	 */
	public CardStack(final boolean filled) {
		this.buildStringArray();
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
	public CardStack(final byte[] initialCards) throws Exception {
		this.buildStringArray();
		if (initialCards.length < 3) {
			throw new Exception(
					"You must give three cards to initialize a CardStack!");
		}
		this.addCards(initialCards);
	}

	/**
	 * Get a string representation of the given card.
	 * 
	 * @return A string that looks like [♥A] for heart-ace
	 */
	public static String cardToString(final int card) {
		CardStack.checkCardPosition(card);
		return "[" + CardStack.cardStackStr[card] + "]";
	}

	/**
	 * Get a random card out of the stack
	 * 
	 * @return Byte array representing the card
	 * @see swimGame.cards.CardUtils#initCardStack
	 */
	public byte getRandomCard() throws Exception {
		if (this.empty) {
			throw new Exception("Unable to get a card. Stack is empty!");
		}

		if (CardStack.random == null) {
			CardStack.random = new Random();
		}

		// try to find a random card that's still on the stack
		// TODO: make this aware of available cards to be more intelligent
		while (true) {
			int card = CardStack.random.nextInt(CardStack.CARDS_MAX_CARD
					* CardStack.CARDS_MAX_COLOR);
			if (this.cardStack[card] == CardStack.FLAG_HAS_CARD) {
				// card is there .. take it
				this.cardStack[card] = CardStack.FLAG_NO_CARD;
				return (byte) card;
			}
		}
	}

	/**
	 * Add a card to this stack
	 * 
	 * @param card
	 *            The card to add
	 */
	public void addCard(int card) {
		this.cardStack[card] = CardStack.FLAG_HAS_CARD;
		this.empty = false;
		this.cardsCount++;
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
		int idx = 0;
		for (byte card : this.cardStack) {
			if (card == CardStack.FLAG_HAS_CARD) {
				cards += CardStack.cardToString(idx);
			}
			idx++;
		}
		return cards;
	}

	/**
	 * Get a full card stack
	 * 
	 * @param full
	 * @return
	 */
	public static byte[] getNewStack(final boolean full) {
		byte[] cardStack = new byte[CardStack.CARDS_MAX_CARD
				* CardStack.CARDS_MAX_COLOR];
		if (full) {
			// this stack will contain all cards
			for (int i = 0; i < cardStack.length; i++) {
				cardStack[i] = 1;
			}
		}
		return cardStack;
	}

	/** Clear out the current card-stack */
	public void clear() {
		this.cardStack = new byte[CardStack.CARDS_MAX_CARD
				* CardStack.CARDS_MAX_COLOR];
		this.empty = true;
	}

	/** Fills the card-stack with the given value */
	public void fill(byte value) {
		Arrays.fill(this.cardStack, value);
	}

	/** Get the byte array for this card-stack */
	public byte[] getArray() {
		return this.cardStack.clone();
	}

	/** Remove a card from this stack */
	public void removeCard(final int card) {
		this.cardStack[card] = CardStack.FLAG_NO_CARD;
	}

	/** Check if a card type is in the legal range */
	private static void checkCardTypeRange(final int cardType) {
		if ((cardType < 0) || (cardType > CardStack.CARDS_MAX_CARD)) {
			throw new IllegalArgumentException(String.format(
					"Card type %d out of bounds (%d-%d)", cardType, 0,
					CardStack.CARDS_MAX_CARD));
		}
	}

	/** Check if a card color is in the legal range */
	private static void checkCardColorRange(final int cardColor) {
		if ((cardColor < 0) || (cardColor > CardStack.CARDS_MAX_COLOR)) {
			throw new IllegalArgumentException(String.format(
					"Card color %d out of bounds (%d-%d)", cardColor, 0,
					CardStack.CARDS_MAX_COLOR));
		}
	}

	/** Check if a cards position in the stack is a legal one */
	private static void checkCardPosition(final int card) {
		if ((card > CardStack.STACK_SIZE) || (card < 0)) {
			throw new IllegalArgumentException(String.format(
					"Card type %d out of bounds (%d-%d)", card, 0,
					CardStack.STACK_SIZE));
		}
	}

	/** Get all cards for a specific card-type (7,8,9,10,J,Q,K,A) */
	public byte[] getCardsByType(int cardType) {
		CardStack.checkCardTypeRange(cardType);
		byte[] typeCards = new byte[CardStack.CARDS_MAX_COLOR];

		int offset = cardType;
		for (int i = 0; i < CardStack.CARDS_MAX_COLOR; i++) {
			typeCards[i] = (byte) (offset + (i * CardStack.CARDS_MAX_CARD));
		}
		return typeCards;
	}

	/** Get all cards for a specific card-color (♦, ♥, ♠, ♣) */
	public byte[] getCardsByColor(int cardColor) {
		CardStack.checkCardColorRange(cardColor);
		byte[] colorCards = new byte[CardStack.CARDS_MAX_CARD];

		int offset = cardColor * CardStack.CARDS_MAX_CARD;
		for (int i = 0; i < CardStack.CARDS_MAX_CARD; i++) {
			colorCards[i] = (byte) (offset + i);
		}
		return colorCards;
	}

	/**
	 * Iterator that steps from top left to right through the stack-array
	 * 
	 * @author Jens Bertram <code@jens-bertram.net>
	 * 
	 */
	public class StackIterator implements Iterator<Integer> {
		int pointer = 0;

		public StackIterator() {
			this.pointer = 0;
		}

		@Override
		public boolean hasNext() {
			return ((this.pointer + 1) < CardStack.this.cardStack.length) ? true
					: false;
		}

		@Override
		public Integer next() {
			if (this.hasNext()) {
				return Integer
						.valueOf(CardStack.this.cardStack[this.pointer++]);
			}
			throw new IllegalStateException("You tried to step out of bounds.");
		}

		@Override
		public void remove() throws IllegalStateException {
			throw new IllegalStateException("Operation not supported.");
		}

		/** Get the current card */
		public int getCard() {
			// be careful we're one ahead here
			return this.pointer - 1;
		}

		public boolean hasNextColor() {
			int col = this.getCard() / CardStack.CARDS_MAX_CARD;
			if ((col + 1) < CardStack.CARDS_MAX_COLOR) {
				return true;
			}
			return false;
		}

		/** Move pointer to beginning of next color */
		public void nextColor() {
			if (this.hasNextColor()) {
				int col = this.getCard() / CardStack.CARDS_MAX_CARD;
				this.pointer = ((col + 1) * CardStack.CARDS_MAX_CARD);
			} else {
				throw new IllegalStateException(
						"You tried to step out of bounds.");
			}
		}

		/** Get the current card type */
		public int getCardType() {
			return CardStack.this.getCardType(this.getCard());
		}

		/** Get the color of the current card */
		public int getCardColor() {
			if (this.pointer < CardStack.CARDS_MAX_CARD) {
				return 0;
			}
			return CardStack.this.getCardColor(this.getCard());
		}
	}

	/**
	 * Get an array with all cards currently in this stack. This will only find
	 * cards witch were added with the appropriate add functions.
	 * 
	 * @return A byte array containing only available cards
	 */
	public byte[] getCards() {
		byte[] cards = new byte[this.cardsCount];
		int currentCard = 0;
		for (int i = 0; i < this.cardStack.length; i++) {
			if (currentCard > this.cardsCount) {
				// we've found all
				break;
			}
			if (this.hasCard(i)) {
				cards[currentCard++] = (byte) i;
			}
		}
		return cards;
	}

	/**
	 * Get the color number for a card.
	 * 
	 * @param card
	 *            The card to check
	 * @return The card color: 0=♦; 1=♥; 2=♠; 3=♣
	 */
	public int getCardColor(int card) {
		CardStack.checkCardPosition(card);
		return card / CardStack.CARDS_MAX_CARD;
	}

	/** Get the type for a card */
	public int getCardType(int card) {
		CardStack.checkCardPosition(card);
		return (card < CardStack.CARDS_MAX_CARD) ? card
				: (card % CardStack.CARDS_MAX_CARD);
	}

	/** Add a bunch of cards */
	public void addCards(byte[] cards) {
		for (int card : cards) {
			this.addCard(card);
		}
		this.empty = false;
	}

	/**
	 * Sets a custom value instead of the predefined flags for a card. This
	 * passes by the check if a card is available (set CardStack#FLAG_HAS_CARD).
	 * If you want to add a card use addCard() or addCards() instead.
	 */
	public void setCardValue(int card, byte value) {
		CardStack.checkCardPosition(card);
		this.cardStack[card] = value;
	}

	/** Gets the value for a card */
	public byte getCardValue(int card) {
		CardStack.checkCardPosition(card);
		return this.cardStack[card];
	}

	/** Checks if this card is in this stack */
	public boolean hasCard(int card) {
		CardStack.checkCardPosition(card);
		return (this.cardStack[card] == CardStack.FLAG_HAS_CARD) ? true : false;
	}

	/** Dump the current stack as nicely formatted table */
	public StringBuffer dumpStack() {
		StringBuffer dumpStr = new StringBuffer();

		String separator = "\n-+----+----+----+----+----+----+----+----+";
		String content = "\n%s|%+4d|%+4d|%+4d|%+4d|%+4d|%+4d|%+4d|%+4d|";

		dumpStr.append(String.format(
				" |   7|   8|   9|  10|   %s|   %s|   %s|   %s|",
				CardStack.CARD_NAMES[4], CardStack.CARD_NAMES[5],
				CardStack.CARD_NAMES[6], CardStack.CARD_NAMES[7]));
		dumpStr.append(separator);
		for (int i = 0; i < CardStack.CARDS_MAX_COLOR; i++) {
			int offset = (i * CardStack.CARDS_MAX_CARD);
			dumpStr.append(String.format(content, CardStack.CARD_SYMBOLS[i],
					this.cardStack[offset + 0], this.cardStack[offset + 1],
					this.cardStack[offset + 2], this.cardStack[offset + 3],
					this.cardStack[offset + 4], this.cardStack[offset + 5],
					this.cardStack[offset + 6], this.cardStack[offset + 7]));
		}
		dumpStr.append(separator);
		return dumpStr;
	}
}