package cardGame.games.swimming;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import cardGame.card.CardDeck.Card;
import cardGame.card.CardDeck.Deck;
import cardGame.event.CardGameEvent;
import cardGame.event.EventBus;
import cardGame.event.EventReceiver;
import cardGame.player.CardPlayer;
import cardGame.table.GeneralGameTable;
import cardGame.table.TableAction;

/**
 * <code>GameTable</code> implementation for playing the swimming game.
 * 
 * @author <a href="mailto:code@jens-bertram.net">Jens Bertram</a>
 * 
 */
public class Table extends GeneralGameTable implements EventReceiver {
    /** Cards currently on the table. */
    private final EnumSet<Card> tableCards = EnumSet.noneOf(Card.class);
    /** Reference to the current playing player. */
    private CardPlayer currentPlayer;
    /** Card picked by current player. */
    private Card playerPickCard;
    /** Card dropped by current player. */
    private Card playerDropCard;
    /** Number of Cards initially passed to the user. */
    private static final byte INITIAL_CARDS_AMOUNT = 3;

    /**
     * Possible interactions with the table. These actions may be fired with an
     * arbitrary number of objects as associated data.
     */
    public enum Action implements TableAction {
	/**
	 * Player wants to drop a card. <br/>
	 * Data: <code>Card</code> that player wants to drop
	 */
	CARD_DROP,
	/**
	 * Player wants to pick a card. <br/>
	 * Data: <code>Card</code> that player wants to pick
	 */
	CARD_PICK,
	/** Player finished his move. */
	FINISHED;
    }

    /** Events emitted by this table. */
    public enum Event implements CardGameEvent {
	/**
	 * Current cards on the table.<br/>
	 * Data: <code>Collection&lt;Card&gt;</code> with the cards currently
	 * available
	 */
	CARDS;
    }

    /**
     * @param deck
     *            The type of cards played on this table
     */
    Table(final Deck deck) {
	super(deck);
	EventBus.INSTANCE.registerEventReceiver(this,
		GameLogic.Event.NEXTPLAYER);
	// CHECKSTYLE:OFF
	this.playersMin = 2;
	this.playersMax = 9;
	// CHECKSTYLE:ON
    }

    /**
     * Deal out cards for a player. Use this only, if the player is not the one
     * who starts the game. In this case use
     * {@link Table#dealInitialCards(CardPlayer)}.
     * 
     * @param player
     *            Player who should receive the cards
     */
    final void dealCards(final CardPlayer player) {
	List<Card> cards = new ArrayList<Card>(INITIAL_CARDS_AMOUNT);
	for (int i = 0; i < INITIAL_CARDS_AMOUNT; i++) {
	    cards.add(this.dealCard());
	}
	player.setCards(cards);
    }

    /**
     * Set an initial set of cards for the beginning player. This will also set
     * the initial cards for the table.
     * 
     * @param player
     *            Player who should receive the cards
     */
    final void dealInitialCards(final CardPlayer player) {
	this.tableCards.clear();
	List<Card> cards = new ArrayList<Card>(INITIAL_CARDS_AMOUNT);
	for (int i = 0; i < INITIAL_CARDS_AMOUNT; i++) {
	    cards.add(this.dealCard());
	}
	if (!player.setCards(cards)) {
	    // ..he rejected the first set, so he must accept the second
	    // set.
	    // The first set remains on the table..
	    this.tableCards.addAll(cards);
	    // .. the new goes to the player
	    cards.clear();
	    for (int i = 0; i < INITIAL_CARDS_AMOUNT; i++) {
		cards.add(this.dealCard());
	    }
	    player.setCards(cards);
	} else {
	    // .. he accepted the first set, so get a second one for the
	    // table.
	    cards.clear();
	    for (int i = 0; i < INITIAL_CARDS_AMOUNT; i++) {
		cards.add(this.dealCard());
	    }
	    this.tableCards.addAll(cards);
	}
	EventBus.INSTANCE.fireEvent(Event.CARDS,
		Collections.unmodifiableSet(this.tableCards));
    }

    @Override
    public final Object interact(final CardPlayer player,
	    final Enum<? extends TableAction> action, final Object data) {
	if (!action.getClass().equals(Action.class)) {
	    throw new IllegalArgumentException(String.format(
		    "Given action '%s' was not of the expected type (%s).",
		    action.getClass(), Action.class));
	}

	// Check if player is allowed to interact. In general this will only be
	// the player whose current turn it is.
	if (!player.equals(this.currentPlayer)) {
	    throw new IllegalArgumentException(String.format(
		    "Player interaction currently not allowed.",
		    action.getClass(), Action.class));
	}

	switch ((Action) action) {
	case CARD_DROP:
	    if (this.tableCards.contains(data)) {
		return false;
	    }
	    this.playerDropCard = (Card) data;
	    return true;
	case CARD_PICK:
	    if (this.tableCards.contains(data)) {
		this.playerPickCard = (Card) data;
		return true;
	    }
	    return false;
	case FINISHED:
	    if ((this.playerDropCard != null) && (this.playerPickCard != null)) {
		return true;
	    }
	    return false;
	default:
	    break;
	}
	return false;
    }

    @Override
    public final void handleEvent(final CardGameEvent event, final Object data) {
	Class<? extends CardGameEvent> eventClass = event.getClass();
	if (eventClass.equals(GameLogic.Event.class)) {
	    switch ((GameLogic.Event) event) {
	    case NEXTPLAYER:
		this.currentPlayer = (CardPlayer) data;
		this.playerPickCard = null;
		this.playerDropCard = null;
		break;
	    default:
		break;
	    }
	}
    }

    /**
     * Get the bare rating of a card according to the rules of the game.
     * 
     * @param card
     *            Card to rate
     * @return Value of the card
     */
    public static int getCardValue(final Card card) {
	int cardValue = card.getType().ordinal();
	// CHECKSTYLE:OFF
	if (cardValue < 5) {
	    // cards with ordinal<5 should't occur, as they are restricted by
	    // the CardDeck
	    return 0;
	}
	if (cardValue < 8) {
	    // card ordinal counting begin at 2, so add this as offset
	    // ordinal(5)=7, ordinal(6)=8, ordinal(7)=9
	    return cardValue + 2;
	} else if (cardValue < 12) {
	    // ordinal(8)=10, ordinal(9)=J, ordinal(10)=Q, ordinal(11)=K
	    return 10;
	} else if (cardValue == 12) {
	    // there shouldn't be any higher card
	    return 11;
	}
	// fall through
	return 0;
	// CHECKSTYLE:ON
    }

    /**
     * Get the cards currently on the table.
     * 
     * @return Cards currently on the table.
     */
    public final Collection<Card> currentCards() {
	return this.tableCards;
    }
}
