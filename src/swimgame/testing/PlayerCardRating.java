package swimgame.testing;

import cardGame.CardDeck;
import cardGame.player.IPlayer;
import swimgame.out.Debug;
import swimgame.player.DefaultPlayer;
import swimgame.table.CardStack;
import swimgame.table.logic.TableLogic;

public class PlayerCardRating {

    /**
     * Test rating a fixed card set
     * 
     * @param args
     */
    public static void main(String[] args) {
	Debug.debug = true;
	IPlayer player = new DefaultPlayer(new TableLogic(new FakeTable()));
	CardStack cardStack = new CardStack();

	cardStack.addCard(new CardDeck.Card[] { CardDeck.Card.DIAMOND_QUEEN,
		CardDeck.Card.HEART_QUEEN, CardDeck.Card.SPADE_QUEEN });
	System.out.println("Cards: " + cardStack.toString());
	System.out.println("Cards value: " + cardStack.getValue());
	player.setCards(cardStack.getCards());
	player.handleTableEvent(TableLogic.Event.GAME_START, null);
	player.doMove(new CardDeck.Card[] { CardDeck.Card.DIAMOND_QUEEN,
		CardDeck.Card.SPADE_EIGHT, CardDeck.Card.CLUB_SEVEN });
    }

}
