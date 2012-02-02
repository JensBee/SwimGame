package cardGame.player;

import swimgame.table.CardStack;
import swimgame.table.logic.TableLogic;
import cardGame.CardDeck.Card;

/**
 * Interface for a player to be able to join the game. A Player model can be
 * derived from the AbstractPlayer class.
 * 
 * @author <a href="mailto:code@jens-bertram.net">Jens Bertram</a>
 * 
 */
public interface IPlayer {
    /**
     * Cards passed to the player from the table.
     * 
     * @param cards
     *            Array of card indices
     * @see CardStack
     */
    void setCards(final Card[] cards);

    /**
     * Handle {@link TableLogic.Event} events. See {@link TableLogic.Event} for
     * a reference of possible signals.
     * 
     * @param event
     *            An int value indicating the {@link TableLogic.Event} type.
     * @param data
     *            Data associated with this {@link TableLogic.Event}
     * @see TableLogic.Event
     */
    void handleTableEvent(final TableLogic.Event event, final Object data);

    /**
     * The player has to decide if he want to keep the card set he already
     * recieved, or if he would like to get another one (witch implies dropping
     * the first). This is only possible, if the player is starting the current
     * round.
     * 
     * @return False, if we want to retrieve a new set of cards
     */
    // TODO: remove this from interface as it's game specific
    boolean keepCardSet();

    /**
     * It's this players turn to decide for a move.
     * 
     * @param tableCards
     *            Array of card indices
     * @see CardStack
     */
    void doMove(final Card[] tableCards);

    /**
     * Get the list of cards owned by this player.
     * 
     * @return Array of card indices
     * @see CardStack
     */
    Card[] getCards();

    /**
     * Get the name of this player.
     * 
     * @return The name of this player
     */
    String getName();
}
