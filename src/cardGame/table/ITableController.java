package cardGame.table;

import cardGame.event.EventReceiver;
import cardGame.player.CardPlayer;

/**
 * A TableController is the actual user interface to the game. All interaction
 * with the game will go through this class.
 */
public interface ITableController extends EventReceiver {
    /**
     * Get the game started. TODO: tell what steps need to be taken here
     */
    void start();

    /**
     * @see TableLogic.CardGame#setNumberOfGamesToPlay(int)
     * @param numberOfGamesToPlay
     *            Amount of games to play in turn
     */
    void setNumberOfGamesToPlay(final int numberOfGamesToPlay);

    /**
     * Add a single player to the table.
     * 
     * @param newPlayer
     *            The {@link CardPlayer} to add
     * @throws Exception
     *             Thrown if table is full or closed (game has already begun)
     */
    void addPlayer(final CardPlayer newPlayer) throws Exception;
}
