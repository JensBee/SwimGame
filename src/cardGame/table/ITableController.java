package cardGame.table;

import cardGame.player.IPlayer;
import swimgame.table.logic.TableLogic;
import swimgame.table.logic.TableLogicException;

/**
 * This class controls the table game and interacts with the user running the
 * game.
 * 
 * @author <a href="mailto:code@jens-bertram.net">Jens Bertram</a>
 * 
 */
public interface ITableController {
    /**
     * Handle an events submitted by a player.
     * 
     * @param action
     *            {@link TableLogic.Action} emitted by a player
     * @param data
     *            Data associated with the {@link TableLogic.Action}
     */
    void handleTableLogicEvent(final TableLogic.Action action, final Object data);

    /**
     * @see TableLogic.Game#setMaxRoundsToPlay(int)
     * @param maxRoundsToPlay
     *            Maximum length of a rounds without anyone winning
     */
    void setMaxRoundsToPlay(final int maxRoundsToPlay);

    /**
     * @see TableLogic.Game#setNumberOfGamesToPlay(int)
     * @param numberOfGamesToPlay
     *            Amount of games to play in turn
     */
    void setNumberOfGamesToPlay(final int numberOfGamesToPlay);

    /**
     * Get the game started. TODO: tell what steps need to be taken here
     */
    void start();

    /**
     * Get the {@link TableLogic} for this {@link ITableController} instance.
     * 
     * @return {@link TableLogic} for this {@link ITableController} instance
     */
    TableLogic getLogic();

    /**
     * @see TableLogic.Table#addPlayers(int)
     * @param amount
     *            Amount of players to add
     * @throws TableLogicException
     *             Might be thrown, if adding of players failed
     */
    void addPlayers(final int amount) throws TableLogicException;

    /**
     * @see TableLogic.Table#addPlayer(IPlayer)
     * @param player
     *            {@link IPlayer} to add to the table
     * @throws TableLogicException
     *             Might be thrown, if adding a player failes
     */
    void addPlayer(final IPlayer player) throws TableLogicException;
}
