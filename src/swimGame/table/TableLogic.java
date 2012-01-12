package swimGame.table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import swimGame.out.Debug;
import swimGame.player.DefaultPlayer;
import swimGame.player.IPlayer;

public class TableLogic {
    // nested classes
    public final TableLogic.Game game;
    public final TableLogic.Player player;
    public final TableLogic.Table table;
    // name for nice console out
    private static final String CNAME = "Table";
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

    public TableLogic(ITable tableController) {
	this.tableController = tableController;
	this.table = new TableLogic.Table();
	this.player = new TableLogic.Player();
	this.game = new TableLogic.Game();
    }

    public TableLogic(ITable tableController, int gamesToPlay) {
	this.tableController = tableController;
	this.player = new TableLogic.Player();
	this.game = new TableLogic.Game(gamesToPlay);
	this.table = new TableLogic.Table();
    }

    public TableLogic(ITable tableController, int maxRoundsPerGame,
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
	/** List of players joined this table */
	// linkedHashMap to ensure element ordering
	protected final HashMap<IPlayer, Double> list = new LinkedHashMap<IPlayer, Double>();
	// has current player performed an action before continuing?
	public boolean hasTakenAnAction = false;
	// Tracks if a player has finished his current move
	public boolean moveFinished = false;

	/** Comparator for player HashMap */
	private class PlayerComparator implements Comparator<IPlayer> {
	    @Override
	    public int compare(IPlayer o1, IPlayer o2) {
		Double p1 = TableLogic.Player.this.list.get(o1);
		Double p2 = TableLogic.Player.this.list.get(o1);
		if (p1 < p2) {
		    return -1;
		}
		if (p1 == p2) {
		    return 0;
		}
		return 1;
	    }
	}

	// allows comparison of players by their overall points
	PlayerComparator playerComparator = new PlayerComparator();

	protected void add(IPlayer player) {
	    this.list.put(player, new Double(0));
	}

	/** Get the current player index */
	protected IPlayer get(int index) {
	    if ((index > -1) && (index <= this.list.size())) {
		return (IPlayer) this.list.keySet().toArray()[0];
	    }
	    return null;
	}

	/** Get a new iterator */
	protected PlayerIterator iterator(boolean wrapAround) {
	    return new PlayerIterator(wrapAround);
	}

	/** Add points to the all-games counter */
	protected void addPoints(IPlayer player, Double pointsToAdd) {
	    Double currentPoints = this.getPoints(player);
	    this.list.put(player, currentPoints + pointsToAdd);
	}

	/** Get the all-games points for a player */
	protected double getPoints(IPlayer player) {
	    return this.list.get(player);
	}

	/** Get the list of players ranked by their overall game-points */
	protected Map<IPlayer, Double> getRanked() {
	    // sort
	    List<IPlayer> keys = new ArrayList<IPlayer>(this.list.size());
	    keys.addAll(this.list.keySet());
	    Collections.sort(keys, new Comparator<IPlayer>() {
		@Override
		public int compare(IPlayer o1, IPlayer o2) {
		    Double p1 = Player.this.list.get(o1);
		    Double p2 = Player.this.list.get(o2);
		    return p1.compareTo(p2);
		}
	    });

	    Map<IPlayer, Double> rankedMap = new HashMap<IPlayer, Double>();
	    for (IPlayer player : keys) {
		rankedMap.put(player, this.list.get(player));
	    }
	    return rankedMap;
	}

	/** Amount of players on this table */
	protected int size() {
	    return this.list.size();
	}

	/**
	 * Fire an event to all registered players.
	 * 
	 * TODO: data needs to be generalized
	 * 
	 * @param event
	 *            The event
	 * @param data
	 *            Some sort of data associated with an event
	 */
	protected void fireEvent(Event event, Object data) {
	    for (IPlayer player : this.list.keySet()) {
		player.handleTableEvent(event, data);
	    }
	}

	/** @see TableLogic.Player#fireEvent(Event, Object) */
	public void fireEvent(Event event) {
	    this.fireEvent(event, null);
	}

	/**
	 * Manage players in game-rounds
	 * 
	 * @author Jens Bertram <code@jens-bertram.neinitializet>
	 * 
	 */
	private class PlayerIterator implements Iterator<IPlayer> {
	    private byte pointer = -1;
	    private byte closingPointer = -1;
	    // restart iteration from the beginning, if we hit the end?
	    private boolean wrapAround = false;
	    // has last next() call wrapped the pointer?
	    private boolean hasWrapped = false;
	    // get the IPlayer objects as array for easy index handling
	    private IPlayer[] player;
	    // track, if we have already build the player array
	    private final boolean initialized = false;

	    /**
	     * @param wrapAround
	     *            If true, the iterator will restart at the beginning,
	     *            if hitting the end of the player list
	     */
	    public PlayerIterator(boolean wrapAround) {
		this.wrapAround = wrapAround;
	    }

	    protected void initialize() {
		int i = 0;
		this.player = new IPlayer[TableLogic.this.player.list.size()];
		for (IPlayer player : TableLogic.this.player.list.keySet()) {
		    this.player[i] = player;
		    i++;
		}
	    }

	    @Override
	    public boolean hasNext() {
		if (this.initialized == false) {
		    this.initialize();
		}

		if (this.wrapAround) {
		    // this one is never ending
		    return true;
		}

		if ((this.pointer + 1) < this.player.length) {
		    if ((this.pointer + 1) == this.closingPointer) {
			return false;
		    }
		    return true;
		}
		return false;
	    }

	    protected void setPointer(int pointer) {
		this.pointer = (byte) pointer;
	    }

	    @Override
	    public IPlayer next() {
		if (this.initialized == false) {
		    this.initialize();
		}

		if (this.hasNext()) {
		    this.pointer++;

		    if (this.pointer == this.player.length) {
			this.hasWrapped = true;
			this.reset();
			this.pointer++;
		    } else {
			this.hasWrapped = false;
		    }

		    IPlayer player = this.get();
		    TableLogic.this.player.moveFinished = false;
		    return player;
		}
		return null;
	    }

	    /** Resets the iterator to the beginning of the player-list */
	    protected void reset() {
		this.pointer = -1;
	    }

	    @Override
	    public void remove() {
		// not implemented
	    }

	    /** Index of the current player in the player list */
	    protected byte getIndex() {
		if (this.initialized == false) {
		    this.initialize();
		}
		return (this.pointer == -1) ? 0 : this.pointer;
	    }

	    /** Get the player the iterator is currently pointing at */
	    protected IPlayer get() {
		return this.player[this.getIndex()];
	    }

	    /**
	     * Set the current player as the one who closed the game.
	     * 
	     * @return Any second try to close will be signaled by returning
	     *         false;
	     */
	    protected boolean setAsClosing() {
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
	    public boolean hasWrapped() {
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
	// after how many rounds should the game be interrupted? (to catch
	// non-enRoundsding games)
	private int maxRoundsToPlay = 32;
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
	protected Game() {
	    this.round = new Round();
	}

	/**
	 * Constructor
	 * 
	 * @param numberOfGames
	 *            Number of games to play
	 */
	protected Game(int numberOfGames) {
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
	protected Game(int numberOfGames, int maxRoundsToPlay) {
	    this.numberOfGamesToPlay = numberOfGames;
	    this.round = new Round(maxRoundsToPlay);
	}

	/**
	 * Initialize the game. This should be called, if all players finished
	 * joining the table.
	 */
	protected void initialize() {
	    // when & who?
	    this.round.initialize();
	}

	/** Check if there's a next game we want to play */
	@Override
	public boolean hasNext() {
	    if ((this.currentGameNumber + 1) <= this.numberOfGamesToPlay) {
		return true;
	    }
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
		this.finished = true;
	    }
	    this.round.reset();
	    this.startingPlayer = this.players.next();
	    this.round.setCurrentPlayer(this.startingPlayer);
	    TableLogic.this.game.round.setCurrentPlayer(this.startingPlayer);
	    TableLogic.this.table.cardStackTable = new CardStack(false);
	    TableLogic.this.table.cardStack = new CardStack(true);
	    TableLogic.this.dealOutCards(this.startingPlayer);
	    return this.startingPlayer;
	}

	/** Test, if the current game is finished */
	public boolean isFinished() {
	    return this.finished;
	}

	public int current() {
	    return this.currentGameNumber;
	}

	@Override
	public void remove() {
	    // not implemented
	}

	protected void setNumberOfGamesToPlay(int numberOfGamesToPlay) {
	    this.numberOfGamesToPlay = numberOfGamesToPlay;
	}

	public int getMaxRoundsToPlay() {
	    return this.maxRoundsToPlay;
	}

	protected void setMaxRoundsToPlay(int maxRoundsToPlay) {
	    this.maxRoundsToPlay = maxRoundsToPlay;
	}

	/**
	 * Rounds of a game being played
	 * 
	 * @author @author Jens Bertram <code@jens-bertram.net>
	 * 
	 */
	public class Round implements Iterator<Integer> {
	    private byte currentRound = 1;
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

	    /** Set the current active player */
	    protected void setCurrentPlayer(IPlayer playerToSet) {
		byte i = 0;
		for (IPlayer player : TableLogic.this.player.list.keySet()) {
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
		this.currentPlayer = this.players.next();
	    }

	    /**
	     * Start the next round
	     * 
	     * @return The player who is starting this new round
	     */
	    public IPlayer nextPlayer() {
		this.currentPlayer = this.players.next();
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
		if (this.finished) {
		    return false;
		}
		if ((this.currentRound + 1) < this.maxNumberOfRounds) {
		    return true;
		}
		this.finished = true;
		return false;
	    }

	    /** Reset the round counter. Useful if a new game has begun */
	    protected void reset() {
		this.closingPlayer = null;
		this.currentPlayer = null;
		this.currentRound = 1;
		this.finished = false;
	    }

	    /** Has this round finished? */
	    public boolean isFinished() {
		return this.finished;
	    }

	    /** Current round number */
	    public int current() {
		return this.currentRound;
	    }

	    /** Current player will close this round */
	    protected boolean close() {
		if (this.players.setAsClosing()) {
		    this.closingPlayer = this.players.get();
		    return true;
		}
		return false;
	    }

	    /** Test, if a given player has closed the round */
	    public boolean hasClosed(IPlayer player) {
		return player.equals(this.closingPlayer);
	    }

	    /** Get the current player of this round */
	    protected IPlayer currentPlayer() {
		return this.currentPlayer;
	    }

	    @Override
	    public void remove() {
		// not implemented
	    }
	}
    }

    private static class Cards {
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
	protected CardStack cardStack = new CardStack(true);
	// cards on the table
	public CardStack cardStackTable = new CardStack(false);

	/**
	 * Close player participation opportunity and prepare game start.
	 */
	public void close() {
	    if (this.closed != true) {
		TableLogic.this.player.fireEvent(Event.TABLE_CLOSED, null);
		this.closed = true;

		if (Debug.debug == true) {
		    Debug.println(this.getClass(), String.format(
			    "Players: %d  Maximum rounds: %d",
			    TableLogic.this.player.size(),
			    TableLogic.this.game.maxRoundsToPlay));
		}
	    }
	}

	protected boolean isClosed() {
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
		    || (TableLogic.this.player.size() == MAX_PLAYER)) {
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
     * Deliver cards to players
     */
    private void dealOutCards(IPlayer beginningPlayer) {
	byte[] initialCardSet = new byte[3];

	for (IPlayer player : this.player.list.keySet()) {
	    if (player == beginningPlayer) {
		initialCardSet = this.getPlayerCardSet();
		player.setCards(initialCardSet);
	    } else {
		// pass initial cards to player
		player.setCards(this.getPlayerCardSet());
	    }
	}

	// deal out a second set of cards for the first player, if he want so
	if (beginningPlayer.keepCardSet() == false) {
	    // update cards on table
	    this.player.fireEvent(Event.INITIAL_CARDSTACK_DROPPED,
		    initialCardSet);
	    TableLogic.this.proxyInteractionEvent(
		    Action.DROP_CARDSTACK_INITIAL, initialCardSet);
	    this.table.cardStackTable.card.add(initialCardSet);
	    beginningPlayer.setCards(this.getPlayerCardSet());
	} else {
	    // player took first cards - make a second public
	    byte cards[] = this.getPlayerCardSet();
	    TableLogic.this.proxyInteractionEvent(
		    Action.INITIAL_CARDSTACK_PICKED, null);
	    // update cards on table
	    this.table.cardStackTable.card.add(cards);
	}
    }

    /**
     * Generate a random card set for a player
     * 
     * @return The randomly generated card set
     * @throws Exception
     */
    private byte[] getPlayerCardSet() {
	return new byte[] { this.table.cardStack.card.getRandom(),
		this.table.cardStack.card.getRandom(),
		this.table.cardStack.card.getRandom() };
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
		this.player.hasTakenAnAction = true;
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
		this.player.hasTakenAnAction = true;
		this.proxyInteractionEvent(action, data);
		return true;
	    }
	    return false;
	case MOVE_FINISHED:
	    this.player.moveFinished = true;
	    this.proxyInteractionEvent(action, data);
	    return true;
	case PICK_CARD:
	    if (this.table.cardStackTable.card.remove((byte) data)) {
		this.player.hasTakenAnAction = true;
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
    }
}
