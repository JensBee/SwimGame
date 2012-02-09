package cardGame.player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cardGame.event.EventBus;
import cardGame.table.GameTable;
import cardGame.table.GeneralGameTable;
import cardGame.util.Util;

public abstract class GeneralCardPlayer implements CardPlayer {
    /** List of predefined player names. */
    private static List<String> playerNames = new ArrayList<String>(
	    Arrays.asList("Bob", "Alice", "Carol", "Dave", "Ted", "Eve",
		    "Oscar", "Peggy", "Victor"));
    /** Name for this player. */
    protected String name;
    /** Table this player is playing at. */
    protected GameTable table;

    /**
     * Empty constructor. This will choose a random players name. This will also
     * register a receiver for {@link GeneralGameTable#Event} events.
     */
    public GeneralCardPlayer() {
	this.name =
		playerNames.remove(Util.getRandomInt(playerNames.size() - 1));
	this.registerTableEvents();
    }

    /**
     * Constructor. This will also register a receiver for
     * {@link GeneralGameTable#Event} events.
     * 
     * @param newName
     *            Players name
     */
    public GeneralCardPlayer(final String newName) {
	this.name = newName;
	this.registerTableEvents();
    }

    /** Register for table events. */
    private void registerTableEvents() {
	// TODO: reduce event listeners
	EventBus.INSTANCE.registerEventReceiver(this,
		GeneralGameTable.Event.values());
    }

    @Override
    public final String getName() {
	return this.name;
    }

    @Override
    public final String toString() {
	return this.getName();
    }

    @Override
    public final void setTable(final GameTable newTable) {
	this.table = newTable;
    }
}
