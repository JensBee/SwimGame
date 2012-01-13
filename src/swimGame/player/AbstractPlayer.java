package swimGame.player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import swimGame.out.Console;
import swimGame.out.Debug;
import swimGame.table.CardStack;
import swimGame.table.TableLogic;

/**
 * A player playing the game. This abstract class handles the basic player
 * interaction with the table. The real game logic must be implemented by the
 * driving classes.
 * 
 * @see swimGame.table.DefaultTable
 * @author Jens Bertram <code@jens-bertram.net>
 * 
 */
abstract class AbstractPlayer implements IPlayer {
    /** The name of this player */
    protected String name;
    /** Holds a list of predefined player names */
    private static List<String> nameList;
    /** Reference to the table we're playing on */
    protected TableLogic tableLogic;
    // one random generator for all players
    private static Random random;
    /** Cards owned by this player */
    protected CardStack cardStack = null;
    // game over?
    protected boolean gameIsFinished = false;
    // game close called?
    protected boolean gameIsClosed = false;

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
	    AbstractPlayer.nameList = new ArrayList<String>(Arrays.asList(
		    "Bob", "Alice", "Carol", "Dave", "Ted", "Eve", "Oscar",
		    "Peggy", "Victor"));
	}
    }

    /**
     * Empty constructor
     */
    public AbstractPlayer(TableLogic tableLogic) {
	this.tableLogic = tableLogic;
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
    public AbstractPlayer(TableLogic tableLogic, final String name) {
	this.tableLogic = tableLogic;
	this.name = name;
    }

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

    @Override
    public byte[] getCards() {
	return this.cardStack.getCards();
    }

    @Override
    public void handleTableEvent(TableLogic.Event event, Object data) {
	switch (event) {
	case GAME_CLOSED:
	    this.gameIsClosed = true;
	    break;
	case GAME_FINISHED:
	    this.gameIsFinished = true;
	    break;
	}
    }
}
