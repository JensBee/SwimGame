package swimgame.table;

/**
 * Custom exceptions for the Table package.
 * 
 * @author <a href="mailto:code@jens-bertram.net">Jens Bertram</a>
 * 
 */
public class TableException extends Exception {
    /** */
    private static final long serialVersionUID = 1L;

    /** Predefined messages to throw. */
    public enum Exception {
	/** Player can't drop the card because he seems not to own it. */
	DROP_NOT_OWNED("You can't drop a card you don't own! (%s)", 0);

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
     * Create a new exception based on an {@link TableException.Exception}.
     * 
     * @param error
     *            {@link TableException.Exception} to throw
     */
    public TableException(final Exception error) {
	super(error.toString());
    }

    /**
     * Create a new exception based on an {@link TableException.Exception}.
     * 
     * @param error
     *            {@link TableException.Exception} to throw
     * @param objects
     *            Replacement for the error format-string
     */
    public TableException(final Exception error, final Object... objects) {
	super(String.format(error.toString(), objects));
    }
}
