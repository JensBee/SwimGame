package cardGame.table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cardGame.card.CardDeck;
import cardGame.card.CardStack;
import cardGame.event.CardGameEvent;
import cardGame.event.EventBus;
import cardGame.player.CardPlayer;
import cardGame.util.LoopIterator;

/**
 * A game table where a card game is happening. This is a generic implementation
 * of all shared functions.
 */
public abstract class GeneralGameTable implements GameTable {
    // CHECKSTYLE:OFF
    /** Minimum number of players needed. */
    protected int playersMin = 2;
    /** Maximum number of players allowed. */
    protected int playersMax = 2;
    // CHECKSTYLE:ON

    /** {@link CardStack} with all cards available on this table. */
    private final CardStack tableCards;

    /** Events emitted by the table. */
    // TODO: describe parameters
    public enum Event implements CardGameEvent {
	/** Table is closed. No new players may attend. */
	CLOSED,
	/** A player joined the table. */
	PLAYER_JOINED,
	/** A player left the table. */
	PLAYER_LEFT;
    }

    /** List of players playing on this table. */
    private List<CardPlayer> player =
	    new ArrayList<CardPlayer>(this.playersMax);

    /**
     * Constructor.
     * 
     * @param deck
     *            The {@link CardDeck} used by the game on this table
     */
    protected GeneralGameTable(final CardDeck.Deck deck) {
	this.tableCards = new CardStack(deck);
	this.tableCards.full();
    }

    @Override
    public final void addPlayer(final CardPlayer newPlayer) throws Exception {
	if (this.player.size() >= this.playersMax) {
	    throw new TableException(
		    TableException.TableExceptions.TABLE_CLOSED_PLAYER_REJECTED);
	}
	this.player.add(newPlayer);
	newPlayer.setTable(this);
	EventBus.INSTANCE.fireEvent(Event.PLAYER_JOINED, newPlayer);
    }

    @Override
    public final void removePlayer(final CardPlayer playerToRemove)
	    throws Exception {
	if (!this.player.remove(playerToRemove)) {
	    throw new IllegalArgumentException("Player not found.");
	}
	EventBus.INSTANCE.fireEvent(Event.PLAYER_LEFT, playerToRemove);
    }

    @Override
    public final void close() {
	// fixate the players list
	this.player = Collections.unmodifiableList(this.player);
	EventBus.INSTANCE.fireEvent(GeneralGameTable.Event.CLOSED, null);
    }

    @Override
    public final List<CardPlayer> player() {
	return Collections.unmodifiableList(this.player);
    }

    @Override
    public final LoopIterator<CardPlayer> playerLoopIterator() {
	return new LoopIterator<CardPlayer>(this.player);
    }

    @Override
    public final CardDeck.Card dealCard() {
	return this.tableCards.getRandomCard();
    }

    @Override
    public final int numberOfPlayers() {
	return this.player.size();
    }

    @Override
    public final void startNewGame() {
	// re-fill card stack
	this.tableCards.full();
    }
}
