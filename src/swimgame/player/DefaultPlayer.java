package swimgame.player;

import swimgame.out.Debug;
import swimgame.player.rating.Card;
import swimgame.player.rating.Stack;
import swimgame.table.CardStack;
import swimgame.table.logic.TableLogic;

/**
 * A player playing the game. The default implementation of a player.
 * 
 * <pre>
 * TODO:
 * 	- handle last round (drop & pick calculation)
 * 	- strategy: if player took lead with n points then drop the cards with 
 * 	  a probability of 0.5 if possible, just to keep the lead
 * </pre>
 * 
 * @author <a href="mailto:code@jens-bertram.net">Jens Bertram</a>
 * 
 */
public class DefaultPlayer extends AbstractPlayer {
    /** Cards seen on the table. */
    private CardStack cardStackTable;
    /** Cards we want to get. */
    private CardStack cardStackNeed;
    /** Cards stack owned by this player. */
    private CardStack cardStack;
    /** Cards on the table to decide on (if it's our turn). */
    private byte[] cardsTableTriple = new byte[] {
	    CardStack.FLAG_UNINITIALIZED, CardStack.FLAG_UNINITIALIZED,
	    CardStack.FLAG_UNINITIALIZED };
    /** {@link TableLogic} of the game table. */
    private final TableLogic tableLogic;
    /** The name of this {@link IPlayer} instance. */
    private final String name;
    /** Game close called? */
    private boolean gameIsClosed = false;

    /** Player behavior configuration. */
    protected final PlayerConfiguration behavior = new PlayerConfiguration();

    /** rating cards. */
    private Card cardRating = null;

    /**
     * Default constructor.
     * 
     * @param newTableLogic
     *            {@link TableLogic} of the game table
     */
    public DefaultPlayer(final TableLogic newTableLogic) {
	this.name = this.getRandomName();
	this.tableLogic = newTableLogic;
    }

    /**
     * Constructor that allows direct setting of the players name.
     * 
     * @param newTableLogic
     *            {@link TableLogic} of the game table
     * @param playerName
     *            Name of this player
     */
    public DefaultPlayer(final TableLogic newTableLogic, final String playerName) {
	this.tableLogic = newTableLogic;
	this.name = playerName;
    }

    @Override
    CardStack getCardStack() {
	return this.cardStack;
    }

    private void initialize() {
	this.cardStackTable = new CardStack();
	this.cardStackNeed = new CardStack();
	this.cardStackTable.fill((byte) Card.AVAILABILITY_UNSEEN);

	if (this.cardRating != null) {
	    this.cardRating.reset();
	} else {
	    this.cardRating = new Card();
	}
    }

    @Override
    public final void setCards(final byte[] cards) {
	super.setCards(cards);
	this.cardRating = new Card();
	// store our own cards as being in the game
	for (byte card : cards) {
	    this.cardRating.cardSeen(card);
	}
    }

    /**
     * Decide (if able to), if our initial card set is good enough.
     */
    @Override
    public final boolean keepCardSet() {
	Debug.println(Debug.TALK, this, "Deciding on my current card set: "
		+ this.cardStack);

	if (this.cardStack.getValue() < this.behavior
		.get(PlayerConfiguration.STACKDROP_INITIAL)) {
	    // drop immediately if below threshold
	    Debug.println(Debug.TALK, this, String.format(
		    "Dropping (below threshold (%f))",
		    this.behavior.get(PlayerConfiguration.STACKDROP_INITIAL)));

	    this.log("Uhm... no!");
	    return false;
	}

	Debug.println(Debug.TALK, this, "Ok, I'm keeping this card set");
	return true;
    }

    /**
     * Rate our current cards to decide witch we'll probably drop. Results go
     * into the needed cards array to save space.
     */
    private void rateCards() {
	Debug.println(Debug.TALK, this, "Rating my current cards..");
	for (byte card : this.cardStack.getCards()) {
	    this.cardStackNeed.setCardValue(card,
		    (byte) this.cardRating.getRating(this.cardStack, card));
	}
    }

    /**
     * Sort a triple of cards according to values in a stack.
     * 
     * @param stack
     *            The stack to get the card values from
     * @param triple
     *            Array containing the card-indices for the cards to sort
     * @return The input array sorted by card-values
     */
    private byte[] sortCardTriple(final CardStack stack, final byte[] triple) {
	byte[] sortedTriple = new byte[] { CardStack.FLAG_UNINITIALIZED,
		CardStack.FLAG_UNINITIALIZED, CardStack.FLAG_UNINITIALIZED };

	for (int i = 0; i < 3; i++) {
	    for (int j = 0; j < 3; j++) {
		int sourceValue = (triple[i] == CardStack.FLAG_UNINITIALIZED) ? CardStack.FLAG_UNINITIALIZED
			: stack.getCardValue(triple[i]);
		int targetValue = (sortedTriple[j] == CardStack.FLAG_UNINITIALIZED) ? CardStack.FLAG_UNINITIALIZED
			: stack.getCardValue(sortedTriple[j]);
		if (sourceValue >= targetValue) {
		    // shift content..
		    if (j == 0) {
			sortedTriple[2] = sortedTriple[1];
			sortedTriple[1] = sortedTriple[0];
		    } else if (j == 1) {
			sortedTriple[2] = sortedTriple[1];
		    }
		    // ..to insert the new
		    sortedTriple[j] = triple[i];
		    break;
		}
	    }
	}
	return sortedTriple;
    }

