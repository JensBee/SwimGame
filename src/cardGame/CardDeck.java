package cardGame;

import java.util.ArrayList;
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
	 * Get the {@link Color} of the card.
	 * 
	 * @return Card {@link Color}
	 */
	public Color getColor() {
	    return Color.values()[this.ordinal() / Type.values().length];
	}

	/**
	 * Get the {@link Type} of the card.
	 * 
	 * @return Card {@link Type}
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
	/** A skat deck with 32 cards ranging from 7-A in all colors. */
	SKAT {
	    /** Number of cards in this deck. */
	    private final List<Card> CARDS = new ArrayList<Card>(32);

	    @Override
	    public final List<Card> getCards() {
		int cardPos;
		for (Card card : Card.values()) {
		    if (card.ordinal() >= Type.values().length) {
			cardPos = card.ordinal() % Type.values().length;
		    } else {
			cardPos = card.ordinal();
		    }
		    if (cardPos >= 5) {
			this.CARDS.add(card);
		    }
		}
		return this.CARDS;
	    }

	    @Override
	    public final int size() {
		return this.CARDS.size();
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
	};

	/**
	 * Get all cards for a defined {@link Deck}.
	 * 
	 * @return {@link List} with all cards belonging to this {@link Deck}
	 */
	public abstract List<Card> getCards();

	/**
	 * Get the size (number of cards) of the {@link Deck}.
	 * 
	 * @return Number of cards in the {@link Deck}
	 */
	public abstract int size();

	/**
	 * Get the number of different card-{@link Type}s contained in this
	 * {@link Deck}.
	 * 
	 * @return Number of different {@link Type}s
	 */
	public abstract int numberOfTypes();

	/**
	 * Get the number of different card-{@link Color}s contained in this
	 * {@link Deck}.
	 * 
	 * @return Number of different {@link Color}s
	 */
	public abstract int numberOfColors();

	/**
	 * Get the {@link Color}s used in this {@link Deck}.
	 * 
	 * @return {@link Set} with all used {@link Color}s
	 */
	public abstract Set<Color> colors();

	/**
	 * Get the {@link Type}s used in this {@link Deck}.
	 * 
	 * @return {@link Set} with all used {@link Type}s
	 */
	public abstract Set<Type> types();
    }
}
