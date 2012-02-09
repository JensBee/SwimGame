package cardGame.logic;

import cardGame.table.GameTable;

/** General card game structure. */
public interface CardGame {
    /**
     * Creates a new {@link CardGame} instance.
     * 
     * @param table
     *            {@link GameTable} the {@link CardGame} is happening at
     */
    void setTable(final GameTable table);

    /**
     * How many games should be played in turn?
     * 
     * @param numberOfGamesToPlay
     *            Amount of games to play in turn
     */
    void setNumberOfGamesToPlay(final int numberOfGamesToPlay);

    /**
     * Get the number of games to play in turn.
     * 
     * @return Number of games to play in turn
     */
    int getNumberOfGamesToPlay();
}
