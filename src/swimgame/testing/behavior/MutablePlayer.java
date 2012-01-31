package swimgame.testing.behavior;

import swimgame.player.DefaultPlayer;
import swimgame.table.logic.TableLogic;

public class MutablePlayer extends DefaultPlayer {
    /**
     * Constructor.
     * 
     * @param tableLogic
     *            The logic for this player
     * @param name
     *            The name of this player
     */
    public MutablePlayer(final TableLogic tableLogic, final String name) {
	super(tableLogic, name);
    }

    /**
     * Constructor without given player name
     * 
     * @param tableLogic
     *            The logic for this player
     */
    public MutablePlayer(final TableLogic tableLogic) {
	super(tableLogic);
    }
}
