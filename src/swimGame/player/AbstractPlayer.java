package swimGame.player;

import java.util.ArrayList;
import java.util.Random;

import swimGame.out.Console;
import swimGame.out.Debug;
import swimGame.table.CardStack;

/**
 * A player playing the game. This abstract class handles the basic player
 * interaction with the table. The real game logic must be implemented by the
 * driving classes.
 * 
 * @see swimGame.table.Table
 * @author Jens Bertram <code@jens-bertram.net>
 * 
 */
abstract class AbstractPlayer implements IPlayer {
	/**
	 * The name of this player
	 */
	private final String name;
	/**
	 * Holds a list of predefined player names
	 */
	private static ArrayList<String> nameList;
	// one random generator for all players
	private static Random random;
	/** Cards owned by this player */
	protected CardStack cardStack = null;

	/**
	 * Log a message to the console
	 * 
	 * @param message
	 */
	protected void log(String message) {
		Console.println(this, message);
	}

	/**
	 * Initialize the predefined player names
	 */
	private void initNames() {
		if (AbstractPlayer.random == null) {
			AbstractPlayer.random = new Random();
		}
		if (AbstractPlayer.nameList == null) {
			// we have nine players at max
			AbstractPlayer.nameList = new ArrayList<String>(9);
			AbstractPlayer.nameList.add("Bob");
			AbstractPlayer.nameList.add("Alice");
			AbstractPlayer.nameList.add("Carol");
			AbstractPlayer.nameList.add("Dave");
			AbstractPlayer.nameList.add("Ted");
			AbstractPlayer.nameList.add("Eve");
			AbstractPlayer.nameList.add("Oscar");
			AbstractPlayer.nameList.add("Peggy");
			AbstractPlayer.nameList.add("Victor");
		}
	}

	/**
	 * Empty constructor
	 */
	public AbstractPlayer() {
		this.initNames();
		// get a random name
		this.name = AbstractPlayer.nameList.remove(AbstractPlayer.random
				.nextInt(AbstractPlayer.nameList.size()));
	}

	/**
	 * Constructor
	 * 
	 * @param name
	 *            The name of this player
	 */
	public AbstractPlayer(final String name) {
		this.name = name;
	}

	/**
	 * Deal out cards to this player
	 * 
	 * @throws Exception
	 */
	@Override
	public void setCards(final byte[] cards) {
		this.cardStack = new CardStack(cards);
		Debug.print(this, "Recieved cards: " + this.cardStack.toString() + "\n");
	}

	/**
	 * Get the name of this player
	 * 
	 * @return The name of this player
	 */
	@Override
	public String toString() {
		return this.name;
	}

	/**
	 * Decide if we like to pickup another initial card set. This is only
	 * possible, if we start the game.
	 * 
	 * @return False, if we want a new set of cards
	 */
	@Override
	public boolean keepCardSet() {
		return true;
	}
}
