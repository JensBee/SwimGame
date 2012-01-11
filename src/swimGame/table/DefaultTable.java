package swimGame.table;

import java.io.IOException;

import swimGame.out.Console;
import swimGame.out.Debug;
import swimGame.player.IPlayer;
import swimGame.table.TableLogic.Action;

public class DefaultTable extends AbstractTable {
    // should we wait for input after each round?
    public static boolean pauseAfterRound = false;
    // points for three cards of same type, but different color
    public static final double WORTH_THREE_OF_SAME_TYPE = 30.5;
    // a card stack to use some general functions
    private final CardStack cardStack = new CardStack();
    // store the current player mainly for output
    IPlayer currentPlayer;

    @Override
    public void start() {
	super.start();
	this.tableLogic.table.close();

	for (IPlayer player : this.tableLogic.table.getPlayer()) {
	    this.logWriter.write("%s joined the table", player.toString());
	}
	if (Console.ask) {
	    Console.println("\nPress return to start...");
	    try {
		System.in.read();
	    } catch (IOException e) {
		e.printStackTrace();
	    }
	}

	this.currentPlayer = null;
	while (this.tableLogic.game.hasNext()) {
	    this.tableLogic.game.next();

	    while ((this.tableLogic.game.isFinished() == false)
		    && this.tableLogic.game.round.hasNext()) {

		Debug.println(String.format("*** Game %d ** Round %d ***",
			this.tableLogic.game.current(),
			this.tableLogic.game.round.current()));

		this.currentPlayer = this.tableLogic.game.round.nextPlayer();
		if (this.tableLogic.game.round.hasClosed(this.currentPlayer)) {
		    break;
		}

		this.logWriter.write("Cards: "
			+ this.tableLogic.table.cardStackTable.toString());
		this.tableLogic.player.hasTakenAnAction = false;
		this.currentPlayer.doMove(this.tableLogic.table.cardStackTable
			.getCards());

		while (this.tableLogic.player.moveFinished != true) {
		    // player actions are handled via events
		}

		if (this.tableLogic.player.hasTakenAnAction == false) {
		    this.logWriter.write("%s skipped!",
			    this.currentPlayer.toString());
		}
	    }

	    this.tableLogic.player.fireEvent(TableLogic.Event.GAME_FINISHED);

	    if (this.tableLogic.game.round.isFinished()) {
		this.logWriter
			.write("Sorry players, I'll stop here! You reached the maximum of %d rounds without anyone winning.",
				this.tableLogic.game.getMaxRoundsToPlay());
	    }
	    this.generateRating();

	    if (pauseAfterRound && this.tableLogic.game.hasNext()) {
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
	for (IPlayer player : this.tableLogic.player.list) {
	    byte[] playerCards = player.getCards();
	    String playerName = (this.tableLogic.game.round.hasClosed(player)) ? "* "
		    + player.toString()
		    : player.toString();
	    if (playerCards == null) {
		this.logWriter.write(" %s gave us no card information",
			playerName);
	    } else {
		CardStack playerCardStack = new CardStack(playerCards);
		this.tableLogic.player.addPoints(player,
			playerCardStack.getValue());
		this.logWriter.write(
			" %s's cards: %s value: %.0f overall: %.0f",
			playerName, playerCardStack.toString(),
			playerCardStack.getValue(),
			this.tableLogic.player.getPoints(player));
	    }
	}
    }

    @Override
    public void handleTableLogicEvent(Action action, Object data) {
	switch (action) {
	case DROP_CARD:
	    this.logWriter.write("%s dropped %s", this.currentPlayer,
		    this.cardStack.card.toString((byte) data));
	    break;
	case END_CALL:
	    this.logWriter.player("is closing. Last call!");
	    break;
	case PICK_CARD:
	    this.logWriter.player("picked card %s",
		    this.cardStack.card.toString((byte) data));
	    break;
	}
    }
}