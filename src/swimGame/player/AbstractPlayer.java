package swimGame.player;

import java.util.ArrayList;
import java.util.Random;

import swimGame.cards.CardStack;
import swimGame.out.Console;
import swimGame.table.Table;

/**
 * A player playing the game. This abstract class handles the basic player
 * interaction with the table. The real game logic must be implemented by the
 * driving classes.
 * 
 * @see swimGame.table.Table
 * @author Jens Bertram <code@jens-bertram.net>
 * 
 */
public abstract class AbstractPlayer implements IPlayer {
	/**
	 * The name of this player
	 */
	protected String name;
	/**
	 * Holds a list of predefined player names
	 */
	private static ArrayList<String> nameList;
	// one random generator for all players
	protected static Random random;
	/** Cards owned by this player */
	protected CardStack cardStack = null;

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
	public void setCards(final int[] cards) throws Exception {
		if (this.cardStack != null) {
			this.cardStack.clear();
		} else {
			this.cardStack = new CardStack(cards);
		}
		// add cards to our stack
		for (int i = 0; i <= 5;) {
			this.cardStack.addCard(new int[] { cards[i], cards[i + 1] });
			i = i + 2;
		}
		// this.hasCards = true;
		Console.println("<" + this.name + ">", "Recieved cards: "
				+ this.cardStack.toString());
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

	/**
	 * An event was emitted from the game table
	 * 
	 * @param event
	 *            The event as defined by Table
	 * @see swimGame.Table
	 */
	@Override
	public void handleTableEvent(final byte event) {
		switch (event) {
		case Table.EVENT_CARD_DROPPED:
			break;
		case Table.EVENT_CARDS_DROPPED:
			break;
		case Table.EVENT_GAME_START:
			break;
		case Table.EVENT_NEXT_PLAYER:
			break;
		case Table.EVENT_NEXT_ROUND:
			break;
		}
	}
}
