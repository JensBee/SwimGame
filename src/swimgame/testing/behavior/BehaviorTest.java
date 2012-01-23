package swimgame.testing.behavior;

import java.util.ArrayList;

import swimgame.out.Console;
import swimgame.out.Debug;
import swimgame.player.IPlayer;
import swimgame.table.CardStack;
import swimgame.table.DefaultTableController;

public class BehaviorTest {
    /** How many genes should we create? */
    private static final int NUMBER_OF_GENES = 10;
    /** How many rounds to play per game in maximum? */
    private static final int NUMBER_OF_ROUNDS = 40;
    /** How many games to play? */
    private static final int NUMBER_OF_GAMES = 10;
    /** How many turns to play? A turn is anumber of games that will be played. */
    private static final int NUMBER_OF_TURNS = 10;

    /**
     * Test player with different behavior values
     * 
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
	Debug.debug = false;
	// stop the talky output
	Console.blocked = true;
	Console.ask = false;

	final BasicTable table = new BasicTable();
	final GenePool genePool = new GenePool(NUMBER_OF_GENES);
	final ArrayList<Double[]> gameRatings = new ArrayList<Double[]>(
		NUMBER_OF_TURNS);

	DefaultTableController.setPauseAfterRound(false);
	MutablePlayer mutablePlayer = new MutablePlayer(table.getLogic(),
		"Mutable");
	// init the test-player with a random gene
	mutablePlayer.setGene(genePool.getRandomGene());

	// add out test-player
	table.addPlayer(mutablePlayer);
	// add three opponents
	table.addPlayer(new MutablePlayer(table.getLogic()));
	table.addPlayer(new MutablePlayer(table.getLogic()));
	table.addPlayer(new MutablePlayer(table.getLogic()));

	// set game
	table.setMaxRoundsToPlay(NUMBER_OF_ROUNDS);
	table.setNumberOfGamesToPlay(NUMBER_OF_GAMES);

	byte rank = 0;
	double ratingPoints = 0;
	double gamePoints = 0;
	for (int i = 0; i < NUMBER_OF_TURNS; i++) {
	    System.out.println("=== Turn " + i);
	    // start a game
	    ratingPoints = 0;
	    gamePoints = 0;
	    rank = 0;
	    table.start();

	    // game finished, now rate
	    for (IPlayer player : table.getLogic().getTable().getPlayer()
		    .getRanked().keySet()) {
		rank++;
		if (!player.equals(mutablePlayer)) {
		    continue;
		}
		// how much points should we earn?
		switch (rank) {
		case 1:
		    ratingPoints = 3;
		    break;
		case 2:
		    ratingPoints = 2;
		    break;
		case 3:
		    ratingPoints = 1;
		    break;
		default:
		    ratingPoints = 0;
		    break;
		}
		gamePoints = new CardStack(player.getCards()).getValue();
		// store test-player ratings [place, points]
		System.out.println("C:"
			+ CardStack.cardsToString(player.getCards()) + " R: "
			+ rank + " r:" + ratingPoints + " g:" + gamePoints);
		gameRatings.add(new Double[] { ratingPoints, gamePoints });

		// we're finished
		break;
	    }
	}
	// just to show something
	table.getFinalRatings();
    }
}
