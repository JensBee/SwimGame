package swimgame.table;

import java.io.IOException;

import swimgame.out.Console;
import swimgame.out.Debug;
import swimgame.player.IPlayer;
import swimgame.table.logic.Game;
import swimgame.table.logic.TableLogic;
import swimgame.table.logic.TableLogic.Action;

/**
 * Default implementation of a TableController.
 * 
 * @author <a href="mailto:code@jens-bertram.net">Jens Bertram</a>
 * 
 */
public class DefaultTableController extends AbstractTableController {
    /** Should we wait for input after each round? */
    private static boolean pauseAfterRound = false;
    /** The {@link TableLogic} to be controlled by this instance. */
    private final TableLogic tableLogic;
    /** Points for three cards of same type, but different color. */
    public static final double WORTH_THREE_OF_SAME_TYPE = 30.5;
    /** A card stack to use some general functions. */
    private final CardStack cardStack = new CardStack();
    /** Store the current player mainly for output. */
    private IPlayer currentPlayer;

    /** Empty constructor. */
    public DefaultTableController() {
	this.tableLogic = new TableLogic(this);
    }

    /**
     * Set if the table should pause after a round and wait for user input.
     * 
     * @param doPause
     *            If true the table will wait
     */
    public static void setPauseAfterRound(final boolean doPause) {
	DefaultTableController.pauseAfterRound = doPause;
    }

    @Override
    public final void start() {
	Game game = this.tableLogic.getTable().getGame();
	this.tableLogic.initialize();

	for (IPlayer player : this.tableLogic.getTable().getPlayer().asList()) {
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

	while (game.hasNext()) {
	    try {
		game.next();
	    } catch (Exception e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	    }

	    while (!this.tableLogic.getTable().getGame().isFinished()
		    && game.getRound().hasNext()) {

		Debug.println(String.format("*** Game %d ** Round %d ***",
			game.current(), game.getRound().getCurrent()));

		this.currentPlayer = this.tableLogic.getTable().getGame()
			.getRound().nextPlayer();
		if (game.getRound().isClosedBy(this.currentPlayer)) {
		    break;
		}

		this.logWriter.write("Cards: "
			+ this.tableLogic.getTable().getCardStack().toString());

		this.logWriter.write("It's your turn %s", this.currentPlayer);
		this.currentPlayer.doMove(this.tableLogic.getTable()
			.getCardStack().getCards());

		while (!this.tableLogic.getTable().getPlayer().isTurnFinished()) {
		    // player actions are handled via events
		}

		if (!this.tableLogic.getTable().getPlayer().getTookAction()) {
		    this.logWriter.write("%s skipped!",
			    this.currentPlayer.toString());
		}
	    }

	    if (game.getRound().isFinished()) {
		this.logWriter
			.write("Sorry players, I'll stop here! You reached the maximum of %d rounds without anyone winning.",
				game.getRound().getMaxLength());
	    }
	    this.generateRating();

	    if (pauseAfterRound && game.hasNext()) {
		Console.println("\nPress return to start the next game...");
		try {
		    System.in.read();
		} catch (IOException e) {
		    e.printStackTrace();
		}
	    }
	}
    }

    /** Generate a simple rating for all players. */
    private void generateRating() {
	Console.nl();
	byte rank;

	this.logWriter.write("Player game-rating:");
	for (IPlayer player : this.tableLogic.getTable().getPlayer().asList()) {
	    final byte[] playerCards = player.getCards();
	    final String playerName = (this.tableLogic.getTable().getGame()
		    .getRound().isClosedBy(player)) ? "* " + player.toString()
		    : player.toString();
	    if (playerCards == null) {
		this.logWriter.write(" %s gave us no card information",
			playerName);
	    } else {
		CardStack playerCardStack = new CardStack(playerCards);
		this.logWriter.write(" %s's cards: %s value: %.0f", playerName,
			playerCardStack.toString(),
			CardStack.calculateValue(playerCards));
	    }
	}

	this.logWriter.write("Player overall-rating:");
	rank = 1;
	for (IPlayer player : this.tableLogic.getTable().getPlayer()
		.getRanked().keySet()) {
	    this.logWriter.write(String.format("%d. %s with %.0f", rank,
		    player.toString(), this.tableLogic.getTable().getPlayer()
			    .getPoints(player)));
	    rank++;
	}
    }

    @Override
    public final TableLogic getLogic() {
	return this.tableLogic;
    }

    @Override
    public final void handleTableLogicEvent(final Action action,
	    final Object data) {
	switch (action) {
	case DROP_CARD:
	    this.logWriter.player("dropped %s",
		    this.cardStack.cardToString((byte) data));
	    break;
	case DROP_CARDSTACK_INITIAL:
	    this.logWriter.player("drops the initial card set: %s",
		    new CardStack((byte[]) data).toString());
	    break;
	case END_CALL:
	    this.logWriter.player("is closing. Last call!");
	    break;
	case PICK_CARD:
	    this.logWriter.player("picked card %s",
		    this.cardStack.cardToString((byte) data));
	    break;
	case INITIAL_CARDSTACK_PICKED:
	    this.logWriter.player("picked the initial card set");
	    break;
	default:
	    break;
	}
    }
}