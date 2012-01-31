package swimgame.player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import swimgame.out.Console;
import swimgame.out.Debug;
import swimgame.table.CardStack;
import swimgame.table.logic.TableLogic;
import swimgame.util.Util;

/**
 * A player playing the game. This abstract class handles the basic player
 * interaction with the table. The real game logic must be implemented by the
 * driving classes.
 * 
 * @see swimgame.table.DefaultTableController
 * @author <a href="mailto:code@jens-bertram.net">Jens Bertram</a>
 * 
 */
abstract class AbstractPlayer implements IPlayer {
    /** List of predefined player names. */
    private static List<String> playerNames = new ArrayList<String>(
	    Arrays.asList("Bob", "Alice", "Carol", "Dave", "Ted", "Eve",
		    "Oscar", "Peggy", "Victor"));

    /**
     * Log a message to the console.
     * 
     * @param message
     *            The message to log
     */
    void log(final String message) {
	Console.println(this, message);
    }

    /**
     * Get a random player name.
     * 
     * @return Player name chosen from a predefined set
     */
    String getRandomName() {
	return AbstractPlayer.playerNames.remove(Util.getRandomInt(playerNames
		.size() - 1));
    }

    @Override
    public void setCards(final byte[] cards) {
	this.setCardStack(new CardStack(cards));
	Debug.printf(Debug.INFO, this, "Recieved cards: %s\n",
		this.getCardStack());
    }

    /**
     * Get the {@link CardStack} owned by this player instance.
     * 
     * @return {@link CardStack} owned by this player instance
     */
    abstract CardStack getCardStack();

    /**
     * Set the {@link CardStack} owned by this player instance.
     * 
     * @param newCardStack
     *            New {@link CardStack} to set for this player
     */
    abstract void setCardStack(final CardStack newCardStack);

    /**
     * Return the name of this player.
     * 
     * @return The name of this player
     */
    @Override
    public String toString() {
	return this.getName();
    }

    @Override
    public byte[] getCards() {
	if (this.getCardStack() != null) {
	    return this.getCardStack().getCards();
	} else {
	    // TODO: sport an error notice
	    return new byte[] {};
	}
    }

    /** Set the current game as being closed.. */
    abstract void setGameClosed();

    @Override
    public void handleTableEvent(final TableLogic.Event event, final Object data) {
	switch (event) {
	case GAME_CLOSED:
	    this.setGameClosed();
	    break;
	default:
	    break;
	}
    }
}
