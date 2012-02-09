package cardGame.table;

import cardGame.games.swimming.Table;

public class TableException extends Exception {
    /** */
    private static final long serialVersionUID = 1L;

    /** Predefined messages to throw. */
    public enum TableExceptions {
	/**
	 * An operation does not succeed, because the {@link Table} was already
	 * set as being closed.
	 */
	TABLE_CLOSED("The table is already closed."), //
	/**
	 * A {@link IPlayer} could not be added to a {@link Table}, because it
	 * was already set as being closed.
	 */
	TABLE_CLOSED_PLAYER_REJECTED("Table is closed! No more player allowed."), //
	/** All games are played, none is left. */
	NO_GAME_LEFT("All games played. Theres no game left.");

	/** Exception message. */
	private final String messsage;

	/**
	 * Default constructor.
	 * 
	 * @param errorMessage
	 *            The exception message
	 */
	TableExceptions(final String errorMessage) {
	    this.messsage = errorMessage;
	}

	@Override
	public String toString() {
	    return this.ordinal() + ": " + this.messsage;
	}
    }

    /**
     * Create a new exception based on an {@link TableException.Exception}.
     * 
     * @param error
     *            The error object
     */
    public TableException(final TableExceptions error) {
	super(error.toString());
    }
}