package swimgame.testing;

import swimgame.table.logic.TableLogic;
import cardGame.event.CardGameEvent;
import cardGame.player.CardPlayer;
import cardGame.table.ITableController;

public class FakeTable implements ITableController {
    /** The {@link TableLogic} to be controlled by this instance. */
    private final TableLogic tableLogic;

    FakeTable() {
	super();
	this.tableLogic = new TableLogic(this);
    }

    @Override
    public void start() {
	// TODO Auto-generated method stub
    }

    @Override
    public void handleEvent(Enum<? extends CardGameEvent> event, Object data) {
	// TODO Auto-generated method stub

    }

    @Override
    public void setNumberOfGamesToPlay(int numberOfGamesToPlay) {
	// TODO Auto-generated method stub

    }

    @Override
    public void addPlayer(CardPlayer newPlayer) throws Exception {
	// TODO Auto-generated method stub

    }
}
