package swimgame.table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import swimgame.out.Debug;
import swimgame.player.DefaultPlayer;
import swimgame.player.IPlayer;

/**
 * Self contained class that handles all the logic needed to get the game
 * running.
 * 
 * @author Jens Bertram <code@jens-bertram.net>
 * 
 */
public class TableLogic {
    // nested classes - these should be public accessible
    public final TableLogic.Game game;
    public final TableLogic.Player player;
    public final TableLogic.Table table;
    // maximum number of allowed players
    public static final int MAX_PLAYER = 9;
    // how many cards needed by type to be in goal state?
    public static final int RULE_GOAL_CARDS_BY_TYPE = 3;
    // instance controlling this logic
    private final ITable tableController;

    /** Events fired to the players wile in-game */
    public enum Event {
	TABLE_CLOSED,
	// Table is closed, game will start shortly
	GAME_START,
	// Game is starting now
	INITIAL_CARDSTACK_DROPPED,
	// A player dropped his initial card set, to get another one
	// Data: int[6] array with three cards 3*(card,color)
	GAME_CLOSED,
	// A player called for closing the game. The last round is running now
	GAME_FINISHED,
	// Game has finished
	CARD_DROPPED,
	// A card has been dropped by a user
	// Data: (byte) card
	;
    }

    /** Player actions to interact with the table */
    public enum Action {
	DROP_CARD,
	// Player drops a card to the table
	// Data: (byte) card
	DROP_CARDSTACK_INITIAL,
	// Player drops his initial card set, to get another one
	MOVE_FINISHED,
	// Player has finished his move
	END_CALL,
	// Player called to end this round
	PICK_CARD,
	// Player wants to pick a card from the table
	// Data: (byte) card
	INITIAL_CARDSTACK_PICKED,
	// A player has picked the initial card stack
    }

    public TableLogic(final ITable tableController) {
	this.tableController = tableController;
	this.table = new TableLogic.Table();
	this.player = new TableLogic.Player();
	this.game = new TableLogic.Game();
    }

    public TableLogic(final ITable tableController, final int gamesToPlay) {
	this.tableController = tableController;
	this.player = new TableLogic.Player();
	this.game = new TableLogic.Game(gamesToPlay);
	this.table = new TableLogic.Table();
    }

    public TableLogic(final ITable tableController, final int maxRoundsPerGame,
	    int gamesToPlay) {
	this.tableController = tableController;
	this.player = new TableLogic.Player();
	this.game = new TableLogic.Game(gamesToPlay, maxRoundsPerGame);
	this.table = new TableLogic.Table();
    }

    /**
     * Handles player on this table
     * 
     * @author Jens Bertram <code@jens-bertram.net>
     * 
     */
    public class Player {
	// List of players joined this table. LinkedHashMap to ensure element
	// ordering
	private final Map<IPlayer, Double> list = new LinkedHashMap<IPlayer, Double>();
	// has current player performed an action before continuing?
	private boolean tookAction = false;
	// Tracks if a player has finished his current move
	private boolean finishedTurn = false;

	/** Comparator for player HashMap */
	private class PlayerComparator implements Comparator<IPlayer> {
	    @Override
	    public int compare(IPlayer o1, IPlayer o2) {
		Double p1 = TableLogic.Player.this.list.get(o1);
		Double p2 = TableLogic.Player.this.list.get(o1);
		if (p1 < p2) {
		    return -1;
		}
		if (p1.equals(p2)) {
		    return 0;
		}
		return 1;
	    }
	}

	// allows comparison of players by their overall points
	private final PlayerComparator playerComparator = new PlayerComparator();

	/** Has player already taken an action while in turn? */
	public boolean getTookAction() {
	    return this.tookAction;
	}

	/** Set flag that player took an action on his turn */
	private void setTookAction(boolean state) {
	    this.tookAction = state;
	}

	/** Get the current players turn state */
	public boolean isTurnFinished() {
	    return this.finishedTurn;
	}

	/** Get the list of players */
	public List<IPlayer> getList() {
	    return new ArrayList<IPlayer>(this.list.keySet());
	}

	/** Add a player to the list */
	public void add(IPlayer player) {
	    this.list.put(player, new Double(0));
	}

	/** Get the current player index */
	public IPlayer get(int index) {
	    if ((index > -1) && (index <= this.list.size())) {
		return (IPlayer) this.getList().toArray()[0];
	    }
	    return null;
	}

	/** Get a new iterator */
	public PlayerIterator iterator(boolean wrapAround) {
	    return new PlayerIterator(wrapAround);
	}

