package swimgame.table;

import java.util.Arrays;
import java.util.Iterator;

import swimgame.Util;
import swimgame.table.logic.TableLogic;

/**
 * A card stack as part of the game. This is initially the full set of cards
 * available in the game.
 * 
 * <pre>
 * The CardStack byte array as reference:
 * 
 *    7  8  9 10  J  Q  K  A
 * ♦ 00 01 02 03 04 05 06 07
 * ♥ 08 09 10 11 12 13 14 15
 * ♠ 16 17 18 19 20 21 22 23
 * ♣ 24 25 26 27 28 29 30 31
 * </pre>
 * 
 * @author <a href="mailto:code@jens-bertram.net">Jens Bertram</a>
 * 
 */
public class CardStack {
    /** Denotes this stacks empty state. */
    private boolean empty = true;

    // one-based card array bounds, just for ease of use / readability
    /** How many cards of a color a present in a card stack? */
    public static final int CARDS_MAX_COLOR = 4;
    /** How many different cards per color a present in a card stack? */
    public static final int CARDS_MAX_CARD = 8;
    /** How many cards are present in a card stack? */
    public static final int STACK_SIZE = (CardStack.CARDS_MAX_CARD * CardStack.CARDS_MAX_COLOR);
    /** Minimum number of points reachable. */
    public static final int STACKVALUE_MIN = 24; // 7 + 8 + 9
    /** Maximum number of points reachable. */
    public static final int STACKVALUE_MAX = 31; // A + B + D
    // flags for the card-stack array
    /** Flag indicating a card is present in the stack. */
    public static final byte FLAG_HAS_CARD = 1;
    /** Flag indicating a card is not present in the stack. */
    public static final byte FLAG_NO_CARD = 0;
    /** Flag indicating a card information is not initialized. */
    public static final byte FLAG_UNINITIALIZED = -1;

    // Card names for pretty printing
    /** Symbols for card colors. */
    public static final char[] CARD_SYMBOLS = { '♦', '♥', '♠', '♣' };
    /** Name abbreviations for card types. */
    public static final String[] CARD_NAMES = { "7", "8", "9", "10", "J", "Q",
	    "K", "A" };
    /** Array with string representation for all cards in this stack. */
    private static String[] cardStackStr = null;

    /** Card stack of this table. **/
    private byte[] cardStack = new byte[CardStack.STACK_SIZE];

    /**
     * Card counter. This counts only cards that were added or removed by the
     * appropriate functions.
     */
    private byte cardsCount = 0;

    /**
     * Build the array containing the string representation of the cards in this
     * card stack.
     */
    private static void buildStringArray() {
	if (CardStack.cardStackStr != null) {
	    return;
	}
	CardStack.cardStackStr = new String[CardStack.STACK_SIZE];
	int idx = 0;
	for (int color = 0; color < CardStack.CARDS_MAX_COLOR; color++) {
	    for (int currentCard = 0; currentCard < CardStack.CARDS_MAX_CARD; currentCard++) {
		CardStack.cardStackStr[idx++] = CardStack.CARD_SYMBOLS[color]
			+ CardStack.CARD_NAMES[currentCard];
	    }
	}
    }

    /**
     * Empty constructor. This will create a card stack with no card being
     * marked as present. So the stack will be initially empty.
     */
    public CardStack() {
	CardStack.buildStringArray();
    }

    /**
     * Creates a filled stack. This means all cards will be marked as being
     * present.
     * 
     * @param filled
     *            If true, the card stack will be initially full (i.e. all cards
     *            are on the stack)
     */
    public CardStack(final boolean filled) {
	CardStack.buildStringArray();
	if (filled) {
	    // The card stack will be initially full
	    this.cardStack = CardStack.getNewStack(true);
	    this.empty = false;
	}
    }

    /**
     * Creates a card stack with the given cards as set as being present on the
     * stack.
     * 
     * @param initialCards
     *            An initial set of cards to initialize this stack
     */
    public CardStack(final byte[] initialCards) {
	CardStack.buildStringArray();
	// TODO: remove initial card amount check
	if (initialCards.length < TableLogic.INITIAL_CARDS) {
	    System.out.println("OUT!");
	}
	this.addCard(initialCards);
    }

    /**
     * Adds a single card to this stack.
     * 
     * @param card
     *            The card to add, specified by it's card-stack array index
     */
    public final void addCard(final int card) {
	if (this.cardStack[card] != CardStack.FLAG_HAS_CARD) {
	    this.cardStack[card] = CardStack.FLAG_HAS_CARD;
	    this.empty = false;
	    this.cardsCount++;
	}
    }

