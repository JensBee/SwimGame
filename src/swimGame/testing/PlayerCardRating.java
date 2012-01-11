package swimGame.testing;

import swimGame.out.Debug;
import swimGame.player.DefaultPlayer;
import swimGame.player.IPlayer;
import swimGame.table.CardStack;
import swimGame.table.TableLogic;

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

	cardStack.card.add(new byte[] { 5, 13, 29 });
	System.out.println("Cards: " + cardStack.toString());
	System.out.println("Cards value: " + cardStack.getValue());
	player.setCards(cardStack.getCards());
	player.handleTableEvent(TableLogic.Event.GAME_START, null);
	player.doMove(new byte[] { 5, 17, 24 });
    }

}
