package swimGame.table;

public interface ITable {
    /**
     * Handle an events submitted by a player
     * 
     * @param action
     * @param data
     */
    void handleTableLogicEvent(TableLogic.Action action, Object data);
}