	/** Add points to the all-games counter */
	public void addPoints(IPlayer player, Double pointsToAdd) {
	    final Double currentPoints = this.getPoints(player);
	    this.list.put(player, currentPoints + pointsToAdd);
	}

	/** Get the all-games points for a player */
	public double getPoints(IPlayer player) {
	    return this.list.get(player);
	}

	/** Get the list of players ranked by their overall game-points */
	public Map<IPlayer, Double> getRanked() {
	    final List<IPlayer> keys = new ArrayList<IPlayer>(this.list.size());
	    keys.addAll(this.list.keySet());
	    Collections.sort(keys, this.playerComparator);
	    final Map<IPlayer, Double> rankedMap = new HashMap<IPlayer, Double>();
	    for (IPlayer player : keys) {
		rankedMap.put(player, this.list.get(player));
	    }
	    return rankedMap;
	}

	/** Amount of players on this table */
	public int amount() {
	    return this.list.size();
	}

	/**
	 * Fire an event to all registered players.
	 * 
	 * @param event
	 *            The event
	 * @param data
	 *            Some sort of data associated with an event
	 */
	private void fireEvent(Event event, Object data) {
	    for (IPlayer player : this.list.keySet()) {
		player.handleTableEvent(event, data);
	    }
	}

	/** @see TableLogic.Player#fireEvent(Event, Object) */
	private void fireEvent(Event event) {
	    this.fireEvent(event, null);
	}

	/**
	 * Manage players in game-rounds
	 * 
	 * @author Jens Bertram <code@jens-bertram.neinitializet>
	 * 
	 */
	private class PlayerIterator implements Iterator<IPlayer> {
	    // pointer to current player
	    private byte pointer = -1;
	    // pointer to the player who closed the current round
	    private byte closingPointer = -1;
	    // restart iteration from the beginning, if we hit the end?
	    private boolean wrapAround = false;
	    // has last next() call wrapped the pointer?
	    private boolean hasWrapped = false;
	    // get the IPlayer objects as array for easy index handling
	    private List<IPlayer> player;
	    // track, if we have already build the player array
	    private boolean initialized = false;

	    /**
	     * @param wrapAround
	     *            If true, the iterator will restart at the beginning,
	     *            if hitting the end of the player list
	     */
	    protected PlayerIterator(boolean wrapAround) {
		this.wrapAround = wrapAround;
	    }

	    /**
	     * Initialize the iterator. This must be called before actually
	     * using it
	     */
	    private void initialize() {
		this.player = Collections
			.unmodifiableList(TableLogic.this.player.getList());
		this.initialized = true;
	    }

	    final @Override
	    public boolean hasNext() {
		if (!this.initialized) {
		    this.initialize();
		}

		if (this.wrapAround) {
		    // this one is never ending
		    return true;
		}

		if ((this.pointer + 1) < this.player.size()) {
		    if ((this.pointer + 1) == this.closingPointer) {
			return false;
		    }
		    return true;
		}
		return false;
	    }

	    private void setPointer(int pointer) {
		this.pointer = (byte) pointer;
	    }

	    @Override
	    public IPlayer next() {
		if (!this.initialized) {
		    this.initialize();
		}

		if (this.hasNext()) {
		    this.pointer++;

		    if (this.pointer == this.player.size()) {
			this.hasWrapped = true;
			this.reset();
			this.pointer++;
		    } else {
			this.hasWrapped = false;
		    }

		    IPlayer player = this.get();
		    TableLogic.this.player.finishedTurn = false;
		    return player;
		}
		return null;
	    }

	    /** Resets the iterator to the beginning of the player-list */
	    private void reset() {
		this.pointer = -1;
	    }

	    @Override
	    public void remove() {
		// not implemented
	    }

	    /** Index of the current player in the player list */
	    private byte getIndex() {
		if (!this.initialized) {
		    this.initialize();
		}
		return (this.pointer == -1) ? 0 : this.pointer;
	    }

	    /** Get the player the iterator is currently pointing at */
	    private IPlayer get() {
		return this.player.get(this.getIndex());
	    }

	    /**
	     * Set the current player as the one who closed the game.
	     * 
	     * @return Any second try to close will be signaled by returning
	     *         false;
	     */
	    private boolean setAsClosing() {
		if (this.closingPointer == -1) {
		    this.closingPointer = this.getIndex();
		    return true;
		}
		return false;
	    }

	    /**
	     * @return True, if while getting the current player the iterator
	     *         has wrapped around
	     */
	    private boolean hasWrapped() {
		return this.hasWrapped;
	    }
	}
    }

