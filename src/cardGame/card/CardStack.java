package cardGame.card;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import cardGame.util.Util;

public class CardStack {
    /**
     * The cards array storing values associated with cards. Negative values
     * (including zero) are reserved for internal flags.
     */
    private final byte[] cardStack;
    /** List of cards n this stack. */
    private final List<CardDeck.Card> deckCards;

    /** Internal array flags. */
    public static enum Flags {
	/** An uninitialized field in the stack array. */
	UNINITIALIZED,
	/** Flag indicating a card is present in the stack. */
	NO_CARD,
	/** Flag indicating a card is not present in the stack. */
	HAS_CARD;

	/**
	 * Get the id for a flag.
	 * 
	 * @return Flag-Id
	 */
	byte id() {
	    return (byte) (this.ordinal() * -1);
	}
    }

    /**
     * Constructor.
     * 
     * @param deck
     *            The deck to use for this stack
     */
    public CardStack(final CardDeck.Deck deck) {
	System.out.println("CardDeck-size: " + deck.size());
	this.cardStack = new byte[deck.size()];
	Arrays.fill(this.cardStack, Flags.UNINITIALIZED.id());
	this.deckCards = Collections.unmodifiableList(deck.getCards());
    }

    /** Set all cards of this stack as being available. */
    public final void full() {
	Arrays.fill(this.cardStack, Flags.HAS_CARD.id());
    }

    /**
     * Get a random card from of the stack.
     * 
     * @return A random {@link PlayerAICard}
     */
    public final CardDeck.Card getRandomCard() {
	// try to find a random card that's still on the stack
	// TODO: make this aware of available cards to be more intelligent
	while (true) {
	    final int card = Util.getRandomInt(this.cardStack.length - 1);
	    if (this.cardStack[card] == Flags.HAS_CARD.id()) {
		// card is there .. take it
		this.cardStack[card] = Flags.NO_CARD.id();
		return this.deckCards.get(card);
	    }
	}
    }
}
