package swimGame.table;

import java.util.Formatter;

import swimGame.out.Console;
import swimGame.player.IPlayer;

public abstract class AbstractTable implements ITable {
    // name for nice console out
    protected String className = "Table";
    // logic for table interaction
    protected final TableLogic tableLogic;
    protected final LogWriter logWriter = new LogWriter();

    protected AbstractTable() {
	this.tableLogic = new TableLogic(this);
    }

    /**
     * Ease handling of lag entries
     * 
     * @author Jens Bertram <code@jens-bertram.net>
     * 
     */
    protected class LogWriter {
	private String getPlayerName(final IPlayer player) {
	    return String.format("<%s> ", player.toString());
	}

	private String getPlayerName() {
	    return this.getPlayerName(AbstractTable.this.tableLogic.game.round
		    .getCurrentPlayer());
	}

	protected void player(final String format, final Object... args) {
	    Console.println(AbstractTable.this.className, this.getPlayerName()
		    + (new Formatter().format(format, args).toString()));
	}

	protected void player(final String message) {
	    this.player(message, (Object[]) null);
	}

	protected void player(IPlayer player, String format,
		final Object... args) {
	    Console.println(
		    AbstractTable.this.className,
		    this.getPlayerName(player)
			    + (new Formatter().format(format, args).toString()));
	}

	protected void player(IPlayer player, String message) {
	    this.player(player, message, (Object[]) null);
	}

	protected void write(String format, Object... args) {
	    Console.println(AbstractTable.this.className, new Formatter()
		    .format(format, args).toString());
	}

	protected void write(String message) {
	    this.write(message, (Object[]) null);
	}
    }

    @Override
    public void addPlayer(IPlayer player) throws Exception {
	this.tableLogic.table.addPlayer(player);
    }

    @Override
    public void addPlayers(final int amount) throws Exception {
	this.tableLogic.table.addPlayers(amount);
    }

    /** Get the logic for this table controller */
    @Override
    public TableLogic getLogic() {
	return this.tableLogic;
    }

    @Override
    public void start() {
	this.tableLogic.initialize();
    }

    @Override
    public void setMaxRoundsToPlay(int maxRoundsToPlay) {
	this.tableLogic.game.round.setMaxLength(maxRoundsToPlay);
    }

    @Override
    public void setNumberOfGamesToPlay(int numberOfGamesToPlay) {
	this.tableLogic.game.setNumberOfGamesToPlay(numberOfGamesToPlay);
    }
}
