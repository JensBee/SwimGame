package swimgame.table;

import swimgame.player.IPlayer;

public interface ITable {
    /**
     * Handle an events submitted by a player
     * 
     * @param action
     * @param data
     */
    void handleTableLogicEvent(TableLogic.Action action, Object data);

    /**
     * @see TableLogic.Game#setMaxRoundsToPlay(int)
     * @param maxRoundsToPlay
     */
    void setMaxRoundsToPlay(int maxRoundsToPlay);

    /**
     * @see TableLogic.Game#setNumberOfGamesToPlay(int)
     * @param numberOfGamesToPlay
     */
    void setNumberOfGamesToPlay(int numberOfGamesToPlay);

    /** Get the game started */
    void start();

    /** Get the logic for this table controller */
    TableLogic getLogic();

    /**
     * @see TableLogic.Table#addPlayers(int)
     */
    void addPlayers(final int amount) throws Exception;

    /**
     * @see TableLogic.Table#addPlayer(IPlayer)
     */
    void addPlayer(IPlayer player) throws Exception;
}
