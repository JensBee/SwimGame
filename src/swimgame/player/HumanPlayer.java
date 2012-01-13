package swimgame.player;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import swimgame.out.Console;
import swimgame.out.Debug;
import swimgame.table.CardStack;
import swimgame.table.TableLogic;

public class HumanPlayer extends AbstractPlayer {
    private static final String CARDS_FORMATSTRING = "%5s %9s";

    public HumanPlayer(TableLogic tableLogic) {
	super(tableLogic);
	this.name = "Human";
    }

    private char getKey(String message) {
	BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	Console.println("> " + message);
	try {
	    return (char) br.read();
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	return 'X';
    }

    @Override
    public boolean keepCardSet() {
	Debug.println("-----------Keep?");
	this.getKey("Keep cards? [Y/n]");
	return false;
    }

    /**
     * 
     * @return Negative, if an error occoured
     */
    private int getDropCard() {
	Debug.println("##DROP");
	boolean input = false;
	int returnCode = -2;
	while (input == false) {
	    char key = this
		    .getKey("Press (1-3) to drop a card, [c] to close, [s] to skip..");
	    switch (key) {
	    case 'c':
		if (this.tableLogic.interact(TableLogic.Action.END_CALL,
			this.cardStack.getCards())) {
		    returnCode = -1;
		    input = true;
		} else {
		    Console.println("Whoops, looks like you can't drop now.");
		}
		break;
	    case 's':
		this.tableLogic.interact(TableLogic.Action.MOVE_FINISHED);
		returnCode = -1;
		input = true;
		break;
	    case '1':
		returnCode = 0;
		input = true;
		break;
	    case '2':
		returnCode = 1;
		input = true;
		break;
	    case '3':
		returnCode = 2;
		input = true;
		break;
	    }
	}
	return returnCode;
    }

    /**
     * 
     * @return Negative, if an error occoured
     */
    private int getPickCard() {
	Debug.println("##PICK");
	boolean input = false;
	int returnCode = -2;
	while (input == false) {
	    char key = this.getKey("Press (1-3) to pick a card");
	    switch (key) {
	    case '1':
		returnCode = 0;
		input = true;
		break;
	    case '2':
		returnCode = 1;
		input = true;
		break;
	    case '3':
		returnCode = 2;
		input = true;
		break;
	    }
	}
	return returnCode;
    }

    @Override
    public void doMove(byte[] tableCards) {
	if (this.gameIsClosed == true) {
	    Console.println("** close called!");
	}
	Console.println(String.format(HumanPlayer.CARDS_FORMATSTRING, "Table",
		new CardStack(tableCards).toString()));
	Console.println(String.format(CARDS_FORMATSTRING, "Yours",
		this.cardStack.toString()));
	int cardToPick = 0;
	int cardToDrop = 0;

	cardToDrop = this.getDropCard();
	// no skip and close
	if (cardToDrop != -1) {
	    cardToPick = this.getPickCard();

	    if ((cardToDrop >= 0) && (cardToDrop <= 3) && (cardToPick >= 0)
		    && (cardToPick <= 4)) {
		// drop & pick
		if (this.tableLogic.interact(TableLogic.Action.DROP_CARD,
			this.cardStack.getCards()[cardToDrop])
			&& this.tableLogic.interact(
				TableLogic.Action.PICK_CARD,
				tableCards[cardToPick])) {
		    this.cardStack.card
			    .remove(this.cardStack.getCards()[cardToDrop]);
		    this.cardStack.card.add(tableCards[cardToPick]);
		}
	    }
	}

	this.tableLogic.interact(TableLogic.Action.MOVE_FINISHED);
    }
}
