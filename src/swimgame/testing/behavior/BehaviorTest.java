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
    /** How many turns to play? A turn is a number of games that will be played. */
    private static final int NUMBER_OF_TURNS = 10;

    /**
     * Test player with different behavior values
     * 
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
	Debug.debug = true;
	Debug.debugLevel = Debug.INFO;
	// stop the talky output
	Console.blocked = false;
	Console.ask = true;

	final BasicTable table = new BasicTable();
	final GenePool genePool = new GenePool(NUMBER_OF_GENES);
	final ArrayList<Double[]> gameRatings = new ArrayList<Double[]>(
		NUMBER_OF_TURNS);

	DefaultTableController.setPauseAfterRound(false);
	MutablePlayer mutablePlayer = new MutablePlayer(table.getLogic(),
		"Mutable");
	// init the test-player with a random gene
	mutablePlayer.setBias(genePool.getRandomGene().getPlayerBias());
	mutablePlayer.setCardBiasValue(genePool.getRandomGene()
		.getPlayerCardBias());

	// add out test-player
	table.addPlayer(mutablePlayer);
	// add three opponents
	// table.addPlayer(new MutablePlayer(table.getLogic()));
	// table.addPlayer(new MutablePlayer(table.getLogic()));
	table.addPlayer(new MutablePlayer(table.getLogic()));

	// set game
	table.setMaxRoundsToPlay(NUMBER_OF_ROUNDS);
	table.setNumberOfGamesToPlay(NUMBER_OF_GAMES);

	// get length of player names for pretty printing
	int playerNameLength = 0;
	for (IPlayer player : table.getLogic().getTable().getPlayer().asList()) {
	    if (player.toString().length() > playerNameLength) {
		playerNameLength = player.toString().length();
	    }
	}

	byte rank = 0;
	double ratingPoints = 0;
	double gamePoints = 0;
	for (int i = 0; i < NUMBER_OF_TURNS; i++) {
	    System.out.print("=== Turn " + i);
	    // start a game
	    ratingPoints = 0;
	    gamePoints = 0;
	    rank = 0;
	    table.start();

	    System.out.print(" - Rounds played:"
		    + table.getLogic().getTable().getGame().getRound()
			    .getCurrent() + "\n");

	    // game finished, now rate
	    for (IPlayer player : table.getLogic().getTable().getPlayer()
		    .getRanked().keySet()) {
		rank++;

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
		gamePoints = CardStack.calculateValue(player.getCards());

		// debug out
		System.out.println(String.format("Rating: %-"
			+ playerNameLength
			+ "s %15s Rank:%d Rating:%.2f Points:%.2f", player,
			CardStack.cardsToString(player.getCards()), rank,
			ratingPoints, gamePoints));

		// store test-player ratings [place, points]
		gameRatings.add(new Double[] { ratingPoints, gamePoints });
	    }
	}
	// just to show something
	table.getFinalRatings();
    }
}
