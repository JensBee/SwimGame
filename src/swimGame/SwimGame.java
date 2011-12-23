package swimGame;

import swimGame.out.Debug;
import swimGame.table.Table;

public class SwimGame {
	// The game playing table
	private static Table table;

	/**
	 * General program termination on errors
	 */
	public static void exitWithError(final Exception e) {
		e.printStackTrace();
		System.exit(1);
	}

	/**
	 * @param args
	 */
	public static void main(final String[] args) {
		// toggle debug messages
		Debug.debug = true;

		// table
		SwimGame.table = new Table();

		// try add some players
		try {
			SwimGame.table.addPlayers(9);
		} catch (final Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			SwimGame.table.startGame();
		} catch (final Exception e) {
			SwimGame.exitWithError(e);
		}
	}

}
