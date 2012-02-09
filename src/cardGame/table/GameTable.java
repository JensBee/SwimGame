package cardGame.table;

import java.util.List;

import cardGame.card.CardDeck;
import cardGame.player.CardPlayer;
import cardGame.util.LoopIterator;

public interface GameTable {
    /**
     * Add a single player to the table. This is possible up to a maximum of
     * {@value TableLogic#MAX_PLAYER}
     * 
     * @param newPlayer
     *            The {@link CardPlayer} to add
     * @throws Exception
     *             If adding the player has failed
     */
    void addPlayer(final CardPlayer newPlayer) throws Exception;

    /**
     * Try to remove a player from the table. No checking if class is allowed to
     * do so is involved here.
     * 
     * @param playerToRemove
     *            Player to remove
     * @throws Exception
     *             If removing fails
     */
    void removePlayer(final CardPlayer playerToRemove) throws Exception;

    /**
     * Close the table. This should be the last step prior to starting the game.
     * A closed table does not allow any player to join. A closed table cannot
     * be made open again.
     */
    void close();

    /**
     * Get an extended (looping) iterator over all players currently at the
     * table.
     * 
     * @return {@link LoopIterator} over all players currently at the table
     */
    LoopIterator<CardPlayer> playerLoopIterator();

    /**
     * Deal out a random card from the stack.
     * 
     * @return Random card from the stack
     */
    CardDeck.Card dealCard();

    /**
     * Get the number of players on this table.
     * 
     * @return Number of players
     */
    int numberOfPlayers();

    /**
     * Resets the tables state to begin a new game. This must be called for
     * every new game following the first one.
     */
    void startNewGame();

    /**
     * Player interaction with the table.
     * 
     * @param player
     *            The player who's interacting
     * @param action
     *            The action to perform
     * @param data
     *            Data associated with the action
     * @return An <code>Object</code> indicating the success of the interaction
     */
    Object interact(CardPlayer player, Enum<? extends TableAction> action,
	    Object data);

    /**
     * Get the list of player attending to this table.
     * 
     * @return Players currently attending to the table
     */
    List<CardPlayer> player();
}
