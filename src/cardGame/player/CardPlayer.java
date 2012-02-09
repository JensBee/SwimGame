package cardGame.player;

import java.util.Collection;

import cardGame.card.CardDeck.Card;
import cardGame.event.EventReceiver;
import cardGame.table.GameTable;

/**
 * Interface for a player to be able to join the game. A Player model can be
 * derived from the AbstractPlayer class.
 * 
 * @author <a href="mailto:code@jens-bertram.net">Jens Bertram</a>
 * 
 */
public interface CardPlayer extends EventReceiver {
    /**
     * Cards passed to the player from the table. Player may respond with false,
     * to indicate rejecting the card set. It's up to the game rules if this is
     * allowed or not.
     * 
     * @param cards
     *            Array of card indices
     * @return True to accept, false to reject the cards
     */
    boolean setCards(final Collection<Card> cards);

    /**
     * Get the list of cards owned by this player.
     * 
     * @return Array of card indices
     * @see CardStack
     */
    Collection<Card> getCards();

    /**
     * Get the name of this player.
     * 
     * @return Name of this player
     */
    String getName();

    /**
     * Set the table this player is playing on.
     * 
     * @param table
     *            Table this player is playing at
     */
    void setTable(final GameTable table);
}