    @Override
    public final void doMove(final byte[] tableCards) {
	int index;
	double dropValue;
	Debug.println(Debug.INFO, this,
		"My stack: " + this.cardStack.toString());

	// regenerate priority list of needed cards
	Stack.calculateNeededCards(this.cardStack, this.cardStackNeed);

	// get cards on table
	index = 0;
	for (byte card : tableCards) {
	    this.cardsTableTriple[index++] = card;
	}

	// rate table cards
	this.cardsTableTriple = this.sortCardTriple(this.cardStackNeed,
		this.cardsTableTriple);
	if (Debug.debug && (Debug.debugLevel == Debug.INFO)) {
	    Debug.print(Debug.INFO, this, "My table cards rating: ");
	    for (byte card : this.cardsTableTriple) {
		Debug.print(Debug.INFO, String.format("%s:%d ",
			CardStack.cardToString(card),
			this.cardStackNeed.getCardValue(card)));
	    }
	    Debug.print(Debug.INFO, "\n");
	}
	// make a pick suggestion
	Debug.println(
		Debug.TALK,
		this,
		"Thought: I'll pick "
			+ CardStack.cardToString(this.cardsTableTriple[0]));

	// rate own cards
	this.rateCards();

	// dump rating stack
	Debug.print(Debug.TALK, this, "My need stack:\n"
		+ this.cardStackNeed.dump().toString() + "\n");

	// estimate goal distance
	byte[] goalDistance = Stack.goalDistance(this.cardStack);
	if (Debug.debug) {
	    if ((goalDistance[0] != -1) || (goalDistance[2] != -1)) {
		Debug.print(Debug.INFO, this, "Goal distance(s): ");
		if (goalDistance[0] > -1) {
		    Debug.print(Debug.INFO, String.format("[%s?]:%d ",
			    CardStack.CARD_SYMBOLS[goalDistance[0]],
			    goalDistance[1]));
		}
		if (goalDistance[2] > -1) {
		    Debug.print(Debug.INFO, String.format("[?%s]:%d",
			    CardStack.CARD_NAMES[goalDistance[2]],
			    goalDistance[3]));
		}
		Debug.nl(Debug.INFO);
	    } else {
		Debug.println(Debug.TALK, this, "Goal not in sight.");
	    }
	}

	// stack value
	dropValue = Stack.dropValue(this.cardStack);
	Debug.println(Debug.INFO, this, "Drop value: " + dropValue);

	// make a drop suggestion
	byte[] cardToDrop = new byte[] { CardStack.FLAG_UNINITIALIZED,
		CardStack.FLAG_UNINITIALIZED };

	cardToDrop[1] = 127; // make sure all cards will be lower
	for (byte card : this.cardStack.getCards()) {
	    byte cardValue = this.cardStackNeed.getCardValue(card);
	    if ((cardValue == -1) || (cardValue < cardToDrop[1])) {
		cardToDrop[0] = card;
		cardToDrop[1] = cardValue;
	    }
	}

	// end game?
	if (!this.gameIsClosed && (dropValue > Stack.NO_RESULT)) {
	    this.log("*knock!, knock!*");
	    this.tableLogic.interact(TableLogic.Action.END_CALL,
		    this.cardStack.getCards());
	} else {
	    if (cardToDrop[0] == -1) {
		// TODO: evaluate skip action
	    } else {
		// drop & pick
		if (this.tableLogic.interact(TableLogic.Action.DROP_CARD,
			cardToDrop[0])
			&& this.tableLogic.interact(
				TableLogic.Action.PICK_CARD,
				this.cardsTableTriple[0])) {
		    try {
			Debug.printf(Debug.INFO, this, "Drop %s Pick %s"
				+ cardToDrop[0], this.cardsTableTriple[0]);
			this.cardStack.removeCard(cardToDrop[0]);
			this.cardStack.addCard(this.cardsTableTriple[0]);
		    } catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		    }
		}
	    }
	}
	// finished
	// this.gameRound++;
	this.tableLogic.interact(TableLogic.Action.MOVE_FINISHED);
    }

    @Override
    public final void handleTableEvent(final TableLogic.Event event,
	    final Object data) {
	switch (event) {
	case INITIAL_CARDSTACK_DROPPED:
	    this.cardStackTable.addCard((byte[]) data);
	    break;
	case GAME_CLOSED:
	    this.gameIsClosed = true;
	    break;
	case GAME_START:
	    this.initialize();
	    break;
	case CARD_DROPPED:
	    this.cardRating.cardSeen((byte) data);
	    break;
	default:
	    break;
	}
    }

    @Override
    public final String getName() {
	return this.name;
    }

    @Override
    void setCardStack(CardStack newCardStack) {
	this.cardStack = newCardStack;
    }

    @Override
    void setGameClosed() {
	this.gameIsClosed = true;
    }
}