    /**
     * Games handling
     * 
     * @author Jens Bertram <code@jens-bertram.net>
     * 
     */
    public class Game implements Iterator<IPlayer> {
	// is game finished?
	private boolean finished = false;
	// the number of games to play in turn
	private int numberOfGamesToPlay = 1;
	// the current game number
	private int currentGameNumber = 0;
	private final TableLogic.Player.PlayerIterator players = TableLogic.this.player
		.iterator(true);
	private IPlayer startingPlayer;
	public TableLogic.Game.Round round;

	/** Empty constructor */
	private Game() {
	    this.round = new Round();
	}

	/**
	 * Constructor
	 * 
	 * @param numberOfGames
	 *            Number of games to play
	 */
	private Game(int numberOfGames) {
	    this.numberOfGamesToPlay = numberOfGames;
	    this.round = new Round();
	}

	/**
	 * Constructor
	 * 
	 * @param numberOfGames
	 *            Number of games to play
	 * @param maxRoundsToPlay
	 *            Maximum number of rounds to play per game
	 */
	private Game(int numberOfGames, int maxRoundsToPlay) {
	    this.numberOfGamesToPlay = numberOfGames;
	    this.round = new Round(maxRoundsToPlay);
	}

	/** Check if there's a next game we want to play */
	@Override
	public boolean hasNext() {
	    if ((this.currentGameNumber + 1) <= this.numberOfGamesToPlay) {
		return true;
	    }
	    TableLogic.this.player.fireEvent(TableLogic.Event.GAME_FINISHED);
	    this.finished = true;
	    return false;
	}

	/**
	 * Prepare the next game. This should handle all needed steps to start a
	 * new round.
	 */
	@Override
	public IPlayer next() {
	    this.currentGameNumber++;
	    if (this.currentGameNumber > this.numberOfGamesToPlay) {
		TableLogic.this.player
			.fireEvent(TableLogic.Event.GAME_FINISHED);
		this.finished = true;
	    }
	    this.round.reset();
	    this.startingPlayer = this.players.next();
	    this.round.setCurrentPlayer(this.startingPlayer);
	    TableLogic.this.game.round.setCurrentPlayer(this.startingPlayer);
	    TableLogic.this.table.resetCards();
	    TableLogic.this.table.dealOutCards(this.startingPlayer);
	    return this.startingPlayer;
	}

	/** Test, if the current game is finished */
	public boolean isFinished() {
	    return this.finished;
	}

	/** Get the current game number that is played */
	public int current() {
	    return this.currentGameNumber;
	}

	@Override
	public void remove() {
	    // not implemented
	}

	/** Set the number of games to play */
	public void setNumberOfGamesToPlay(int numberOfGamesToPlay) {
	    this.numberOfGamesToPlay = numberOfGamesToPlay;
	}

	/** Get the number of games to play */
	public int getNumberOfGamesToPlay() {
	    return this.numberOfGamesToPlay;
	}

	/**
	 * Rounds of a game being played
	 * 
	 * @author @author Jens Bertram <code@jens-bertram.net>
	 * 
	 */
	public class Round implements Iterator<Integer> {
	    private byte currentRound = -1;
	    private int maxNumberOfRounds = 32;
	    private boolean finished = false;
	    private final TableLogic.Player.PlayerIterator players;
	    private IPlayer currentPlayer;
	    private IPlayer closingPlayer;

	    /** Empty constructor */
	    protected Round() {
		this.players = TableLogic.this.player.iterator(true);
	    }

	    /**
	     * Constructor
	     * 
	     * @param maxNumberOfRounds
	     *            Maximum number of rounds to play
	     */
	    protected Round(int maxNumberOfRounds) {
		this.players = TableLogic.this.player.iterator(true);
		this.maxNumberOfRounds = maxNumberOfRounds;
	    }

	    /** Get the number of maximum rounds to play per game */
	    public int getMaxLength() {
		return this.maxNumberOfRounds;
	    }

	    /** Set the number of maximum rounds to play per game */
	    protected void setMaxLength(int maxRoundsToPlay) {
		this.maxNumberOfRounds = maxRoundsToPlay;
	    }

	    /** Set the current active player */
	    protected void setCurrentPlayer(IPlayer playerToSet) {
		byte i = 0;
		for (IPlayer player : TableLogic.this.player.getList()) {
		    if (player.equals(playerToSet)) {
			this.players.setPointer(i);
			this.currentPlayer = playerToSet;
			break;
		    }
		    i++;
		}
	    }

