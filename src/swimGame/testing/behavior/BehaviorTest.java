package swimGame.testing.behavior;

import swimGame.out.Debug;
import swimGame.player.DefaultPlayer;
import swimGame.player.IPlayer;
import swimGame.table.DefaultTable;
import swimGame.table.TableLogic;

public class BehaviorTest {
    // store the number of won games (mutated player first)
    private final int[] wonGames = new int[4];
    // store the points (mutated player first)
    private final double[] points = new double[4];

    /**
     * Test player with different behavior values
     * 
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
	// not nice, but sufficient here
	class BasicTable extends DefaultTable {
	    BasicTable() {
		super();
	    }

	    protected void getFinalRatings() {
		byte rank = 1;
		for (IPlayer player : this.tableLogic.player.getRanked()
			.keySet()) {
		    System.out.println(String.format("%d. %s with %.0f", rank,
			    player.toString(),
			    this.tableLogic.player.getPoints(player)));
		    rank++;
		}
	    }
	}

	Debug.debug = false;
	BasicTable table = new BasicTable();
	TableLogic tableLogic = table.getLogic();

	// add our mutable player
	table.addPlayer(new DefaultPlayer(tableLogic, "MutablePlayer"));
	// add a number of default behavior opponents
	table.addPlayers(3);

	table.start();
	table.getFinalRatings();
    }
}
