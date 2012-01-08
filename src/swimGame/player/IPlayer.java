package swimGame.player;

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
	 * Cards passed to the player from the table.
	 */
	public void setCards(final byte[] cards);

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

	/**
	 * It's this players turn to decite for a move
	 * 
	 * @param tableCards
	 */
	public void doMove(byte[] tableCards);

	/**
	 * Get the list of cards owned by this player. This should only return
	 * something, if the table game is finished.
	 * 
	 * @return
	 */
	public byte[] getCards();
}