	    /**
	     * Initialize the round managing. This must be called, after all
	     * players have joined the table.
	     */
	    protected void initialize() {
		this.currentRound = -1;
		this.finished = false;
		this.currentPlayer = this.players.next();
	    }

	    /**
	     * Start the next round
	     * 
	     * @return The player who is starting this new round
	     */
	    public IPlayer nextPlayer() {
		this.currentPlayer = this.players.next();
		TableLogic.this.player.setTookAction(false);
		if (this.players.hasWrapped()) {
		    // FIXME: really next on wrap?
		    this.next();
		}
		return this.currentPlayer;
	    }

	    @Override
	    public Integer next() {
		this.currentRound++;
		if (this.currentRound == this.maxNumberOfRounds) {
		    this.finished = true;
		}
		return (int) this.currentRound;
	    }

	    /** Is there a next round left to play? */
	    @Override
	    public boolean hasNext() {
		if ((this.currentRound + 1) <= this.maxNumberOfRounds) {
		    return true;
		}
		return false;
	    }

	    /** Reset the round counter. Useful if a new game has begun */
	    protected void reset() {
		this.closingPlayer = null;
		this.currentPlayer = null;
		this.currentRound = 0;
		this.finished = false;
	    }

	    /** Has this round finished? */
	    public boolean isFinished() {
		return this.finished;
	    }

	    /** Current round number */
	    public int getCurrent() {
		return this.currentRound;
	    }

	    /** Current player will close this round */
	    public boolean close() {
		if (this.players.setAsClosing()) {
		    this.closingPlayer = this.players.get();
		    return true;
		}
		return false;
	    }

	    /** Test, if a given player has closed the round */
	    public boolean isClosedBy(IPlayer player) {
		return player.equals(this.closingPlayer);
	    }

	    /** Get the current player of this round */
	    public IPlayer getCurrentPlayer() {
		return this.currentPlayer;
	    }

	    @Override
	    public void remove() {
		// not implemented
	    }
	}
    }

    private static class Cards {
	/** Verifies the goal state of a given list of cards */
	protected static boolean verifyGoal(byte[] cards) {
	    CardStack userCardStack;
	    byte cardsCount;

	    try {
		userCardStack = new CardStack(cards);
	    } catch (Exception e) {
		return false;
	    }

	    // three of a color?
	    cardsCount = 0;
	    for (byte card : userCardStack.getCardsByColor(userCardStack.card
		    .getColor(cards[0]))) {
		if (userCardStack.hasCard(card)) {
		    cardsCount++;
		}
	    }
	    if (cardsCount == 3) {
		Debug.println(Table.class, "Three of a color!");
		return true;
	    }

	    // three of a type?
	    cardsCount = 0;
	    for (byte card : userCardStack.getCardsByType(userCardStack.card
		    .getType(cards[0]))) {
		if (userCardStack.hasCard(card)) {
		    cardsCount++;
		}
	    }
	    if (cardsCount == 3) {
		Debug.println(Table.class, "Three of a type!");
		return true;
	    }

	    return false;
	}
    }

    public class Table {
	// Table will be closed, if full or the game has begun
	private boolean closed = false;
	// Card stack owned by this table (stack will be full, i.e. has all
	// cards)
	private CardStack cardStack = new CardStack(true);
	// cards on the table
	private CardStack cardStackTable = new CardStack(false);

	/**
	 * Close player participation opportunity and prepare game start.
	 */
	protected void close() {
	    if (!this.closed) {
		TableLogic.this.player.fireEvent(Event.TABLE_CLOSED, null);
		this.closed = true;

		if (Debug.debug) {
		    Debug.println(this.getClass(), String.format(
			    "Players: %d  Maximum rounds: %d",
			    TableLogic.this.player.amount(),
			    TableLogic.this.game.round.getMaxLength()));
		}
	    }
	}

	/**
	 * Renew the card stack of this table. This should be called, if a new
	 * game should be started.
	 */
	protected void resetCards() {
	    this.cardStackTable = new CardStack(false);
	    this.cardStack = new CardStack(true);
	}

	/** Returns a CardStack with the cards currently on the table */
	public CardStack getCardStack() {
	    return new CardStack(this.cardStackTable.getCards());
	}

	/**
	 * Generate a random card set for a player
	 * 
	 * @return The randomly generated card set
	 * @throws Exception
	 */
	protected byte[] getPlayerCardSet() {
	    return new byte[] { this.cardStack.card.getRandom(),
		    this.cardStack.card.getRandom(),
		    this.cardStack.card.getRandom() };
	}

