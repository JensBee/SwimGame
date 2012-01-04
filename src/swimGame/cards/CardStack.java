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
	// Card handling helpers
	private final CardUtils cardUtils = new CardUtils();
	// one random number generator for all stacks should be enough
	private static Random random = null;
	// track the number of cards in this stack
	private int cards = 0;
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
			this.cardStack = this.cardUtils.getStack(true);
			this.cards = CardStack.CARDS_MAX;
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
		this.cards = 3;
		this.empty = false;
	}

	/**
	 * Randomly get a card off of the stack
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
		final boolean gotCard = false;
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
		this.cards++;
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
					cards += this.cardUtils.cardToString(new int[] { card,
							color });
				}
			}
		}
		return cards;
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
	 * Get the cards of this stack condensed in an linear byte array
	 * 
	 * @return
	 */
	public byte[] getCards() {
		byte[] cards = new byte[6];
		for (int card = 0; card < CardStack.CARDS_MAX_CARD; card++) {
			for (int color = 0; color < CardStack.CARDS_MAX_COLOR; color++) {

			}
		}
		return cards;
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
			this.col = 0;
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

		@Override
		public void remove() throws IllegalStateException {
			throw new IllegalStateException("Operation not supported.");
		}

		public int getCard() {
			return this.col;
		}

		public int getColor() {
			return this.row;
		}
	}
}