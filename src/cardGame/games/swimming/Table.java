package cardGame.games.swimming;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;

import cardGame.card.CardDeck.Card;
import cardGame.card.CardDeck.Deck;
import cardGame.event.CardGameEvent;
import cardGame.event.EventBus;
import cardGame.event.EventReceiver;
import cardGame.out.Debug;
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
    /** Number of Cards initially passed to the user. */
    private static final byte INITIAL_CARDS_AMOUNT = 3;
    /** True if the current round was closed. Prevents closing a second time. */
    private boolean roundClosed = false;
    /** Interactions stored by a player. */
    private final EnumMap<Action, Object> interactions =
	    new EnumMap<Action, Object>(Action.class);

    /**
     * Possible interactions with the table. These actions may be fired with an
     * arbitrary number of objects as associated data.
     * 
     * If no object data is documented here than it's obsolete and should be
     * passed as <code>null</code>.
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
	/** Player want's to close this round. */
	CLOSE,
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
	CARDS,
	/**
	 * Player dropped a card. <br/>
	 * Data: <code>Card</code> that was dropped
	 */
	CARD_DROP,
	/**
	 * Player picked a card. <br/>
	 * Data: <code>Card</code> that player was picked
	 */
	CARD_PICK,
	/**
	 * A player closed the round.<br/>
	 * Data: <code>Player</code> that closed.
	 */
	CLOSE_CALL;
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
     * the initial state (cards, etc.) for the table.
     * 
     * @param player
     *            Player who should receive the cards
     */
    final void dealInitialCards(final CardPlayer player) {
	// setup a new game
	this.tableCards.clear();
	this.roundClosed = false;

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

    /**
     * Check if player is allowed to interact. In general this will only be the
     * player whose current turn it is.
     */
    private boolean legitimatePlayer(final CardPlayer player) {
	if (!player.equals(this.currentPlayer)) {
	    throw new IllegalArgumentException(
		    "Player interaction currently not allowed.");
	}
	return true;
    }

    @Override
    public final void addInteraction(final CardPlayer player,
	    final Enum<? extends TableAction> action, final Object data) {
	this.legitimatePlayer(player);
	if (!action.getClass().equals(Action.class)) {
	    throw new IllegalArgumentException(String.format(
		    "Given action '%s' was not of the expected type (%s).",
		    action.getClass(), Action.class));
	}

	// store interaction
	this.interactions.put((Action) action, data);
    }

    @Override
    public final Enum<? extends TableAction> commitInteraction(
	    final CardPlayer player) {
	boolean actionDone = false;
	this.legitimatePlayer(player);

	if (this.interactions.containsKey(Action.CARD_DROP)
		&& (this.interactions.containsKey(Action.CARD_PICK))) {
	    Card pickCard = (Card) this.interactions.get(Action.CARD_PICK);
	    Card dropCard = (Card) this.interactions.get(Action.CARD_DROP);

	    // try pick
	    if (this.tableCards.remove(pickCard)) {
		EventBus.INSTANCE.fireEvent(Event.CARD_PICK, pickCard);
	    } else {
		// failed
		return Action.CARD_PICK;
	    }
	    // try drop
	    if (this.tableCards.add(dropCard)) {
		EventBus.INSTANCE.fireEvent(Event.CARD_DROP, dropCard);
	    } else {
		// failed
		return Action.CARD_DROP;
	    }

	    Debug.printfn(Debug.Level.INFO, "Table <%s> drop:%s pick:%s",
		    player, dropCard, pickCard);

	    this.interactions.remove(Action.CARD_PICK);
	    this.interactions.remove(Action.CARD_DROP);
	    // there shouldn't be any more actions
	    actionDone = true;
	}

	for (Action action : this.interactions.keySet()) {
	    if (actionDone) {
		// this action is too much
		return action;
	    }
	    switch (action) {
	    case CLOSE:
		if (!this.roundClosed) {
		    Debug.printfn(Debug.Level.INFO,
			    "Table <%s> Closed the round!", player);
		    this.roundClosed = true;
		    EventBus.INSTANCE.fireEvent(Event.CLOSE_CALL, player);
		    actionDone = true;
		}
		break;
	    default:
		return action;
	    }
	}
	return null;
    }

    @Override
    public final void handleEvent(final CardGameEvent event, final Object data) {
	Class<? extends CardGameEvent> eventClass = event.getClass();
	if (eventClass.equals(GameLogic.Event.class)) {
	    switch ((GameLogic.Event) event) {
	    case NEXTPLAYER:
		this.currentPlayer = (CardPlayer) data;
		this.interactions.clear();
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
