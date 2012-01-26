package swimgame.player;

import swimgame.out.Debug;
import swimgame.player.rating.Card;
import swimgame.table.CardStack;
import swimgame.table.DefaultTableController;
import swimgame.table.logic.TableLogic;

/**
 * A player playing the game. The default implementation of a player.
 * 
 * <pre>
 * TODO:
 * 	- handle last round (drop & pick calculation)
 * 	- implement a rating pipeline for different game modes
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
    /** Tracks, witch colors were dropped during the game. */
    private final byte[] dropColorIndex = new byte[CardStack.CARDS_MAX_COLOR];
    /** {@link TableLogic} of the game table. */
    private final TableLogic tableLogic;
    /** The name of this {@link IPlayer} instance. */
    private final String name;
    /** Game close called? */
    private boolean gameIsClosed = false;

    /** Player behavior configuration. */
    protected final PlayerConfiguration behavior = new PlayerConfiguration();

    /** To be removed! */
    private static final byte B_RISKYNESS = 3;

    // nested classes
    /** Nested class for rating cards. */
    private Card cardRating = null;
    /** Nested class for rating a card stack. */
    private final StackRating stackRating = new StackRating();

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
	for (int i = 0; i < this.dropColorIndex.length; i++) {
	    this.dropColorIndex[i] = 0;
	}
	// this.gameRound = 0;
	if (this.cardRating != null) {
	    this.cardRating.reset();
	}
    }

    /**
     * General rating helper functions.
     * 
     * @author <a href="mailto:code@jens-bertram.net">Jens Bertram</a>
     * 
     */
    private static class Rating {
	/**
	 * Calculate with default minimum fixed to zero.
	 * 
	 * @param max
	 *            Maximum value
	 * @param value
	 *            Current value
	 * @return Normalized value
	 */
	protected static double normalize(final double max, final double value) {
	    return Rating.normalize(0, max, value);
	}

	/**
	 * Calculate a normalized value.
	 * 
	 * @param min
	 *            Minimum value
	 * @param max
	 *            Maximum value
	 * @param value
	 *            Current value
	 * @return Normalized value
	 */
	protected static double normalize(final double min, final double max,
		final double value) {
	    return new Double(((value - min) / (max - min)) * 10);
	}
    }

    /**
     * Stack rating functions
     * 
     * @author <a href="mailto:code@jens-bertram.net">Jens Bertram</a>
     * 
     */
    private class StackRating {
	protected static final int NO_RESULT = -1;

	/**
	 * Check, how many cards are missing to reach a goal state. No
	 * calculation of values is involved here
	 * 
	 * @return How many cards are missing to reach a goal state
	 *         [color][steps][type][steps]
	 */
	protected byte[] goalDistance(final byte[] cards) {
	    byte positionColor;
	    byte valueColor;
	    byte positionType;
	    byte valueType;
	    byte count;
	    byte[] distances = new byte[] { CardStack.FLAG_UNINITIALIZED,
		    CardStack.FLAG_UNINITIALIZED, CardStack.FLAG_UNINITIALIZED,
		    CardStack.FLAG_UNINITIALIZED };

	    positionColor = -1;
	    positionType = -1;
	    valueColor = -1;
	    valueType = -1;
	    for (byte card : cards) {
		count = 0;
		final int color = CardStack.getCardColor(card);
		for (byte cardByColor : CardStack.getCardsByColor(color)) {
		    if (DefaultPlayer.this.cardStack.hasCard(cardByColor)) {
			count++;
		    }
		}
		// store max
		if (count > valueColor) {
		    valueColor = count;
		    positionColor = (byte) color;
		}

		count = 0;
		final int type = CardStack.getCardType(card);
		for (byte cardByType : CardStack.getCardsByType(type)) {
		    if (DefaultPlayer.this.cardStack.hasCard(cardByType)) {
			count++;
		    }
		}
		// store max
		if (count > valueType) {
		    valueType = count;
		    positionType = (byte) type;
		}
	    }

	    if (valueColor >= 2) {
		distances[0] = positionColor;
		distances[1] = (byte) ((CardStack.CARDS_MAX_COLOR - 1) - valueColor);
	    }
	    if (valueType >= 2) {
		distances[2] = positionType;
		distances[3] = (byte) (TableLogic.RULE_GOAL_CARDS_BY_TYPE - valueType);
	    }
	    return distances;
	}

	protected double dropValue(final byte[] cards) {
	    double value;
	    final byte[] goalDistances = this.goalDistance(cards);
	    if ((goalDistances[1] == 0) || (goalDistances[3] == 0)) {
		value = DefaultPlayer.this.cardStack.getValue();
	    } else {
		value = StackRating.NO_RESULT;
	    }
	    return value;
	}

	/**
	 * Create a rating array for all currently not-owned cards. This may be
	 * helpful to decide, witch card to pick from the table.
	 */
	protected void calculateNeededCards() {
	    DefaultPlayer.this.cardStackNeed.fill(CardStack.FLAG_UNINITIALIZED);
	    byte[] typeChecked = new byte[CardStack.CARDS_MAX_CARD];

	    // walk the stack by color
	    for (int cardsColor = 0; cardsColor < CardStack.CARDS_MAX_COLOR; cardsColor++) {
		int cardOffset;
		int matches;

		// rate by color
		matches = 0;
		cardOffset = (cardsColor * (CardStack.CARDS_MAX_CARD));
		// count matching cards by color
		for (int cardNum = 0; cardNum < CardStack.CARDS_MAX_CARD; cardNum++) {
		    int cardToCheck = cardOffset + cardNum;
		    if (DefaultPlayer.this.cardStack.hasCard(cardToCheck)) {
			matches++;
		    }
		}

		// skip further rating, if no card found in current color
		if (matches > 0) {
		    // color: rate the missing ones based on the number of
		    // matches
		    for (int cardNum = 0; cardNum < CardStack.CARDS_MAX_CARD; cardNum++) {
			int cardToCheck = cardOffset + cardNum;
			if (!DefaultPlayer.this.cardStack.hasCard(cardToCheck)) {
			    int ratingValue = Card.getCardRating(cardToCheck)
				    + (matches * Card.INFLUENCE_SAME_COLOR);
			    Card.uprate(DefaultPlayer.this.cardStackNeed,
				    cardToCheck, ratingValue);
			}
		    }

		    // rate by type
		    cardOffset = CardStack.CARDS_MAX_CARD;
		    // count matching cards by type
		    for (int cardNum = 0; cardNum < CardStack.CARDS_MAX_CARD; cardNum++) {
			if (typeChecked[cardNum] == 1) {
			    continue;
			}

			int ownedCard = cardNum
				+ (cardsColor * (CardStack.CARDS_MAX_CARD));

			// if we found a card check remaining colors for this
			// card for matches
			if (DefaultPlayer.this.cardStack.hasCard(ownedCard)) {
			    typeChecked[Card.getCardRating(cardNum)] = 1;
			    for (int i = cardsColor; i < CardStack.CARDS_MAX_COLOR; i++) {
				int cardToCheck = cardNum + (i * cardOffset);
				if ((cardToCheck != ownedCard)
					&& DefaultPlayer.this.cardStack
						.hasCard(cardToCheck)) {
				    int ratingValue = DefaultPlayer.this.cardStackNeed
					    .getCardValue(cardToCheck)
					    + Card.INFLUENCE_SAME_TYPE
					    + (int) (DefaultTableController.WORTH_THREE_OF_SAME_TYPE / 3);

				    for (int fixedTypeCard = 0; fixedTypeCard < CardStack.CARDS_MAX_COLOR; fixedTypeCard++) {
					int cardToRate = cardNum
						+ (fixedTypeCard * cardOffset);
					if (!DefaultPlayer.this.cardStack
						.hasCard(cardToRate)) {
					    Card.uprate(
						    DefaultPlayer.this.cardStackNeed,
						    cardToRate, ratingValue);
					}
				    }
				}
			    }
			}
		    }
		}
	    }
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

	if (DefaultPlayer.this.cardStack.getValue() < this.behavior
		.get(PlayerConfiguration.STACKDROP_INITIAL)) {
	    // drop immediately if blow threshold
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
     * into the needed cards array (as negative values) to save some space.
     */
    private void rateCards() {
	Debug.println(Debug.TALK, this, "Rating my current cards..");
	for (byte card : this.cardStack.getCards()) {
	    this.cardRating = new Card();
	    System.out.println("CARD: " + CardStack.cardToString(card));
	    this.cardStackNeed.setCardValue(card,
		    (byte) this.cardRating.getRating(this.cardStack, card));
	    // if (Debug.debug) {
	    // Debug.println(
	    // Debug.TALK,
	    // this,
	    // String.format("Rating %-5s: %s",
	    // CardStack.cardToString(card),
	    // this.cardRating.dumpRating()));
	    // }
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
	this.stackRating.calculateNeededCards();

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
	byte[] goalDistance = this.stackRating.goalDistance(this.cardStack
		.getCards());
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
	dropValue = this.stackRating.dropValue(this.cardStack.getCards());
	Debug.println(Debug.INFO, this, "Drop value: " + dropValue);

	// make a drop suggestion
	byte[] cardToDrop = new byte[] { CardStack.FLAG_UNINITIALIZED,
		CardStack.FLAG_UNINITIALIZED };

	cardToDrop[1] = 127; // make sure all cards will be lower
	for (byte card : this.cardStack.getCards()) {
	    byte cardValue = this.cardStackNeed.getCardValue(card);
	    if ((cardValue == -1) || (cardValue < cardToDrop[1])) {
		// MODEL:risk wait for third card type?
		if ((CardStack.getCardType(card) == goalDistance[2])
			&& (goalDistance[3] > 0)) {
		    if ((B_RISKYNESS >= 2)
			    && (dropValue < DefaultPlayer.this.behavior
				    .get(PlayerConfiguration.WAIT_FOR_CARD))) {
			Debug.println(Debug.INFO, this,
				"#MODEL:risk Waiting for third card.");
			continue;
		    } else if (B_RISKYNESS > 2) {
			Debug.println(Debug.INFO, this,
				"#MODEL:risk Waiting for third card, event threshold is good.");
		    } else {
			Debug.println(Debug.INFO, this,
				"#MODEL:risk Skipping wait for third card.");
		    }
		}
		cardToDrop[0] = card;
		cardToDrop[1] = cardValue;
	    }
	}

	// end game?
	if (!this.gameIsClosed && (dropValue > StackRating.NO_RESULT)) {
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
