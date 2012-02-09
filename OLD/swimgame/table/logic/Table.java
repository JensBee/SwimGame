package swimgame.table.logic;

import swimgame.player.DefaultPlayer;
import swimgame.table.CardStack;
import swimgame.table.logic.TableLogic.Action;
import swimgame.table.logic.TableLogic.Event;
import cardGame.card.CardDeck.Card;
import cardGame.player.CardPlayer;
import cardGame.table.GameTable;

/**
 * The game table.
 * 
 * @author <a href="mailto:code@jens-bertram.net">Jens Bertram</a>
 * 
 */
public class Table implements GameTable {
    /** Table will be closed, if full or the game has begun. */
    private boolean closed = false;
    /**
     * {@link CardStack} owned by this {@link Table} instance (stack will be
     * full, i.e. has all cards).
     */
    private CardStack cardStack = new CardStack(true);
    /** {@link CardStack} with cards currently on the table. */
    private CardStack cardStackTable = new CardStack(false);

    // these are intended to be accessible
    // CHECKSTYLE:OFF
    /** {@link TableLogic} controlling this table. */
    TableLogic logic;
    /** The {@link Player} management object. */
    Player player;
    /** The {@link Game} handling object. */
    Game game;

    // CHECKSTYLE:ON

    /**
     * Default constructor.
     * 
     * @param newTableLogic
     *            {@link TableLogic} controlling this {@link Table} instance
     */
    Table(final TableLogic newTableLogic) {
	this.logic = newTableLogic;
	this.player = new Player();
	this.game = new Game(this);
    }

    /**
     * Constructor.
     * 
     * @param newTableLogic
     *            {@link TableLogic} controlling this {@link Table} instance
     * @param gamesToPlay
     *            How many games to play
     */
    Table(final TableLogic newTableLogic, final int gamesToPlay) {
	this.logic = newTableLogic;
	this.player = new Player();
	this.game = new Game(this, gamesToPlay);
    }

    /**
     * Constructor.
     * 
     * @param newTableLogic
     *            {@link TableLogic} controlling this {@link Table} instance
     * @param maxRoundsPerGame
     *            How many rounds are to play in maximum. Defaults to
     *            {@value TableLogic#DEFAULT_MAX_ROUNDS}
     * @param gamesToPlay
     *            How many games to play
     */
    Table(final TableLogic newTableLogic, final int maxRoundsPerGame,
	    final int gamesToPlay) {
	this.logic = newTableLogic;
	this.player = new Player();
	this.game = new Game(this, gamesToPlay, maxRoundsPerGame);
    }

    /**
     * Get the {@link Game} associated with this {@link Table} instance.
     * 
     * @return The {@link Game} object associated with this {@link Table}
     *         instance
     * @see Game
     */
    public final Game getGame() {
	return this.game;
    }

    /**
     * Get the {@link Player} instance associated with this {@link Table}
     * instance.
     * 
     * @return The {@link Player} instance associated with this {@link Table}
     *         instance
     */
    public final Player getPlayer() {
	return this.player;
    }

    /**
     * Close player participation opportunity and prepare game start. If the
     * table is closed no more players are allowed to join the game.
     */
    final void close() {
	if (!this.closed) {
	    this.closed = true;
	}
    }

    /**
     * Reset the {@link CardStack} instances (table and stack) of this
     * {@link Table} instance. This should be called, if a new game should be
     * started.
     */
    final void resetCards() {
	this.cardStackTable = new CardStack(false);
	this.cardStack = new CardStack(true);
    }

    /**
     * Returns a new {@link CardStack} with the cards currently available on
     * this table.
     * 
     * @return A new {@link CardStack} containing the cards currently on the
     *         table
     */
    public final CardStack getCardStack() {
	return new CardStack(this.cardStackTable.getCards());
    }

    /**
     * Generate a random card set for a player. The amount of cards returned is
     * specified by {@link TableLogic#INITIAL_CARDS}.
     * 
     * @return The randomly generated card set
     */
    private Card[] getPlayerCardSet() {
	return new Card[] { this.cardStack.getRandomCard(),
		this.cardStack.getRandomCard(), this.cardStack.getRandomCard() };
    }

    /**
     * Deliver cards to players.
     * 
     * @param beginningPlayer
     *            The player receiving the initial stack of cards
     */
    protected final void dealOutCards(final CardPlayer beginningPlayer) {
	Card[] initialCardSet = new Card[TableLogic.INITIAL_CARDS];

	for (CardPlayer currentPlayer : this.player.asList()) {
	    if (currentPlayer.equals(beginningPlayer)) {
		initialCardSet = this.getPlayerCardSet();
		currentPlayer.setCards(initialCardSet);
	    } else {
		// pass initial cards to player
		currentPlayer.setCards(this.getPlayerCardSet());
	    }
	}

	// deal out a second set of cards for the first player, if he want
	// so
	if (!beginningPlayer.keepCardSet()) {
	    // update cards on table
	    this.player.fireEvent(Event.INITIAL_CARDSTACK_DROPPED,
		    initialCardSet);
	    this.logic.proxyInteractionEvent(Action.DROP_CARDSTACK_INITIAL,
		    initialCardSet);
	    this.cardStackTable.addCard(initialCardSet);
	    beginningPlayer.setCards(this.getPlayerCardSet());
	} else {
	    // player took first cards - make a second public
	    Card[] cards = this.getPlayerCardSet();
	    this.logic.proxyInteractionEvent(Action.INITIAL_CARDSTACK_PICKED,
		    null);
	    // update cards on table
	    this.cardStackTable.addCard(cards);
	}
    }

    /**
     * Check if table is closed.
     * 
     * @return True if closed
     * @see Table#close()
     */
    public final boolean isClosed() {
	return this.closed;
    }

    /**
     * Add a single player to the table. This is possible up to a maximum of
     * {@value TableLogic#MAX_PLAYER}
     * 
     * @param newPlayer
     *            The {@link CardPlayer} to add
     * @throws TableLogicException
     *             Thrown if table is full or game has already begun
     */
    @Override
    public final void addPlayer(final CardPlayer newPlayer)
	    throws TableLogicException {
	if ((this.isClosed())
		|| (this.player.amount() == TableLogic.MAX_PLAYER)) {
	    throw new TableLogicException(
		    TableLogicException.Exception.TABLE_CLOSED_PLAYER_REJECTED);
	}

	this.player.add(newPlayer);
    }

    /**
     * Batch add a bunch of {@link CardPlayer} to the table. This is possible up to
     * a maximum defined by {@link TableLogic#MAX_PLAYER}
     * 
     * @param amount
     *            The number of players to add (max 9)
     * @throws TableLogicException
     *             If the adding of a player has failed
     */
    public final void addPlayers(final int amount) throws TableLogicException {
	for (int i = 0; i < amount; i++) {
	    this.addPlayer(new DefaultPlayer(this.logic));
	}
    }
}
