package swimGame;

import swimGame.out.Console;
import swimGame.out.Debug;
import swimGame.player.HumanPlayer;
import swimGame.table.DefaultTable;
import swimGame.table.TableLogic;

public class SwimGame {
    private static final double version = 0.1;
    private static boolean humanPlayer = false;

    /** General program termination on errors */
    private static void exitWithError(final Exception e) {
	e.printStackTrace();
	System.exit(1);
    }

    /** General program termination on errors */
    private static void exitWithError(String message) {
	System.err.println(message);
	System.exit(1);
    }

    private static String formatHelpString(String option, String description) {
	String format = "%-15s   %-60s";
	return String.format(format, option, description);
    }

    private static void printHelp() {
	Console.println("Your help to swim right:");
	Console.println(SwimGame.formatHelpString("--debug",
		"print (a lot) of debugging messages"));
	Console.println(SwimGame.formatHelpString("--max-rounds n",
		"maximum number of rounds to play before intercepting"));
	Console.println(SwimGame.formatHelpString("--paused",
		"add n players (2 to 9 players are allowed)"));
	Console.println(SwimGame.formatHelpString("--player n",
		"add n players (2 to 9 players are allowed)"));
	Console.println(SwimGame.formatHelpString("--human",
		"add one slot for a human player)"));
	Console.println(SwimGame.formatHelpString("--plays",
		"play n rounds of the game"));
	Console.nl();
	Console.println(SwimGame.formatHelpString("--help", "show this help"));
    }

    /**
     * Main
     */
    public static void main(final String[] args) {
	int numberOfPLayers = 3;
	// game playing table
	DefaultTable table;
	// default rounds to play
	int numberOfGamesToPlay = 1;
	int maxRoundsToPlay = 1;

	Console.println("SWIMMING.GAME.O°o°O°o.\nv" + SwimGame.version
		+ ", 2011 Jens Bertram <code@jens-bertram.net>\n");

	int i = 0;
	String arg;
	while ((i < args.length) && args[i].startsWith("-")) {
	    arg = args[i++];

	    switch (arg) {
	    case "--help":
		SwimGame.printHelp();
		System.exit(0);
		break;
	    case "--player":
		if (i < args.length) {
		    numberOfPLayers = Integer.parseInt(args[i++]);
		} else {
		    SwimGame.exitWithError("No value for parameter --player given.");
		}
		break;
	    case "--debug":
		Debug.debug = true;
		break;
	    case "--max-rounds":
		if (i < args.length) {
		    maxRoundsToPlay = Integer.parseInt(args[i++]);
		} else {
		    SwimGame.exitWithError("No value for parameter --max-rounds given.");
		}
		break;
	    case "--plays":
		if (i < args.length) {
		    numberOfGamesToPlay = Integer.parseInt(args[i++]);
		} else {
		    SwimGame.exitWithError("No value for parameter --plays given.");
		}
		break;
	    case "--noask":
		Console.ask = false;
		break;
	    case "--human":
		humanPlayer = true;
		break;
	    case "--paused":
		DefaultTable.pauseAfterRound = true;
		break;
	    }
	}

	// sum up configuration
	if (Debug.debug) {
	    Console.println("INFO: Debugging messages are turned on");
	}
	if (Console.ask == false) {
	    Console.println("INFO: Won't ask any questions");
	}
	int gamePlayer = numberOfPLayers + ((humanPlayer) ? 1 : 0);
	Console.println(String.format(
		"INFO: Game will be played with %d players", gamePlayer));
	// TODO: get max rounds
	// Console.println(String
	// .format("INFO: A maximum of %d rounds will be played",
	// Table.MAX_ROUNDS));
	Console.nl();

	// table = new Table(numberOfRounds);
	// TODO: set max rounds
	table = new DefaultTable();
	table.setMaxRoundsToPlay(maxRoundsToPlay);
	table.setNumberOfGamesToPlay(numberOfGamesToPlay);

	// try add some players
	try {
	    if (humanPlayer) {
		if (numberOfPLayers == TableLogic.MAX_PLAYER) {
		    Console.println("WARN: Removed one player to get a slot for the human player");
		    table.addPlayers(numberOfPLayers - 1);
		}
		table.addPlayers(numberOfPLayers);
		table.addPlayer(new HumanPlayer(table.getLogic()));
	    } else {
		table.addPlayers(numberOfPLayers);
	    }
	} catch (final Exception e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}

	try {
	    table.start();
	} catch (final Exception e) {
	    SwimGame.exitWithError(e);
	}
    }
}
