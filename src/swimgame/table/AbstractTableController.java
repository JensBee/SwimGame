package swimgame.table;

import java.util.Formatter;

import swimgame.out.Console;
import swimgame.player.IPlayer;
import swimgame.table.logic.TableLogicException;

/**
 * Abstract implementation of the ITableController.
 * 
 * @author <a href="mailto:code@jens-bertram.net">Jens Bertram</a>
 * 
 */
public abstract class AbstractTableController implements ITableController {
    /** Readable class name for nice console output. */
    private final String className = "Table";
    /** An output class for console logging. */
    // CHECKSTYLE:OFF
    protected final LogWriter logWriter = new LogWriter();

    // CHECKSTYLE:ON

    /**
     * Console output handling.
     * 
     * @author <a href="mailto:code@jens-bertram.net">Jens Bertram</a>
     * 
     */
    protected class LogWriter {
	/**
	 * Get the name of the given {@link IPlayer} instance..
	 * 
	 * @param player
	 *            {@link IPlayer} instance to get the name from
	 * @return Player-name of the given {@link IPlayer} instance
	 */
	private String getPlayerName(final IPlayer player) {
	    return String.format("<%s> ", player.toString());
	}

	/**
	 * Get the name of the current active {@link IPlayer} instance..
	 * 
	 * @return Player-name of the currently active {@link IPlayer} instance
	 */
	private String getPlayerName() {
	    return this.getPlayerName(AbstractTableController.this.getLogic()
		    .getTable().getGame().getRound().getCurrentPlayer());
	}

	/**
	 * Write a log entry with the current active player name as prefix.
	 * 
	 * @param format
	 *            Format-string to print
	 * @param args
	 *            Format-String arguments
	 */
	final void player(final String format, final Object... args) {
	    Console.printf(AbstractTableController.this.className,
		    this.getPlayerName() + format, args);
	}

	/**
	 * Write a log entry with the current active player name as prefix.
	 * 
	 * @param message
	 *            Message as string
	 */
	final void player(final String message) {
	    this.player(message, (Object[]) null);
	}

	/**
	 * Write a log entry prefixed with the name of the given {@link IPlayer}
	 * .
	 * 
	 * @param player
	 *            {@link IPlayer} whose name should be used as prefix
	 * @param format
	 *            Format-string for printing
	 * @param args
	 *            Arguments for the format-string
	 */
	final void player(final IPlayer player, final String format,
		final Object... args) {
	    Console.println(
		    AbstractTableController.this.className,
		    this.getPlayerName(player)
			    + (new Formatter().format(format, args).toString()));
	}

	/**
	 * Write a log entry prefixed with the name of the given {@link IPlayer}
	 * .
	 * 
	 * @param player
	 *            {@link IPlayer} whose name should be used as prefix
	 * @param message
	 *            Message to print
	 */
	final void player(final IPlayer player, final String message) {
	    this.player(player, message, (Object[]) null);
	}

	/**
	 * Write a log entry.
	 * 
	 * @param format
	 *            Format-string to write
	 * @param args
	 *            Arguments for the format-string
	 */
	final void write(final String format, final Object... args) {
	    Console.println(AbstractTableController.this.className,
		    new Formatter().format(format, args).toString());
	}

	/**
	 * Write a log entry.
	 * 
	 * @param message
	 *            Message to write
	 */
	final void write(final String message) {
	    this.write(message, (Object[]) null);
	}
    }

    @Override
    public final void addPlayer(final IPlayer player)
	    throws TableLogicException {
	this.getLogic().getTable().addPlayer(player);
    }

    @Override
    public final void addPlayers(final int amount) throws TableLogicException {
	this.getLogic().getTable().addPlayers(amount);
    }

    @Override
    public final void setMaxRoundsToPlay(final int maxRoundsToPlay) {
	this.getLogic().getTable().getGame().getRound()
		.setMaxLength(maxRoundsToPlay);
    }

    @Override
    public final void setNumberOfGamesToPlay(final int numberOfGamesToPlay) {
	this.getLogic().getTable().getGame()
		.setNumberOfGamesToPlay(numberOfGamesToPlay);
    }
}
