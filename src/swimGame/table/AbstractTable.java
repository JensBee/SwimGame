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
	private String getPlayerName(IPlayer player) {
	    return String.format("<%s> ", player.toString());
	}

	private String getPlayerName() {
	    return this.getPlayerName(AbstractTable.this.tableLogic.game.round
		    .currentPlayer());
	}

	protected void player(String format, Object... args) {
	    Console.println(AbstractTable.this.className, this.getPlayerName()
		    + (new Formatter().format(format, args).toString()));
	}

	protected void player(String message) {
	    this.player(message, (Object[]) null);
	}

	protected void player(IPlayer player, String format, Object... args) {
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

    /**
     * @see TableLogic.Table#addPlayer(IPlayer)
     */
    public void addPlayer(IPlayer player) throws Exception {
	this.tableLogic.table.addPlayer(player);
    }

    /**
     * @see TableLogic.Table#addPlayers(int)
     */
    public void addPlayers(final int amount) throws Exception {
	this.tableLogic.table.addPlayers(amount);
    }

    /** Get the logic for this table controller */
    public TableLogic getLogic() {
	return this.tableLogic;
    }

    /** Get the game started */
    public void start() {
	this.tableLogic.player.fireEvent(TableLogic.Event.GAME_START);
    }

    /**
     * @see TableLogic.Game#setMaxRoundsToPlay(int)
     * @param maxRoundsToPlay
     */
    public void setMaxRoundsToPlay(int maxRoundsToPlay) {
	this.tableLogic.game.setMaxRoundsToPlay(maxRoundsToPlay);
    }

    /**
     * @see TableLogic.Game#setNumberOfGamesToPlay(int)
     * @param numberOfGamesToPlay
     */
    public void setNumberOfGamesToPlay(int numberOfGamesToPlay) {
	this.tableLogic.game.setNumberOfGamesToPlay(numberOfGamesToPlay);
    }
}
