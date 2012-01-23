package swimgame.player;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import swimgame.out.Console;
import swimgame.out.Debug;
import swimgame.table.CardStack;
import swimgame.table.logic.TableLogic;

/** A simple human player interface. */
public class HumanPlayer extends AbstractPlayer {
    /** Name for this player. */
    private static final String NAME = "Human";
    /** {@link TableLogic} of the game table. */
    private final TableLogic tableLogic;
    /** Card stack owned by this player instance. */
    private CardStack cardStack;
    /** Game close called? */
    private boolean gameIsClosed = false;

    /**
     * Default constructor.
     * 
     * @param newTableLogic
     *            {@link TableLogic} of the game table
     */
    public HumanPlayer(final TableLogic newTableLogic) {
	this.tableLogic = newTableLogic;
    }

    /**
     * Reads a key from the input stream.
     * 
     * @param message
     *            The message to display before reading
     * @return The key pressed
     */
    private char getKey(final String message) {
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
    public final boolean keepCardSet() {
	Debug.println("-----------Keep?");
	this.getKey("Keep cards? [Y/n]");
	return false;
    }

    /**
     * 
     * @return Negative, if an error occoured
     */
    private int getDropCard() {
	boolean input = false;
	while (!input) {
	    char key = this
		    .getKey("Press (1-3) to drop a card, [c] to close, [s] to skip..");
	    switch (key) {
	    case 'c':
		if (this.tableLogic.interact(TableLogic.Action.END_CALL,
			this.cardStack.getCards())) {
		    return -1;
		} else {
		    Console.println("Whoops, looks like you can't drop now.");
		}
		break;
	    case 's':
		this.tableLogic.interact(TableLogic.Action.MOVE_FINISHED);
		return -1;
	    case '1':
	    case '2':
	    case '3':
		return Integer.parseInt(String.valueOf(key)) - 1;
	    default:
		// just set some illegal value
		return -2;
	    }
	}
	return -2;
    }

    /**
     * 
     * @return Negative, if an error occoured
     */
    private int getPickCard() {
	Debug.println("##PICK");
	boolean input = false;
	int returnCode = -2;
	while (!input) {
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
	    default:
		break;
	    }
	}
	return returnCode;
    }

    @Override
    public final void doMove(final byte[] tableCards) {
	String cardsFormatString = "%5s %9s";

	if (this.gameIsClosed) {
	    Console.println("** close called!");
	}
	Console.println(String.format(cardsFormatString, "Table",
		new CardStack(tableCards).toString()));
	Console.println(String.format(cardsFormatString, "Yours",
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
		    try {
			this.cardStack
				.removeCard(this.cardStack.getCards()[cardToDrop]);
		    } catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		    }
		    this.cardStack.addCard(tableCards[cardToPick]);
		}
	    }
	}

	this.tableLogic.interact(TableLogic.Action.MOVE_FINISHED);
    }

    @Override
    public final String getName() {
	return NAME;
    }

    @Override
    final CardStack getCardStack() {
	return this.cardStack;
    }

    @Override
    final void setCardStack(final CardStack newCardStack) {
	this.cardStack = newCardStack;
    }

    @Override
    final void setGameClosed() {
	this.gameIsClosed = true;
    }
}