    /**
     * Removes a card from the stack.
     * 
     * @param card
     *            The card to remove by it's index
     * @throws TableException
     *             Thrown if card could not be dropped
     */
    public final void removeCard(final int card) throws TableException {
	if (this.cardStack[card] == CardStack.FLAG_NO_CARD) {
	    throw new TableException(TableException.Exception.DROP_NOT_OWNED,
		    CardStack.cardToString(card));
	}
	this.cardStack[card] = CardStack.FLAG_NO_CARD;
	this.cardsCount--;
    }

    /**
     * Add a bunch of cards to the stack.
     * 
     * @param cards
     *            Array of cards to add. Each card is specified by it's
     *            card-stack array index.
     */
    public final void addCard(final byte[] cards) {
	for (int card : cards) {
	    this.addCard(card);
	}
	CardStack.this.empty = false;
    }

    /**
     * Returns a string representation of the cards available in this stack.
     * 
     * @return String representation of this stacks cards
     */
    @Override
    public final String toString() {
	if (this.empty) {
	    return "";
	}
	StringBuffer cards = new StringBuffer();
	for (byte card : this.getCards()) {
	    cards.append(CardStack.cardToString(card));
	}
	return cards.toString();
    }

    /**
     * Get a string representation of the given card.
     * 
     * @param card
     *            The card by index
     * @return A string that looks like [♥A] for heart-ace
     */
    public static final String cardToString(final int card) {
	CardStack.validateCardIndex(card);
	return "[" + CardStack.cardStackStr[card] + "]";
    }

    /**
     * Get a string representation of the given cards.
     * 
     * @param cards
     *            Byte array with cards to print
     * @return String representation of the given cards
     */
    public static final String cardsToString(final byte[] cards) {
	StringBuffer cardString = new StringBuffer();
	for (byte card : cards) {
	    CardStack.validateCardIndex(card);
	    cardString.append("[" + CardStack.cardStackStr[card] + "]");
	}
	return cardString.toString();
    }

    /**
     * Get the current card stack as array.
     * 
     * @return Array representation of this card-stack content
     */
    public final byte[] asArray() {
	return this.cardStack.clone();
    }

