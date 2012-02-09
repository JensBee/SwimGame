package swimgame.table.logic;

import cardGame.player.CardPlayer;

/**
 * Custom exceptions for the TableLogic package.
 * 
 * @author <a href="mailto:code@jens-bertram.net">Jens Bertram</a>
 * 
 */
public class TableLogicException extends Exception {
    /** */
    private static final long serialVersionUID = 1L;

    /** Predefined messages to throw. */
    public enum Exception {
	/**
	 * An operation does not succeed, because the {@link Table} was already
	 * set as being closed.
	 */
	TABLE_CLOSED("The table is already closed.", 0), //
	/**
	 * A {@link IPlayer} could not be added to a {@link Table}, because it
	 * was already set as being closed.
	 */
	TABLE_CLOSED_PLAYER_REJECTED(
		"Table is closed! No more player allowed.", 1), //
	/** All games are played, none is left. */
	NO_GAME_LEFT("All games played. Theres no game left.", 2),
	/** The current cards are not sufficient for dropping. */
	CARDS_NOT_DROPPABLE("Given cards can't be dropped!", 3);

	/** Exception message. */
	private String messsage;
	/** Exception code. */
	private int code;

	/**
	 * Default constructor.
	 * 
	 * @param errorMessage
	 *            The exception message
	 * @param errorCode
	 *            The code for this exception
	 */
	Exception(final String errorMessage, final int errorCode) {
	    this.messsage = errorMessage;
	}

	@Override
	public String toString() {
	    return this.messsage;
	}

	/**
	 * Get the error code for this exception.
	 * 
	 * @return The error code
	 */
	public int getCode() {
	    return this.code;
	}
    }

    /**
     * Create a new exception based on an {@link TableLogicException.Exception}.
     * 
     * @param error
     *            The error object
     */
    public TableLogicException(final Exception error) {
	super(error.toString());
    }
}
