package swimGame.testing;

import java.io.IOException;

import swimGame.out.Console;
import swimGame.out.Debug;
import swimGame.player.DefaultPlayer;
import swimGame.player.IPlayer;
import swimGame.table.AbstractTable;
import swimGame.table.CardStack;
import swimGame.table.ITable;
import swimGame.table.TableLogic;
import swimGame.table.TableLogic.Action;

public class BehaviorTest {
    // store the number of won games (mutated player first)
    private final int[] wonGames = new int[4];
    // store the points (mutated player first)
    private final double[] points = new double[4];

    /**
     * Test player with different behavior values
     * 
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
	// not nice, but sufficient here
	class BasicTable extends AbstractTable {
	    BasicTable() {
		super();
	    }

	    @Override
	    public void handleTableLogicEvent(Action action, Object data) {
		// TODO Auto-generated method stub

	    }
	}

	Debug.debug = true;
	ITable table = new BasicTable();
	TableLogic tableLogic = table.getLogic();

	// add our mutable player
	table.addPlayer(new DefaultPlayer(tableLogic));
	// add a number of default behavior opponents
	table.addPlayers(3);

	// table card stack
	CardStack cardStack = new CardStack();

	table.start();
	tableLogic.table.close();

	IPlayer currentPlayer = null;
	while (tableLogic.game.hasNext()) {
	    tableLogic.game.next();

	    while ((tableLogic.game.isFinished() == false)
		    && tableLogic.game.round.hasNext()) {

		Debug.println(String.format("*** Game %d ** Round %d ***",
			tableLogic.game.current(),
			tableLogic.game.round.current()));

		currentPlayer = tableLogic.game.round.nextPlayer();
		if (tableLogic.game.round.hasClosed(currentPlayer)) {
		    break;
		}

		// this.logWriter.write("Cards: "
		// + this.tableLogic.table.cardStackTable.toString());
		tableLogic.player.hasTakenAnAction = false;
		currentPlayer
			.doMove(tableLogic.table.cardStackTable.getCards());

		while (tableLogic.player.moveFinished != true) {
		    // player actions are handled via events
		}

		if (tableLogic.player.hasTakenAnAction == false) {
		    // logWriter.write("%s skipped!", currentPlayer.toString());
		}
	    }

	    tableLogic.player.fireEvent(TableLogic.Event.GAME_FINISHED);

	    if (tableLogic.game.round.isFinished()) {
		// logWriter
		// .write("Sorry players, I'll stop here! You reached the maximum of %d rounds without anyone winning.",
		// tableLogic.game.getMaxRoundsToPlay());
	    }
	    // generateRating();

	    if (tableLogic.game.hasNext()) {
		Console.println("\nPress return to start the next game...");
		try {
		    System.in.read();
		} catch (IOException e) {
		    e.printStackTrace();
		}
	    }
	}

    }

}
