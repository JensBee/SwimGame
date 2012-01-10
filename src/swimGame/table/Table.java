package swimGame.table;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.Iterator;

import swimGame.out.Console;
import swimGame.out.Debug;
import swimGame.player.DefaultPlayer;
import swimGame.player.IPlayer;

public class Table {
    // Table will be closed, if full or the game has begun
    private boolean tableClosed = false;
    /** Card stack owned by this table (stack will be full, i.e. has all cards) */
    private final CardStack cardStack = new CardStack(true);
    // cards on the table
    private final CardStack cardStackTable = new CardStack(false);
    // name for console out
    private static final String CNAME = "Table";
    // Tracks if a player has finished his current move
    private boolean playerMoveFinished = false;
    // after how many rounds should the game be interrupted? (to catch
    // non-enRoundsding games)
    public static int MAX_ROUNDS = 32;
    // maximum number of allowed players
    public static final int MAX_PLAYER = 9;
    // should we wait for input after each round?
    public static boolean pauseAfterRound = false;
    // has current player performed an action before continuing?
    boolean playerHasTakenAnAction = false;

    // nested classes
    private final Game game;
    private final LogWriter logWriter = new LogWriter();
    private final Player player;

    // points for three cards of same type, but different color
    public static final double WORTH_THREE_OF_SAME_TYPE = 30.5;

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
    }

    /**
     * Handles player on this table
     * 
     * @author Jens Bertram <code@jens-bertram.net>
     * 
     */
    private class Player {
	/** List of players joined this table */
	private final ArrayList<IPlayer> list = new ArrayList<IPlayer>();

	protected void add(IPlayer player) {
	    this.list.add(player);
	}

	protected IPlayer get(int index) {
	    return ((index > -1) && (index <= this.list.size())) ? this.list
		    .get(index) : null;
	}

	/** Get a new iterator */
	protected PlayerIterator iterator(boolean wrapAround) {
	    return new PlayerIterator(wrapAround);
	}

	protected int size() {
	    return this.list.size();
	}

	/**
	 * Manage players in game-rounds
	 * 
	 * @author Jens Bertram <code@jens-bertram.net>
	 * 
	 */
	private class PlayerIterator implements Iterator<IPlayer> {
	    private int pointer = -1;
	    private int closingPointer = -1;
	    // restart iteration from the beginning, if we hit the end?
	    private boolean wrapAround = false;
	    // has last next() call wrapped the pointer?
	    private boolean hasWrapped = false;

	    public PlayerIterator(boolean wrapAround) {
		this.wrapAround = wrapAround;
	    }

	    @Override
	    public boolean hasNext() {
		if (this.wrapAround) {
		    // this one is never ending
		    return true;
		}
		if ((this.pointer + 1) < Table.this.player.size()) {
		    if ((this.pointer + 1) == this.closingPointer) {
			return false;
		    }
		    return true;
		}
		return false;
	    }

	    protected boolean nextIsClosing() {
		if ((this.pointer + 1) == this.closingPointer) {
		    return true;
		}
		return false;
	    }

	    @Override
	    public IPlayer next() {
		if (this.hasNext()) {
		    this.pointer++;

		    if (this.pointer == Table.this.player.size()) {
			this.hasWrapped = true;
			this.reset();
			this.pointer++;
		    } else {
			this.hasWrapped = false;
		    }

		    IPlayer player = this.get();
		    Console.println(Table.CNAME,
			    String.format("It's your turn %s", player));
		    Table.this.playerMoveFinished = false;
		    return player;
		}
		return null;
	    }

	    protected void reset() {
		this.pointer = -1;
	    }

	    @Override
	    public void remove() throws IllegalStateException {
		throw new IllegalStateException("Operation not supported.");
	    }

	    protected int getId() {
		return (this.pointer == -1) ? 0 : this.pointer;
	    }

	    protected IPlayer get() {
		return Table.this.player.get(this.getId());
	    }

	    /**
	     * Set the current player as the one who closed the game.
	     * 
	     * @return Any second try to close will be signaled by returning
	     *         false;
	     */
	    protected boolean setAsClosing() {
		if (this.closingPointer == -1) {
		    this.closingPointer = this.getId();
		    return true;
		}
		return false;
	    }

	    public boolean hasWrapped() {
		return this.hasWrapped;
	    }
	}
    }

    /**
     * Ease handling of lag entries
     * 
     * @author Jens Bertram <code@jens-bertram.net>
     * 
     */
    private class LogWriter {
	private String getPlayerName(IPlayer player) {
	    return String.format("<%s> ", player.toString());
	}

	private String getPlayerName() {
	    return this.getPlayerName(Table.this.game.round.currentPlayer());
	}

	protected void player(String format, Object... args) {
	    Console.println(Table.CNAME, this.getPlayerName()
		    + (new Formatter().format(format, args).toString()));
	}

	protected void player(String message) {
	    this.player(message, (Object[]) null);
	}

	protected void player(IPlayer player, String format, Object... args) {
	    Console.println(Table.CNAME, this.getPlayerName(player)
		    + (new Formatter().format(format, args).toString()));
	}

	protected void player(IPlayer player, String message) {
	    this.player(player, message, (Object[]) null);
	}

	protected void write(String format, Object... args) {
	    Console.println(Table.CNAME, new Formatter().format(format, args)
		    .toString());
	}

	protected void write(String message) {
	    this.write(message, (Object[]) null);
	}
    }

    private static class Cards {
	protected static boolean verifyGoal(byte[] cards) {
	    CardStack userCardStack;
	    int cardsCount;

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

    /**
     * Games handling
     * 
     * @author Jens Bertram <code@jens-bertram.net>
     * 
     */
    private class Game {
	/**
	 * Rounds of a game being played
	 * 
	 * @author @author Jens Bertram <code@jens-bertram.net>
	 * 
	 */
	private class Round {
	    private int currentRound = 1;
	    private int maxNumberOfRounds = 32;
	    private boolean finished = false;
	    private final Table.Player.PlayerIterator players;
	    private IPlayer currentPlayer;
	    private IPlayer closingPlayer;

	    /** Empty constructor */
	    protected Round() {
		this.players = Table.this.player.iterator(true);
	    }

	    /**
	     * Constructor
	     * 
	     * @param maxNumberOfRounds
	     *            Maximum number of rounds to play
	     */
	    protected Round(int maxNumberOfRounds) {
		this.players = Table.this.player.iterator(true);
		this.maxNumberOfRounds = maxNumberOfRounds;
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
	    protected IPlayer nextPlayer() {
		this.currentPlayer = this.players.next();
		if (this.players.hasWrapped()) {
		    this.next();
		}
		return this.currentPlayer;
	    }

	    protected void next() {
		this.currentRound++;
		if (this.currentRound == this.maxNumberOfRounds) {
		    this.finished = true;
		}
	    }

	    /** Is there a next round left to play? */
	    protected boolean hasNext() {
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
	    protected boolean isFinished() {
		return this.finished;
	    }

	    /** Current round number */
	    protected int current() {
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
	    protected boolean hasClosed(IPlayer player) {
		return player.equals(this.closingPlayer);
	    }

	    /** Get the current player of this round */
	    protected IPlayer currentPlayer() {
		return this.currentPlayer;
	    }
	}

	private boolean finished = false;
	private int numberOfGamesToPlay = 1;
	private int currentGameNumber = 0;
	private final Table.Player.PlayerIterator players = Table.this.player
		.iterator(true);
	private IPlayer startingPlayer;
	protected Table.Game.Round round;

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
	    this.round.initialize();
	}

	/** Check if there's a next game we want to play */
	protected boolean hasNext() {
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
	protected IPlayer next() {
	    this.currentGameNumber++;
	    if (this.currentGameNumber > this.numberOfGamesToPlay) {
		this.finished = true;
	    }
	    this.round.reset();
	    this.startingPlayer = this.players.next();
	    return this.startingPlayer;
	}

	/** Test, if the current game is finished */
	protected boolean isFinished() {
	    return this.finished;
	}

	protected int current() {
	    return this.currentGameNumber;
	}
    }

    public Table() {
	this.player = new Player();
	this.game = new Game();
    }

    public Table(int gamesToPlay) {
	this.player = new Player();
	this.game = new Game(gamesToPlay);
    }

    public Table(int maxRoundsPerGame, int gamesToPlay) {
	this.player = new Player();
	this.game = new Game(gamesToPlay, maxRoundsPerGame);
    }

    /**
     * Add a single player to the table
     * 
     * @param player
     * @throws Exception
     *             Thrown if table is full or game has already begun
     */
    public void addPlayer(final IPlayer player) throws Exception {
	if ((this.tableClosed == true) || (this.player.size() == MAX_PLAYER)) {
	    this.close(); // just to be sure
	    throw new Exception("Table is closed! No more player allowed.");
	}

	this.player.add(player);
	this.logWriter.player(player, "joined the table");
    }

    /**
     * Batch add a bunch of players to the game
     * 
     * @param amount
     *            The number of players to add (max 9)
     */
    public void addPlayers(final int amount) throws Exception {
	for (int i = 0; i < amount; i++) {
	    this.addPlayer(new DefaultPlayer());
	}
    }

    /**
     * Close player participation opportunity and prepare game start.
     */
    private void close() {
	if (this.tableClosed != true) {
	    this.firePlayerEvent(Event.TABLE_CLOSED, null);
	    this.tableClosed = true;
	    String playerNames = "";
	    for (IPlayer p : this.player.list) {
		playerNames += p.toString() + ", ";
	    }
	    Console.nl();
	    this.logWriter.write("*** Table closed ***");
	    if (Debug.debug == true) {
		Debug.println(this.getClass(), String.format(
			"Players: %d  Maximum rounds: %d", this.player.size(),
			Table.MAX_ROUNDS));
	    }
	    this.logWriter.write(String.format("Players (in order) are: %s",
		    playerNames.subSequence(0, playerNames.length() - 2)));

	    if (Console.ask) {
		Console.println("\nPress return to start...");
		try {
		    System.in.read();
		} catch (IOException e) {
		    e.printStackTrace();
		}
	    }
	}
    }

    /**
     * Get the game started
     */
    public void start() throws Exception {
	if (this.player.size() <= 1) {
	    throw new Exception("No game without players!");
	}

	this.close();
	this.firePlayerEvent(Event.GAME_START, null);

	this.game.initialize();

	this.dealOutCards(this.game.round.currentPlayer());

	IPlayer currentPlayer;
	while (this.game.hasNext()) {
	    this.game.next();

	    while ((this.game.isFinished() == false)
		    && this.game.round.hasNext()) {

		Debug.println(String.format("*** Game %d ** Round %d ***",
			this.game.current(), this.game.round.current()));

		currentPlayer = this.game.round.nextPlayer();
		if (this.game.round.hasClosed(currentPlayer)) {
		    break;
		}

		this.logWriter
			.write("Cards: " + this.cardStackTable.toString());
		this.playerHasTakenAnAction = false;
		currentPlayer.doMove(this.cardStackTable.getCards());
		while (this.playerMoveFinished != true) {
		}
		if (this.playerHasTakenAnAction == false) {
		    this.logWriter.write("%s skipped!",
			    currentPlayer.toString());
		}
	    }

	    this.fireEvent(Event.GAME_FINISHED);

	    if (this.game.round.isFinished()) {
		this.logWriter
			.write("Sorry players, I'll stop here! You reached the maximum of %d rounds without anyone winning.",
				Table.MAX_ROUNDS);
	    }
	    this.generateRating();

	    if (pauseAfterRound && this.game.hasNext()) {
		Console.println("\nPress return to start the next game...");
		try {
		    System.in.read();
		} catch (IOException e) {
		    e.printStackTrace();
		}
	    }
	}
    }

    /** Generate a simple rating for all players */
    private void generateRating() {
	Console.nl();
	this.logWriter.write("Player-rating:");
	for (IPlayer player : this.player.list) {
	    byte[] playerCards = player.getCards();
	    String playerName = (this.game.round.hasClosed(player)) ? "* "
		    + player.toString() : player.toString();
	    if (playerCards == null) {
		this.logWriter.write(" %s gave us no card information",
			playerName);
	    } else {
		CardStack playerCardStack = new CardStack(playerCards);
		this.logWriter.write(" %s's cards: %s value: %.0f", playerName,
			playerCardStack.toString(), playerCardStack.getValue());
	    }
	}
    }

    /**
     * Generate a random card set for a player
     * 
     * @return The randomly generated card set
     * @throws Exception
     */
    private byte[] getPlayerCardSet() throws Exception {
	return new byte[] { this.cardStack.card.getRandom(),
		this.cardStack.card.getRandom(),
		this.cardStack.card.getRandom() };
    }

    public boolean interact(Action action) {
	return this.interact(action, null);
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
	    return true;
	case DROP_CARD:
	    CardStack.checkCard((byte) data);
	    this.logWriter.player("dropped card %s",
		    this.cardStack.card.toString((byte) data));

	    if (this.cardStackTable.card.add((byte) data)) {
		this.playerHasTakenAnAction = true;
		this.firePlayerEvent(Event.CARD_DROPPED, data);
		return true;
	    }
	    // TODO: tell why it failed, if it does
	    return false;
	case END_CALL:
	    byte[] userCards = (byte[]) data;
	    if (Cards.verifyGoal(userCards) && this.game.round.close()) {
		this.game.round.close();
		this.logWriter.player("is closing. Last call!");
		this.fireEvent(Event.GAME_CLOSED);
		this.playerHasTakenAnAction = true;
		return true;
	    }
	    return false;
	case MOVE_FINISHED:
	    this.playerMoveFinished = true;
	    return true;
	case PICK_CARD:
	    this.logWriter.player("picked card %s",
		    this.cardStack.card.toString((byte) data));

	    if (this.cardStackTable.card.remove((byte) data)) {
		this.playerHasTakenAnAction = true;
		return true;
	    }
	    // TODO: tell why it failed, if it does
	    return false;
	}
	return false;
    }

    /**
     * Deliver cards to players
     */
    private void dealOutCards(IPlayer beginningPlayer) throws Exception {
	byte[] firstCards = new byte[3];

	this.logWriter.write("Dealing out cards..");
	for (IPlayer p : this.player.list) {
	    if (p == beginningPlayer) {
		firstCards = this.getPlayerCardSet();
		p.setCards(firstCards);
	    } else {
		// pass initial cards to player
		p.setCards(this.getPlayerCardSet());
	    }
	}

	this.logWriter.player("begins!");

	// deal out a second set of cards for the first player, if he want so
	if (beginningPlayer.keepCardSet() == false) {
	    // player is about to drop his cards - make them public
	    this.logWriter.player("drops the initial card set: %s",
		    new CardStack(firstCards).toString());
	    this.firePlayerEvent(Event.INITIAL_CARDSTACK_DROPPED, firstCards);

	    // update cards on table
	    this.cardStackTable.card.add(firstCards);

	    // deal out second card set
	    this.logWriter.write("Dealing out a second card set for %s..",
		    beginningPlayer);
	    beginningPlayer.setCards(this.getPlayerCardSet());
	} else {
	    // player took first cards - make a second public
	    byte cards[] = this.getPlayerCardSet();
	    this.firePlayerEvent(Event.INITIAL_CARDSTACK_DROPPED, cards);
	    // update cards on table
	    this.cardStackTable.card.add(cards);
	}
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
    private void firePlayerEvent(Event event, Object data) {
	for (IPlayer p : this.player.list) {
	    p.handleTableEvent(event, data);
	}
    }

    private void fireEvent(Event event) {
	this.firePlayerEvent(event, null);
    }
}
