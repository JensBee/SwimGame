package swimgame.table.logic;

import cardGame.player.CardPlayer;

/**
 * Games handling.
 * 
 * @author <a href="mailto:code@jens-bertram.net">Jens Bertram</a>
 * 
 */
public class Game {
    /** Indicates if a game is finished. */
    // private final boolean finished = false;
    /** The number of games to play in turn. */
    private int numberOfGamesToPlay = 1;
    /** The current game number. */
    private int currentGameNumber = 0;
    /** {@link Player.PlayerIterator} for all players of this game. */
    private Player.PlayerIterator players;
    /** The {@link CardPlayer} starting this game or round. */
    private CardPlayer startingPlayer;
    /** {@link Round} handling the rounds of this {@link Game} instance. */
    // CHECKSTYLE:OFF
    final Round round;
    // CHECKSTYLE:ON
    /** The {@link Table} this game belongs to. */
    private Table table;

    /**
     * Get the {@link Round} instance associated with this {@link Game}
     * instance.
     * 
     * @return The {@link Game} instance associated with this {@link Game}
     *         instance
     */
    public final Round getRound() {
	return this.round;
    }

    /**
     * Initialize the game class.
     * 
     * @param newTable
     *            The {@link Table} associated with this {@link Game} instance
     * @param maxRoundsToPlay
     *            The maximum rounds to play per game. If set to <=0 the default
     *            as defined by {@link TableLogic#DEFAULT_MAX_ROUNDS} will be
     *            used
     */
    private void initGame(final Table newTable, final int maxRoundsToPlay) {
	this.table = newTable;
	this.players = this.table.player.iterator(true);
    }

    /**
     * Initialize the game class.
     * 
     * @param newTable
     *            The {@link Table} instance associated with this {@link Game}
     *            instance
     */
    private void initGame(final Table newTable) {
	this.initGame(newTable, -1);
    }

    /**
     * Empty constructor.
     * 
     * @param newTable
     *            The {@link Table} instance associated with this {@link Game}
     *            instance
     */
    Game(final Table newTable) {
	this.initGame(newTable);
	this.round = new Round(newTable);
    }

    /**
     * Constructor.
     * 
     * @param newTable
     *            The {@link Table} instance associated with this {@link Game}
     *            instance
     * @param numberOfGames
     *            Number of games to play
     */
    Game(final Table newTable, final int numberOfGames) {
	this.initGame(newTable);
	this.round = new Round(newTable);
	this.numberOfGamesToPlay = numberOfGames;
    }

    /**
     * Constructor.
     * 
     * @param newTable
     *            The {@link Table} instance associated with this {@link Game}
     *            instance
     * @param numberOfGames
     *            Number of games to play
     * @param maxRoundsToPlay
     *            The maximum rounds to play per game. If set to <=0 the default
     *            as defined by {@link TableLogic#DEFAULT_MAX_ROUNDS} will be
     *            used
     */
    Game(final Table newTable, final int numberOfGames,
	    final int maxRoundsToPlay) {
	this.initGame(newTable, maxRoundsToPlay);
	this.round = new Round(newTable, maxRoundsToPlay);
	this.numberOfGamesToPlay = numberOfGames;
    }

    /** Setup the game logic. */
    public final void initialize() {
	this.players = this.table.player.iterator(true);
	this.currentGameNumber = 0;
	this.round.initialize();
    }

    /**
     * Check, if there's a next game to play.
     * 
     * @return True, if there's a next game
     */
    public final boolean hasNext() {
	if ((this.currentGameNumber + 1) <= this.numberOfGamesToPlay) {
	    return true;
	}
	return false;
    }

    /**
     * Prepare the next game. This should handle all needed steps to start a new
     * round.
     * 
     * @return The next game starting {@link CardPlayer}
     * @throws TableLogicException
     *             Thrown, if no next player is left
     */
    public final CardPlayer next() throws TableLogicException {
	if (!this.hasNext()) {
	    throw new TableLogicException(
		    TableLogicException.Exception.NO_GAME_LEFT);
	}
	this.currentGameNumber++;

	// initialize new game
	this.round.reset();
	this.startingPlayer = this.players.next();
	this.round.setCurrentPlayer(this.startingPlayer);
	this.table.game.round.setCurrentPlayer(this.startingPlayer);
	this.table.resetCards();
	this.table.dealOutCards(this.startingPlayer);
	return this.startingPlayer;
    }

    /**
     * Get the current game number that is played.
     * 
     * @return The number of the current game
     */
    public final int current() {
	return this.currentGameNumber;
    }

    /**
     * Set the number of games to play.
     * 
     * @param gamesCount
     *            The number of games to play.
     */
    public final void setNumberOfGamesToPlay(final int gamesCount) {
	this.numberOfGamesToPlay = gamesCount;
    }

    /**
     * Get the number of games to play.
     * 
     * @return The number of games to play
     */
    public final int getNumberOfGamesToPlay() {
	return this.numberOfGamesToPlay;
    }
}