    /**
     * Create a new card stack.
     * 
     * @param full
     *            If true it will be full
     * @return The newly created stack
     */
    private static byte[] getNewStack(final boolean full) {
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

    /**
     * Fills the card-stack with a custom value.
     * 
     * @param value
     *            The value to fill the stack with
     */
    public final void fill(final byte value) {
	Arrays.fill(this.cardStack, value);
    }

    /**
     * Verify that a card type is in the legal range from 0 to
     * {@value #CARDS_MAX_CARD}.
     * 
     * @param cardType
     *            The card type to check
     */
    public static void validateCardType(final int cardType) {
	if ((cardType < 0) || (cardType > CardStack.CARDS_MAX_CARD)) {
	    throw new IllegalArgumentException(String.format(
		    "Card type %d out of bounds (%d-%d)", cardType, 0,
		    CardStack.CARDS_MAX_CARD));
	}
    }

    /**
     * Gets the stored value for a card.
     * 
     * @param card
     *            Card to get the value for
     * @return The value that was stored for the given card. This may be
     *         {@link #FLAG_UNINITIALIZED} if no parameter was set for this
     *         card. {@link #FLAG_HAS_CARD} if the card is in this stack or
     *         {@link #FLAG_NO_CARD} if it isn't. Other values might have been
     *         set by the user by using {@link #setCardValue(int, byte)}.
     */
    public final byte getCardValue(final int card) {
	CardStack.validateCardIndex(card);
	return this.cardStack[card];
    }

    /**
     * Get the type for a card.
     * 
     * @param card
     *            Card to check the type for
     * @return The type of the given card
     */
    public static int getCardType(final int card) {
	CardStack.validateCardIndex(card);
	if (card < CardStack.CARDS_MAX_CARD) {
	    return card;
	} else {
	    return card % CardStack.CARDS_MAX_CARD;
	}
    }

    /**
     * Verify that a card color is in the legal range.
     * 
     * @param cardColor
     *            The card color to check
     */
    public static void validateCardColor(final int cardColor) {
	if ((cardColor < 0) || (cardColor >= CardStack.CARDS_MAX_COLOR)) {
	    throw new IllegalArgumentException(String.format(
		    "Card color %d out of bounds (%d-%d)", cardColor, 0,
		    CardStack.CARDS_MAX_COLOR));
	}
    }

    /**
     * Check if a cards position in the stack is a legal one.
     * 
     * @param card
     *            Card to check
     */
    public static void validateCardIndex(final int card) {
	if ((card >= CardStack.STACK_SIZE) || (card < 0)) {
	    throw new IllegalArgumentException(String.format(
		    "Card type %d out of bounds (%d-%d)", card, 0,
		    CardStack.STACK_SIZE));
	}
    }

    /**
     * Get all cards for a specific card-type (7,8,9,10,J,Q,K,A).
     * 
     * @param cardType
     *            The card type to get as int (0-7)
     * @return Card array indices related to the given card type
     */
    public static byte[] getCardsByType(final int cardType) {
	CardStack.validateCardType(cardType);
	byte[] typeCards = new byte[CardStack.CARDS_MAX_COLOR];

	final int offset = cardType;
	for (int i = 0; i < CardStack.CARDS_MAX_COLOR; i++) {
	    typeCards[i] = (byte) (offset + (i * CardStack.CARDS_MAX_CARD));
	}
	return typeCards;
    }

    /**
     * Get all cards for a specific card-color (♦, ♥, ♠, ♣). This returns all
     * cards, regardless if they owned or not.
     * 
     * @param cardColor
     *            The color as integer (0-3)
     * @return Card array indices related to the given color
     */
    public static byte[] getCardsByColor(final int cardColor) {
	CardStack.validateCardColor(cardColor);
	byte[] colorCards = new byte[CardStack.CARDS_MAX_CARD];

	final int offset = cardColor * CardStack.CARDS_MAX_CARD;
	for (int i = 0; i < CardStack.CARDS_MAX_CARD; i++) {
	    colorCards[i] = (byte) (offset + i);
	}
	return colorCards;
    }

    /**
     * Get the color number for a card.
     * 
     * @param card
     *            The card to check
     * @return The card color: 0=♦; 1=♥; 2=♠; 3=♣
     */
    public static final int getCardColor(final int card) {
	CardStack.validateCardIndex(card);
	return card / CardStack.CARDS_MAX_CARD;
    }

    /**
     * Iterator that steps from top left to right through the stack-array.
     * 
     * @author <a href="mailto:code@jens-bertram.net">Jens Bertram</a>
     * 
     */
    public class CardIterator implements Iterator<Integer> {
	/** Pointer to the current card. */
	private int pointer = 0;

	/** Empty constructor. */
	public CardIterator() {
	    this.pointer = 0;
	}

	@Override
	public final boolean hasNext() {
	    // CHECKSTYLE:OFF
	    return ((this.pointer + 1) < CardStack.this.cardStack.length) ? true
		    : false;
	    // CHECKSTYLE:ON
	}

	@Override
	public final Integer next() {
	    if (this.hasNext()) {
		return Integer
			.valueOf(CardStack.this.cardStack[this.pointer++]);
	    }
	    throw new IllegalStateException("You tried to step out of bounds.");
	}

	@Override
	public final void remove() {
	    throw new IllegalStateException("Operation not supported.");
	}

	/**
	 * Get the current card.
	 * 
	 * @return Pointer to the current card
	 */
	public final int getCard() {
	    // be careful we're one ahead here
	    return this.pointer - 1;
	}

	/**
	 * Get the current card type.
	 * 
	 * @return The type of the current card
	 */
	public final int getCardType() {
	    return CardStack.getCardType(this.getCard());
	}

	/**
	 * Get the color of the current card.
	 * 
	 * @return Color of the current card
	 */
	public final int getCardColor() {
	    if (this.pointer < CardStack.CARDS_MAX_CARD) {
		return 0;
	    }
	    return CardStack.getCardColor(this.getCard());
	}
    }

    /**
     * Get an array with all cards currently in this stack. This will only find
     * cards witch were added with the appropriate add functions.
     * 
     * @return A byte array containing only available cards
     */
    public final byte[] getCards() {
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
     * Calculate the drop value for the given cards.
     * 
     * TODO: fuse with getValue()
     * 
     * @param cards
     *            Cards to do the calculation for
     * @return The value of the given cards if dropped
     */
    public static double calculateValue(final byte[] cards) {
	int value = 0;
	// three of a color?
	for (byte color = 0; color < CardStack.CARDS_MAX_COLOR; color++) {
	    int newValue = 0;
	    for (byte card : CardStack.getCardsByColor(color)) {
		if (CardStack.hasCard(card, cards)) {
		    newValue = newValue + CardStack.getCardWorth(card);
		}
	    }
	    // CHECKSTYLE:OFF
	    value = (newValue > value) ? newValue : value;
	    // CHECKSTYLE:ON
	}

	// three of a type?
	for (byte type = 0; type < CardStack.CARDS_MAX_CARD; type++) {
	    int newValue = 0;
	    int count = 0;
	    for (byte card : CardStack.getCardsByType(type)) {
		if (CardStack.hasCard(card, cards)) {
		    newValue = newValue + CardStack.getCardWorth(card);
		    count++;
		}
	    }

	    if (count == TableLogic.RULE_GOAL_CARDS_BY_TYPE) {
		return DefaultTableController.WORTH_THREE_OF_SAME_TYPE;
	    }
	}
	return value;
    }

    /**
     * Get the drop value for the current card stack.
     * 
     * TODO: fuse with getValue(byte[]..)
     * 
     * @return The value of the given cards if dropped
     */
    public final double getValue() {
	int value = 0;
	// three of a color?
	for (byte color = 0; color < CardStack.CARDS_MAX_COLOR; color++) {
	    int newValue = 0;
	    for (byte card : CardStack.getCardsByColor(color)) {
		if (this.hasCard(card)) {
		    newValue = newValue + CardStack.getCardWorth(card);
		}
	    }
	    // CHECKSTYLE:OFF
	    value = (newValue > value) ? newValue : value;
	    // CHECKSTYLE:ON
	}

	// three of a type?
	for (byte type = 0; type < CardStack.CARDS_MAX_CARD; type++) {
	    int newValue = 0;
	    int count = 0;
	    for (byte card : CardStack.getCardsByType(type)) {
		if (this.hasCard(card)) {
		    newValue = newValue + CardStack.getCardWorth(card);
		    count++;
		}
	    }

	    if (count == TableLogic.RULE_GOAL_CARDS_BY_TYPE) {
		return DefaultTableController.WORTH_THREE_OF_SAME_TYPE;
	    }
	}
	return value;
    }

    /**
     * Checks if this card is in this stack.
     * 
     * @param card
     *            Card to check
     * @return True if it's in the stack
     */
    public final boolean hasCard(final int card) {
	CardStack.validateCardIndex(card);
	// CHECKSTYLE:OFF
	return (this.cardStack[card] == CardStack.FLAG_HAS_CARD) ? true : false;
	// CHECKSTYLE:ON
    }

    /**
     * Checks if a given card is in the given cards array.
     * 
     * @param card
     *            The card to search for
     * @param cards
     *            The cards to check against
     * @return True if the card was found
     */
    public static boolean hasCard(final int card, final byte[] cards) {
	CardStack.validateCardIndex(card);
	for (byte aCard : cards) {
	    if (aCard == card) {
		return true;
	    }
	}
	return false;
    }

    /**
     * Get the value of a card as calculated at the end of the game.
     * 
     * @param card
     *            The card to do the calculation for
     * @return The calculated card value
     */
    public static byte getCardWorth(final int card) {
	byte worth;
	CardStack.validateCardIndex(card);
	int positionValue = 0;
	if (card < CardStack.CARDS_MAX_CARD) {
	    positionValue = card;
	}
	positionValue = (card - ((card / CardStack.CARDS_MAX_CARD) * CardStack.CARDS_MAX_CARD));
	// CHECKSTYLE:OFF
	if (positionValue > 2) {
	    worth = (byte) (positionValue < 7 ? 10 : 11);
	} else {
	    worth = (byte) (positionValue + 7);
	}
	// CHECKSTYLE:ON
	return worth;
    }

    /**
     * Sets a custom value instead of the predefined flags for a card. This
     * passes by the check if a card is available. If you want to add a card use
     * {@link #addCard(int)} or {@link #addCard(byte[])} instead.
     * 
     * @param card
     *            Card witch will get a new value
     * @param value
     *            New value for the card
     */
    public final void setCardValue(final int card, final byte value) {
	CardStack.validateCardIndex(card);
	CardStack.this.cardStack[card] = value;
    }

    /**
     * Dump the current stack as nicely formatted table.
     * 
     * @return The card stack as text-table
     */
    public final StringBuffer dump() {
	final StringBuffer dumpStr = new StringBuffer();

	final String separator = "\n-+----+----+----+----+----+----+----+----+";
	String content = "\n%s|%+4d|%+4d|%+4d|%+4d|%+4d|%+4d|%+4d|%+4d|";

	// CHECKSTYLE:OFF
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
	// CHECKSTYLE:ON
	dumpStr.append(separator);
	return dumpStr;
    }

    /**
     * Get a random card from of the stack.
     * 
     * @return Byte array representing the card
     * @see swimGame.cards.CardUtils#initCardStack
     */
    public final byte getRandomCard() {
	if (this.empty) {
	    throw new IllegalArgumentException(
		    "Unable to get a card. Stack is empty!");
	}

	// try to find a random card that's still on the stack
	// TODO: make this aware of available cards to be more intelligent
	while (true) {
	    // TODO: changed random here :)
	    final int card = Util.getRandomInt(CardStack.STACK_SIZE - 1);
	    if (CardStack.this.cardStack[card] == CardStack.FLAG_HAS_CARD) {
		// card is there .. take it
		this.cardStack[card] = CardStack.FLAG_NO_CARD;
		return (byte) card;
	    }
	}
    }
}