	/**
	 * Deliver cards to players
	 */
	protected void dealOutCards(IPlayer beginningPlayer) {
	    byte[] initialCardSet = new byte[3];

	    for (IPlayer player : TableLogic.this.player.getList()) {
		if (player.equals(beginningPlayer)) {
		    initialCardSet = this.getPlayerCardSet();
		    player.setCards(initialCardSet);
		} else {
		    // pass initial cards to player
		    player.setCards(this.getPlayerCardSet());
		}
	    }

	    // deal out a second set of cards for the first player, if he want
	    // so
	    if (beginningPlayer.keepCardSet() == false) {
		// update cards on table
		TableLogic.this.player.fireEvent(
			Event.INITIAL_CARDSTACK_DROPPED, initialCardSet);
		TableLogic.this.proxyInteractionEvent(
			Action.DROP_CARDSTACK_INITIAL, initialCardSet);
		this.cardStackTable.card.add(initialCardSet);
		beginningPlayer.setCards(this.getPlayerCardSet());
	    } else {
		// player took first cards - make a second public
		byte cards[] = this.getPlayerCardSet();
		TableLogic.this.proxyInteractionEvent(
			Action.INITIAL_CARDSTACK_PICKED, null);
		// update cards on table
		this.cardStackTable.card.add(cards);
	    }
	}

	/**
	 * Check if table is closed (no more players are able to join)
	 * 
	 * @return True if closed
	 */
	public boolean isClosed() {
	    return this.closed;
	}

	/**
	 * Add a single player to the table
	 * 
	 * @param player
	 * @throws Exception
	 *             Thrown if table is full or game has already begun
	 */
	public void addPlayer(final IPlayer player) throws Exception {
	    if ((TableLogic.this.table.isClosed())
		    || (TableLogic.this.player.amount() == MAX_PLAYER)) {
		TableLogic.this.table.close(); // just to be sure
		throw new Exception("Table is closed! No more player allowed.");
	    }

	    TableLogic.this.player.add(player);
	}

	/**
	 * Batch add a bunch of players to the game
	 * 
	 * @param amount
	 *            The number of players to add (max 9)
	 */
	public void addPlayers(final int amount) throws Exception {
	    for (int i = 0; i < amount; i++) {
		this.addPlayer(new DefaultPlayer(TableLogic.this));
	    }
	}

	/** Get the list of players currently on this table */
	public List<IPlayer> getPlayer() {
	    List<IPlayer> playerList = new ArrayList<IPlayer>(
		    TableLogic.this.player.list.size());
	    for (IPlayer player : TableLogic.this.player.list.keySet()) {
		playerList.add(player);
	    }
	    return playerList;
	}
    }

    /**
     * Player callback function to respond to events fired by the table.
     * 
     * @param action
     * @param data
     * @return
     */
    public boolean interact(Action action, Object data) {
	switch (action) {
	case DROP_CARDSTACK_INITIAL:
	    this.proxyInteractionEvent(action, data);
	    return true;
	case DROP_CARD:
	    CardStack.checkCard((byte) data);
	    if (this.table.cardStackTable.card.add((byte) data)) {
		this.player.setTookAction(true);
		TableLogic.this.player.fireEvent(Event.CARD_DROPPED, data);
		this.proxyInteractionEvent(action, data);
		return true;
	    }
	    // TODO: tell why it failed, if it does
	    return false;
	case END_CALL:
	    byte[] userCards = (byte[]) data;
	    if (Cards.verifyGoal(userCards) && this.game.round.close()) {
		this.game.round.close();
		TableLogic.this.player.fireEvent(Event.GAME_CLOSED);
		this.player.setTookAction(true);
		this.proxyInteractionEvent(action, data);
		return true;
	    }
	    return false;
	case MOVE_FINISHED:
	    this.player.finishedTurn = true;
	    this.proxyInteractionEvent(action, data);
	    return true;
	case PICK_CARD:
	    if (this.table.cardStackTable.card.remove((byte) data)) {
		this.player.setTookAction(true);
		this.proxyInteractionEvent(action, data);
		return true;
	    }
	    // TODO: tell why it failed, if it does
	    return false;
	}
	return false;
    }

    public boolean interact(Action action) {
	return this.interact(action, null);
    }

    /** Proxy player events to the table controller */
    private void proxyInteractionEvent(Action action, Object data) {
	this.tableController.handleTableLogicEvent(action, data);
    }

    /** Setup the table logic */
    public void initialize() {
	this.table.close();
	this.player.fireEvent(TableLogic.Event.GAME_START);
    }
}
