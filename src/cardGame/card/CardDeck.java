package cardGame.card;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class CardDeck {
    /** Holds all possible card colors. */
    public enum Color {
	// CHECKSTYLE:OFF
	DIAMOND("♦"), HEART("♥"), CLUB("♣"), SPADE("♠");
	// CHECKSTYLE:ON

	/** String representation for constants. */
	private final String symbol;

	/**
	 * Constructor witch sets the string representation.
	 * 
	 * @param newSymbol
	 *            String representation for this constant
	 */
	Color(final String newSymbol) {
	    this.symbol = newSymbol;
	}

	@Override
	public String toString() {
	    return new String(this.symbol);
	}
    }

    /** Holds all possible card names. */
    public enum Type implements Comparable<Type> {
	// CHECKSTYLE:OFF
	TWO("2"), THREE("3"), FOUR("4"), FIVE("5"), SIX("6"), SEVEN("7"),
	EIGHT("8"), NINE("9"), TEN("10"), JACK("J"), QUEEN("Q"), KING("K"),
	ACE("A");
	// CHECKSTYLE:ON

	/** String representation for constants. */
	private final String name;

	/**
	 * Constructor witch sets the string representation.
	 * 
	 * @param newName
	 *            String representation for this constant
	 */
	private Type(final String newName) {
	    this.name = newName;
	}

	@Override
	public final String toString() {
	    return this.name;
	}

	public final int compare(final Type t1, final Type t2) {
	    if (t1.ordinal() == t2.ordinal()) {
		return 0;
	    }
	    if (t1.ordinal() > t2.ordinal()) {
		return 1;
	    }
	    return -1;
	}
    }

    /** Generic card deck with all possible game cards. */
    public enum Card {
	// CHECKSTYLE:OFF
	// Diamond
	DIAMOND_TWO, DIAMOND_THREE, DIAMOND_FOUR, DIAMOND_FIVE, DIAMOND_SIX,
	DIAMOND_SEVEN, DIAMOND_EIGHT,
	DIAMOND_NINE,
	DIAMOND_TEN,
	DIAMOND_JACK,
	DIAMOND_QUEEN,
	DIAMOND_KING,
	DIAMOND_ACE,
	// Heart
	HEART_TWO, HEART_THREE, HEART_FOUR, HEART_FIVE, HEART_SIX, HEART_SEVEN,
	HEART_EIGHT, HEART_NINE, HEART_TEN,
	HEART_JACK,
	HEART_QUEEN,
	HEART_KING,
	HEART_ACE,
	// Spade
	SPADE_TWO, SPADE_THREE, SPADE_FOUR, SPADE_FIVE, SPADE_SIX, SPADE_SEVEN,
	SPADE_EIGHT, SPADE_NINE, SPADE_TEN, SPADE_JACK, SPADE_QUEEN,
	SPADE_KING,
	SPADE_ACE,
	// Club
	CLUB_TWO, CLUB_THREE, CLUB_FOUR, CLUB_FIVE, CLUB_SIX, CLUB_SEVEN,
	CLUB_EIGHT, CLUB_NINE, CLUB_TEN, CLUB_JACK, CLUB_QUEEN, CLUB_KING,
	CLUB_ACE;
	// CHECKSTYLE:ON

	@Override
	public String toString() {
	    return String.format("[%s%s]", this.getColor(), this.getType());
	}

	/**
	 * Get the Color of the card.
	 * 
	 * @return Card Color
	 */
	public Color getColor() {
	    return Color.values()[this.ordinal() / Type.values().length];
	}

	/**
	 * Get the Type of the card.
	 * 
	 * @return Card Type
	 */
	public Type getType() {
	    int name;
	    if (this.ordinal() >= Type.values().length) {
		name = this.ordinal() % Type.values().length;
	    } else {
		name = this.ordinal();
	    }
	    return Type.values()[name];
	}
    }

    /** Provides different specialized types of card-decks for different games. */
    public enum Deck {
	/** A skat playing deck with 32 cards ranging from 7-A in all colors. */
	SKAT {
	    /** Number of cards in this deck. */
	    private final List<Card> CARDS = new ArrayList<Card>(32);

	    @Override
	    public final List<Card> getCards() {
		int cardPos;
		// only do this the first time
		if (this.CARDS.size() == 0) {
		    for (Card card : Card.values()) {
			if (card.ordinal() >= Type.values().length) {
			    cardPos = card.ordinal() % Type.values().length;
			} else {
			    cardPos = card.ordinal();
			}
			// we start with card 7 witch is at ordinal 5
			if (cardPos >= 5) {
			    this.CARDS.add(card);
			}
		    }
		}
		return Collections.unmodifiableList(this.CARDS);
	    }

	    @Override
	    public final int size() {
		return 32;
	    }

	    @Override
	    public final int numberOfColors() {
		return this.colors().size();
	    }

	    @Override
	    public final int numberOfTypes() {
		return this.types().size();
	    }

	    @Override
	    public Set<Type> types() {
		return EnumSet.range(Type.SEVEN, Type.ACE);
	    }

	    @Override
	    public Set<Color> colors() {
		return EnumSet.range(Color.DIAMOND, Color.SPADE);
	    }

	    @Override
	    public List<Card> getCardsByColor(final Color color) {
		int offset = color.ordinal() * this.numberOfTypes();
		return this.CARDS
			.subList(offset, offset + this.numberOfTypes());
	    }

	    @Override
	    public List<Card> getCardsByType(final Type type) {
		List<Card> cards = new ArrayList<Card>(this.numberOfColors());
		for (Card card : this.CARDS) {
		    if (card.getType().equals(type)) {
			cards.add(card);
		    }
		}
		return cards;
	    }
	};

	/**
	 * Get all cards for a defined Deck.
	 * 
	 * @return List with all cards belonging to this Deck
	 */
	public abstract List<Card> getCards();

	/**
	 * Get the size (number of cards) of the Deck.
	 * 
	 * @return Number of cards in the Deck
	 */
	public abstract int size();

	/**
	 * Get the number of different card-Types contained in this Deck.
	 * 
	 * @return Number of different Types
	 */
	public abstract int numberOfTypes();

	/**
	 * Get the number of different card-colors contained in this Deck.
	 * 
	 * @return Number of different Colors
	 */
	public abstract int numberOfColors();

	/**
	 * Get the Colors used in this Deck.
	 * 
	 * @return All used Colors
	 */
	public abstract Set<Color> colors();

	/**
	 * Get the Types used in this Deck.
	 * 
	 * @return All used Types
	 */
	public abstract Set<Type> types();

	/**
	 * Get all Cards by a specified color.
	 * 
	 * @param color
	 *            The color the cards should have
	 * @return Cards belonging to the given color
	 */
	public abstract List<Card> getCardsByColor(Color color);

	/**
	 * Get all Cards by a specified color.
	 * 
	 * @param type
	 *            The color the cards should have
	 * @return Cards belonging to the given color
	 */
	public abstract List<Card> getCardsByType(Type type);
    }
}
