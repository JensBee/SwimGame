package swimGame.player;

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
	 * two-pair values, each representing a card. Where [0,2,4,3,6,1] would
	 * represent the three cards 0,2:7♠ 4,3:B♣ 6,1:K♥. The meaning of the int
	 * values are derived from the matrix shown below. The first value is in X
	 * direction, the second in Y.
	 * 
	 * <pre>
	 *   7 8 9 10 B D K A 
	 * ♦ . . . .  . . . .
	 * ♥ . . . .  . . . . 
	 * ♠ . . . .  . . . . 
	 * ♣ . . . .  . . . .
	 * </pre>
	 */
	public void setCards(final int[] cards) throws Exception;

	/**
	 * Handle events emitted by the table. See Table class for a reference of
	 * possible signals.
	 * 
	 * @param event
	 *            A byte value indicating the event-type.
	 * @see swimGame.Table
	 */
	public void handleTableEvent(final byte event);

	/**
	 * The player has to decide if he want to keep the card set he already
	 * recieved, or if he would like to get another one (wich implies dropping
	 * the first). This is only possible, if the player is starting the current
	 * round.
	 * 
	 * @return False, if we want to retrieve a new set of cards
	 */
	public boolean keepCardSet();
}
