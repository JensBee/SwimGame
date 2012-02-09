package cardGame.games.swimming;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import cardGame.card.CardDeck;
import cardGame.card.CardDeck.Card;
import cardGame.card.CardDeck.Deck;

/**
 * Handles a collection of cards (the stack). Besides storing the availability
 * of a cards, this class allows storing of custom values attached to each card.
 * This makes it possible to use a CardStack for storing card weightings for
 * further processing.
 */
public class CardStack {
    /** All cards possible in this game. */
    static final List<Card> CARDS = Deck.SKAT.getCards();
    /** Maximum number of points reachable. */
    static final int STACKVALUE_MAX = 31; // A + B + D
    /** Marks a card a not being part of the stack. */
    static final byte CARD_UNAVAILABLE = -1;
    /** Marks a card as being part of the stack. */
    static final byte CARD_AVAILABLE = 1;
    /**
     * Stored values of single cards. Any negative value means that this cards
     * isn't available. Positive values are used for ratings.
     **/
    private final byte[] cardStack = new byte[Deck.SKAT.size()];

    /** Empty constructor. */
    CardStack() {
	this.resetCardValues();
    }

    /**
     * Sets a custom value instead of the predefined flags for a card. This
     * passes by the check if a card is available. If you want to add a card use
     * {@link #addCard(int)} or {@link #addCard(byte[])} instead.
     * 
     * @param card
     *            Card witch will get a new value
     * @param value
     *            New value for the card. This value must fit into the bounds
     *            defined by the byte type.
     * @see Byte#MIN_VALUE
     * @see Byte#MAX_VALUE
     */
    final void setCardValue(final Card card, final float value) {
	if (CARDS.indexOf(card) == -1) {
	    throw new IllegalArgumentException("Unknown card specified.");
	}
	this.cardStack[CARDS.indexOf(card)] = this.enforceValueBounds(value);
    }

    /**
     * Restrict a card value to the defined legal value bounds.
     * 
     * @param value
     *            Value to check
     * @return The passed in value modified to fit into the bounds
     */
    private byte enforceValueBounds(final float value) {
	int newValue = Math.round(value);
	// value is larger than byte
	if (newValue > Byte.MAX_VALUE) {
	    newValue = Byte.MAX_VALUE;
	}
	// negative values are reserved for internal use
	if (newValue <= 0) {
	    newValue = 0;
	}
	return (byte) newValue;
    }

    /**
     * Adds a value to an already stored Card value. If the new value exceeds
     * the value bounds it will be cut down to the maximum possible value.
     * 
     * @param card
     *            The Card whose value should be updated
     * @param value
     *            Value to add to the already stored
     */
    final void addToCardValue(final Card card, final float value) {
	if (CARDS.indexOf(card) == -1) {
	    throw new IllegalArgumentException("Unknown card specified.");
	}
	float newValue = this.getCardValue(card) + value;
	this.setCardValue(card, this.enforceValueBounds(newValue));
    }

    /**
     * Get the value stored for a specific Card.
     * 
     * @param card
     *            The Card whose value to get
     * @return Value stored for the Card
     */
    final byte getCardValue(final Card card) {
	if (CARDS.indexOf(card) == -1) {
	    throw new IllegalArgumentException("Unknown card specified.");
	}
	return this.cardStack[CARDS.indexOf(card)];
    }

    /**
     * Dump the current stack as nicely formatted table.
     * 
     * @return The card stack as text-table
     */
    final StringBuffer dump() {
	final StringBuffer dump = new StringBuffer();
	final String separator = "\n-+----+----+----+----+----+----+----+----+";

	// header with card types
	dump.append(" |");
	for (CardDeck.Type cardType : Deck.SKAT.types()) {
	    dump.append(String.format("%4s|", cardType));
	}
	dump.append(separator);

	// card values
	for (CardDeck.Color cardColor : Deck.SKAT.colors()) {
	    dump.append("\n" + cardColor);
	    for (Card card : Deck.SKAT.getCardsByColor(cardColor)) {
		dump.append(String.format("|%+4d", this.getCardValue(card)));
	    }
	    dump.append("|");
	}

	dump.append(separator);
	return dump;
    }

    /**
     * Returns a string representation of the cards available in this stack.
     * 
     * @return String representation of this stacks cards
     */
    @Override
    public final String toString() {
	StringBuffer cards = new StringBuffer();
	for (Card card : this.getCards()) {
	    cards.append(card.toString());
	}
	return cards.toString();
    }

    /**
     * Removes all cards from the stack (setting their values to {@value
     * CARD_UNAVAILABLE}) and set only the ones as available (setting their
     * values to {@value CARD_AVAILABLE}) that were passed in.
     * 
     * @param newCards
     *            Cards to add
     */
    final void setCards(final Collection<Card> newCards) {
	this.resetCardValues();
	for (Card card : newCards) {
	    this.setCardValue(card, CARD_AVAILABLE);
	}
    }

    /**
     * Get all cards currently part of the stack.
     * 
     * @return All cards currently in the stack
     */
    final Collection<Card> getCards() {
	List<Card> cardList = new ArrayList<Card>(Deck.SKAT.size());
	for (int i = 0; i < this.cardStack.length; i++) {
	    byte cardValue = this.cardStack[i];
	    if (cardValue >= 0) {
		cardList.add(CARDS.get(i));
	    }
	}
	return cardList;
    }

    /**
     * Reset all stored card values. This will set all cards as not being part
     * of the stack.
     */
    final void resetCardValues() {
	Arrays.fill(this.cardStack, CARD_UNAVAILABLE);
    }

    /**
     * Check if a card is available on this stack.
     * 
     * @param card
     *            Card to check
     * @return True if it is in the stack
     */
    final boolean containsCard(final Card card) {
	if (this.getCardValue(card) == CardStack.CARD_UNAVAILABLE) {
	    return false;
	}
	return true;
    }
}
