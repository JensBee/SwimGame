package swimGame.testing;

import swimGame.table.Table;

public class FakeTable extends Table {

    @Override
    public boolean interact(Table.Action action) {
	return true;
    }

    @Override
    public boolean interact(Table.Action action, Object data) {
	return true;
    }
}
