package cardGame.games.swimming;

import cardGame.card.CardDeck;

public class Swimming {

    public static void main(String[] args) {
	GameLogic game = new GameLogic();
	Table table = new Table(CardDeck.Deck.SKAT);

	game.setTable(table);

	try {
	    table.addPlayer(new AIPlayer());
	    table.addPlayer(new AIPlayer());
	    table.addPlayer(new AIPlayer());
	    table.addPlayer(new AIPlayer());
	} catch (Exception e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}

	game.setNumberOfGamesToPlay(10);
	game.setMaxRoundsToPlay(40);

	game.start();
    }

}
