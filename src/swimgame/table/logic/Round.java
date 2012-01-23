package swimgame.table.logic;

import java.util.Iterator;

import swimgame.player.IPlayer;

/**
 * Rounds of a game being played.
 * 
 * @author <a href="mailto:code@jens-bertram.net">Jens Bertram</a>
 * 
 */
public class Round implements Iterator<Integer> {
    /** The current round that is played. */
    private byte currentRound = -1;
    /** The maximum number of rounds to play without anyone winning. */
    private int maxRounds = TableLogic.DEFAULT_MAX_ROUNDS;
    /** Has this round finished? */
    private boolean finished = false;
    /** The iterator for players participating in this round. */
    private final Player.PlayerIterator players;
    /** The current {@link IPlayer}. */
    private IPlayer currentPlayer;
    /** The {@link IPlayer} who called closing this round. */
    private IPlayer closingPlayer;
    /** The {@link Table} this round is happening at. */
    private final Table table;

    /**
     * Creates a new game round. The game will happen at the given {@link Table}
     * .
     * 
     * @param newTable
     *            The {@link Table} this Round is happening at
     */
    Round(final Table newTable) {
	this.table = newTable;
	this.players = this.table.player.iterator(true);
    }

    /**
     * Constructor.
     * 
     * @param newTable
     *            The {@link Table} this Round is happening at
     * @param newMaxRounds
     *            Maximum number of rounds to play
     */
    Round(final Table newTable, final int newMaxRounds) {
	this.table = newTable;
	this.players = this.table.player.iterator(true);
	this.maxRounds = newMaxRounds;
    }

    /**
     * Get the number of maximum rounds to play per game.
     * 
     * @return Maximum number of rounds per game
     */
    public final int getMaxLength() {
	return this.maxRounds;
    }

    /**
     * Set the number of maximum rounds to play per game.
     * 
     * @param newMaxRounds
     *            Maximum number of rounds to play
     */
    public final void setMaxLength(final int newMaxRounds) {
	this.maxRounds = newMaxRounds;
    }

    /**
     * Set the current active {@link IPlayer}.
     * 
     * @param playerToSet
     *            {@link IPlayer} to set as current
     */
    final void setCurrentPlayer(final IPlayer playerToSet) {
	byte i = 0;
	for (IPlayer player : this.table.player.asList()) {
	    if (player.equals(playerToSet)) {
		this.players.setPointer(i);
		this.currentPlayer = playerToSet;
		break;
	    }
	    i++;
	}
    }

    /**
     * Initialize the round managing. This must be called, after all players
     * have joined the table.
     */
    final void initialize() {
	this.currentRound = -1;
	this.finished = false;
	this.currentPlayer = this.players.next();
    }

    /**
     * Start the next round.
     * 
     * @return The {@link IPlayer} who is starting this new round
     */
    public final IPlayer nextPlayer() {
	this.currentPlayer = this.players.next();
	this.table.player.setTookAction(false);
	if (this.players.hasWrapped()) {
	    // FIXME: really next on wrap?
	    this.next();
	}
	return this.currentPlayer;
    }

    @Override
    public final Integer next() {
	this.currentRound++;
	if (this.currentRound == this.maxRounds) {
	    this.finished = true;
	}
	return (int) this.currentRound;
    }

    @Override
    public final boolean hasNext() {
	if ((this.currentRound + 1) <= this.maxRounds) {
	    return true;
	}
	return false;
    }

    /** Reset the round counter. Useful if a new game has begun */
    final void reset() {
	this.closingPlayer = null;
	this.currentPlayer = null;
	this.currentRound = 0;
	this.finished = false;
    }

    /**
     * Has this round finished?
     * 
     * @return True if round has finished
     */
    public final boolean isFinished() {
	return this.finished;
    }

    /**
     * Get the current round number.
     * 
     * @return The current round number
     */
    public final int getCurrent() {
	return this.currentRound;
    }

    /**
     * Current player will close this round.
     * 
     * @return True if closing was accepted. If false the table might be already
     *         closed or the cards weren't accepted.
     * @throws TableLogicException
     *             Thrown if cards not droppable
     */
    public final boolean close() throws TableLogicException {
	if (!TableLogic.verifyGoal(this.getCurrentPlayer().getCards())) {
	    throw new TableLogicException(
		    TableLogicException.Exception.CARDS_NOT_DROPPABLE);
	}
	if (this.players.setAsClosing()) {
	    this.closingPlayer = this.players.get();
	    return true;
	}
	return false;
    }

    /**
     * Test, if a given player has closed the round.
     * 
     * @param player
     *            The {@link IPlayer} to test
     * @return True if the given player has closed this round
     */
    public final boolean isClosedBy(final IPlayer player) {
	return player.equals(this.closingPlayer);
    }

    /**
     * Get the current player of this round.
     * 
     * @return The {@link IPlayer} whos turn is current
     */
    public final IPlayer getCurrentPlayer() {
	return this.currentPlayer;
    }

    @Override
    public void remove() {
	// not implemented
    }
}
