package swimGame.testing;

import swimGame.out.Debug;
import swimGame.player.DefaultPlayer;
import swimGame.player.IPlayer;
import swimGame.table.CardStack;
import swimGame.table.TableLogic;

public class PlayerCardRating {

    /**
     * [Table] Alice's cards: [♥J][♣J][♣K] value: 20 overall: 20 [Table] Oscar's
     * cards: [♥A][♠K][♠A] value: 21 overall: 21 [Table] * Eve's cards:
     * [♦Q][♥Q][♣Q] value: 10 overall: 10
     * 
     * @param args
     */
    public static void main(String[] args) {
	Debug.debug = true;
	IPlayer player = new DefaultPlayer(new TableLogic(new FakeTable()));
	CardStack cardStack = new CardStack();

	cardStack.card.add(new byte[] { 5, 13, 29 });
	System.out.println("Cards: " + cardStack.toString());
	System.out.println("Cards value: " + cardStack.getValue());
	player.setCards(cardStack.getCards());
	player.handleTableEvent(TableLogic.Event.GAME_START, null);
	player.doMove(new byte[] { 5, 17, 24 });
    }

}
