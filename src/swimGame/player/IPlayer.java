package swimGame.player;

import swimGame.cards.CardStack;
import swimGame.table.Table;

/**
 * Interface for a player to be able to join the game. A Player model can be
 * derived from the AbstractPlayer class.
 * 
 * @author Jens Bertram <code@jens-bertram.net>
 * 
 */
public interface IPlayer {
	/**
	 * Cards passed to the player from the table. The int[] array consists of 3
	 * values. Each representing a card (see array-table below). ♦7 will be
	 * represented as 0, ♣A will be represented as 31.
	 * 
	 * <pre>
	 * Example (31 = ♣A): 
	 * 	ROW: (31/8) = 3,..; 3 is the row (♣)
	 *  COLUMN: (31 - (3*8)) = 7; 7 is the column (A)
	 * </pre>
	 * 
	 * <pre>
	 *  Array table:
	 *  
	 *   7 8 9 10 J Q K A 
	 * ♦ . . . .  . . . .
	 * ♥ . . . .  . . . . 
	 * ♠ . . . .  . . . . 
	 * ♣ . . . .  . . . .
	 * </pre>
	 */
	public void setCards(final byte[] cards) throws Exception;

	/**
	 * Handle events emitted by the table. See Table class for a reference of
	 * possible signals.
	 * 
	 * @param event
	 *            An int value indicating the event-type.
	 * @param data
	 *            Data associated with this event (see swimGame.Table#Event)
	 * @see swimGame.Table
	 * @see swimGame.Table#Event
	 */
	public void handleTableEvent(final Table.Event event, Object data);

	/**
	 * The player has to decide if he want to keep the card set he already
	 * recieved, or if he would like to get another one (wich implies dropping
	 * the first). This is only possible, if the player is starting the current
	 * round.
	 * 
	 * @return False, if we want to retrieve a new set of cards
	 */
	public boolean keepCardSet();

	public boolean doMove(CardStack table);
}
