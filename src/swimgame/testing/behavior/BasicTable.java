package swimgame.testing.behavior;

import java.util.HashMap;
import java.util.Map;

import swimgame.player.IPlayer;
import swimgame.table.CardStack;
import swimgame.table.DefaultTableController;
import swimgame.table.logic.TableLogic;

/**
 * A table implementing extra functions for excessive player ratings.
 * 
 * @author <a href="mailto:code@jens-bertram.net">Jens Bertram</a>
 * 
 */
class BasicTable extends DefaultTableController {
    /** Holds a list with genes and their won games. */
    private final Map<Integer, PlayerGene> winList = new HashMap<Integer, PlayerGene>();
    /** The {@link TableLogic} to be controlled by this instance. */
    private final TableLogic tableLogic;

    /** Empty constructor. */
    BasicTable() {
	super();
	this.tableLogic = new TableLogic(this);
    }

    /** Create a final rating after a number of games. */
    protected void getFinalRatings() {
	byte rank = 1;
	for (IPlayer player : this.tableLogic.getTable().getPlayer()
		.getRanked().keySet()) {
	    System.out.println(String.format("%d. %s with %.0f", rank,
		    player.toString(), this.tableLogic.getTable().getPlayer()
			    .getPoints(player)));
	    rank++;
	}
    }

    /** Generate the user ranking after a game-round. */
    protected void generateRanking() {
	byte rank = 0;

	System.out.println("Player game-rating:");
	for (IPlayer player : this.tableLogic.getTable().getPlayer().asList()) {
	    final byte[] playerCards = player.getCards();

	    if (rank == 1) {
		// player.hasWon();
	    }

	    if (playerCards == null) {
		// TODO: error out!
	    } else {
		CardStack playerCardStack = new CardStack(playerCards);
		System.out.printf(" %s's cards: %s value: %.0f\n", player,
			playerCardStack.toString(), playerCardStack.getValue());
	    }
	}
    }
}