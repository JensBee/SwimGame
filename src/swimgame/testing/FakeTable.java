package swimgame.testing;

import swimgame.table.AbstractTableController;
import swimgame.table.logic.TableLogic;
import swimgame.table.logic.TableLogic.Action;

public class FakeTable extends AbstractTableController {
    /** The {@link TableLogic} to be controlled by this instance. */
    private final TableLogic tableLogic;

    FakeTable() {
	super();
	this.tableLogic = new TableLogic(this);
    }

    @Override
    public void handleTableLogicEvent(Action action, Object data) {
	// TODO Auto-generated method stub

    }

    @Override
    public void start() {
	this.getLogic().initialize();
    }

    @Override
    public TableLogic getLogic() {
	return this.tableLogic;
    }

}
