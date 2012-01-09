package swimGame.player;

import swimGame.SwimGame;
import swimGame.out.Debug;
import swimGame.table.CardStack;
import swimGame.table.CardStack.CardIterator;
import swimGame.table.Table;

/**
 * A player playing the game. The default implementation of a player.
 * 
 * <pre>
 * TODO:
 * 	- handle last round (drop & pick calculation)
 * </pre>
 * 
 * @author Jens Bertram <code@jens-bertram.net>
 * 
 */
public class DefaultPlayer extends AbstractPlayer {
    // Cards seen on the table
    private CardStack cardStackTable;
    // Cards we want to get
    private CardStack cardStackNeed;
    // Cards on the table to decide on (if it's our turn)
    private byte[] cardsTableTriple = new byte[] {
	    CardStack.FLAG_UNINITIALIZED, CardStack.FLAG_UNINITIALIZED,
	    CardStack.FLAG_UNINITIALIZED };
    // the round we're playing
    private int gameRound = 0;

    // --- behavior defaults
    // below witch value should we drop an initial card stack immediately?
    private static final int STACk_DROP_TRESHOLD = 20;

    // nested classes
    private CardRating cardRating = null;
    private final StackRating stackRating = new StackRating();

    private void initialize() {
	this.cardStackTable = new CardStack();
	this.cardStackNeed = new CardStack();
	this.cardStackTable
		.fill((byte) DefaultPlayer.CardRating.INFLUENCE_DEFAULT_UNSEEN);
	this.gameRound = 0;
	if (this.cardRating != null) {
	    this.cardRating.reset();
	}
    }

    /** Constructor */
    public DefaultPlayer() {
	super();
	this.initialize();
    }

    /**
     * General rating helper functions
     * 
     * @author Jens Bertram <code@jens-bertram.net>
     * 
     */
    private static class Rating {
	/** Calculate with default min to zero */
	// TODO reuse other, rturn double
	protected static double normalize(double max, double value) {
	    return Rating.normalize(0, max, value);
	}

	protected static double normalize(double min, double max, double value) {
	    return new Double(((value - min) / (max - min)) * 10);
	}
    }

    /**
     * Card rating functions
     * 
     * @author Jens Bertram <code@jens-bertram.net>
     * 
     */
    private class CardRating {
	// rating influencing parameters
	private static final int INFLUENCE_SAME_COLOR = 5;
	private static final int INFLUENCE_SAME_TYPE = 10;
	// initial rating for cards we've not seen on the table
	private static final int INFLUENCE_DEFAULT_UNSEEN = -5;
	// the maximum value a card rating by type may reach
	protected final int worth_max_by_type = (2 * CardRating.INFLUENCE_SAME_TYPE)
		+ this.getNormalizedValue(CardStack.CARDS_MAX - 1)
		+ this.getNormalizedValue(CardStack.CARDS_MAX - 2)
		+ this.getNormalizedValue(CardStack.CARDS_MAX - 3);
	// the maximum value a card rating by color may reach
	private final int worth_max_by_color = (2 * CardRating.INFLUENCE_SAME_COLOR)
		+ this.getNormalizedValue(CardStack.CARDS_MAX - 1)
		+ this.getNormalizedValue(CardStack.CARDS_MAX - 2)
		+ this.getNormalizedValue(CardStack.CARDS_MAX - 3);
	protected byte[] dropColorIndex = new byte[CardStack.CARDS_MAX_COLOR];

	/** General function to rate a card based on it's color or value */
	private int rateColorOrType(int card, byte[] cardsArray, int influence) {
	    int cardValue = 0;
	    int same = 0;

	    for (byte stackCard : cardsArray) {
		if (DefaultPlayer.this.cardStack.hasCard(stackCard) == true) {
		    same++;
		    cardValue = cardValue + this.getNormalizedValue(card);

		    // modify rating if there are more cards
		    cardValue = (same > 1) ? (cardValue + influence)
			    : cardValue;
		}
	    }
	    return cardValue;
	}

	/** Rate a card based on other cards of the same color */
	protected double byColor(int card) {
	    int cardValue = this.rateColorOrType(card,
		    DefaultPlayer.this.cardStack
			    .getCardsByColor(DefaultPlayer.this.cardStack.card
				    .getColor(card)),
		    CardRating.INFLUENCE_SAME_COLOR);
	    return Rating.normalize(this.worth_max_by_color, cardValue);
	}

	protected double byColorFrequency(int card) {
	    int maxValue = 0;
	    for (byte value : this.dropColorIndex) {
		maxValue = (value > maxValue) ? value : maxValue;
	    }
	    return Rating.normalize(maxValue,
		    this.dropColorIndex[DefaultPlayer.this.cardStack.card
			    .getColor(card)]);
	}

	/** Rate a card based on it's type and cards of the same type */
	protected double byType(int card) {
	    int cardValue = this.rateColorOrType(card,
		    DefaultPlayer.this.cardStack
			    .getCardsByType(DefaultPlayer.this.cardStack.card
				    .getType(card)),
		    CardRating.INFLUENCE_SAME_TYPE);
	    return Rating.normalize(this.worth_max_by_type, cardValue);
	}

	/** Rate a card based on it's card value */
	protected double byValue(int card) {
	    return Rating.normalize(7, this.getRating(card));
	}

	protected double byAvailability(int card) {
	    int maxValue = 0;
	    for (byte stackCard : DefaultPlayer.this.cardStackTable.getCards()) {
		maxValue = DefaultPlayer.this.cardStackTable.card
			.getValue(stackCard) > maxValue ? DefaultPlayer.this.cardStackTable.card
			.getValue(stackCard) : maxValue;
	    }
	    return Rating.normalize(
		    DefaultPlayer.CardRating.INFLUENCE_DEFAULT_UNSEEN,
		    maxValue,
		    DefaultPlayer.this.cardStackTable.card.getValue(card));
	}

	/**
	 * Shortcut to assign a new rating to a card, only if it's higher than
	 * the existing rating
	 * 
	 * @param stack
	 *            The stack to search in
	 * @param card
	 *            The card to work with
	 * @param newValue
	 *            The new value we try to assign
	 */
	protected void uprate(CardStack stack, int card, int newValue) {
	    int oldValue = stack.card.getValue(card);
	    stack.card.setValue(card, (byte) (oldValue > newValue ? oldValue
		    : newValue));
	}

	/**
	 * Get the rating for this card
	 * 
	 */
	protected byte getRating(int card) {
	    CardStack.checkCard(card);
	    if (card < CardStack.CARDS_MAX_CARD) {
		return (byte) card;
	    }
	    return (byte) (card - ((card / CardStack.CARDS_MAX_CARD) * CardStack.CARDS_MAX_CARD));
	}

	/** Get a normalized value of a card */
	protected int getNormalizedValue(int card) {
	    int value = this.getRating(card);
	    if (value > 2) {
		// B, D, K -> 3; A -> 4
		return value < 7 ? 3 : 4;
	    } else {
		// 7 -> 0; 8 -> 1; 9 -> 2 (basically their array locations)
		return value;
	    }
	}

	protected void reset() {
	    this.dropColorIndex = new byte[CardStack.CARDS_MAX_COLOR];
	}

	protected void cardSeen(byte card) {
	    DefaultPlayer.this.cardStackTable.card.add(card);
	    this.dropColorIndex[DefaultPlayer.this.cardStack.card
		    .getColor(card)] = (byte) (this.dropColorIndex[DefaultPlayer.this.cardStack.card
		    .getColor(card)] + 1);
	}

	protected double byGoalDistance(byte card) {

	    // TODO: rate type also!

	    byte[] distances = DefaultPlayer.this.stackRating
		    .goalDistance(DefaultPlayer.this.cardStack.getCards());

	    if (distances[1] == -1) {
		return Rating.normalize(-1, 1, distances[1]);
	    }

	    int color = DefaultPlayer.this.cardStack.card.getColor(card);
	    if (distances[0] == color) {
		return Rating.normalize(-1, 1, distances[1]);
	    }

	    return 0;
	}
    }

    /**
     * Stack rating functions
     * 
     * @author Jens Bertram <code@jens-bertram.net>
     * 
     */
    private class StackRating {
	protected static final int NO_RESULT = -1;

	/**
	 * Check, how many cards are missing to reach a goal state. No
	 * calculation of values is involved here
	 * 
	 * @return How many cards are missing to reach a goal state
	 */
	protected byte[] goalDistance(byte[] cards) {
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
		int color = DefaultPlayer.this.cardStack.card.getColor(card);
		for (byte cardByColor : DefaultPlayer.this.cardStack
			.getCardsByColor(color)) {
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
		int type = DefaultPlayer.this.cardStack.card.getType(card);
		for (byte cardByType : DefaultPlayer.this.cardStack
			.getCardsByType(type)) {
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
		distances[3] = (byte) (valueType - 1);
	    }

	    return distances;
	}

	protected double dropValue(byte[] cards) {
	    byte[] goalDistances = this.goalDistance(cards);
	    if ((goalDistances[1] == 0) || (goalDistances[3] == 0)) {
		return this.byValue();
	    }
	    return StackRating.NO_RESULT;
	}

	/** Returns the current value of the stack */
	protected double byValue() {
	    CardIterator sI = DefaultPlayer.this.cardStack.new CardIterator();

	    double value = 0;
	    while (sI.hasNext()) {
		int sameType = 0;
		double currentValue = 0;
		// skip if we don't own this card
		if (sI.next() == CardStack.FLAG_NO_CARD) {
		    continue;
		}

		// do we have the same card with another color?
		for (byte b : DefaultPlayer.this.cardStack.getCardsByType(sI
			.getCardType())) {
		    if (b == CardStack.FLAG_HAS_CARD) {
			sameType++;
		    }
		}

		// same type in three colors is fixed value
		if (sameType == 3) {
		    value = Table.WORTH_THREE_OF_SAME_TYPE;
		    break;
		} else if (sameType == 2) {
		    // we only missing one card of our type
		    // TODO: better decide on players "model" if he want's to
		    // take
		    // this route
		    Debug.println("--> shortening decision [FIXME!]");
		    value = (Table.WORTH_THREE_OF_SAME_TYPE / 3) * 2;
		    break;
		}

		// do we have another card in this row/of the same type?
		for (byte card : DefaultPlayer.this.cardStack
			.getCardsByColor(sI.getCardColor())) {
		    if (DefaultPlayer.this.cardStack.hasCard(card)) {
			// calculate the real value here
			currentValue = currentValue
				+ DefaultPlayer.this.cardRating
					.getNormalizedValue(card) + 7;
		    }
		}

		value = (currentValue > value) ? currentValue : value;
	    }

	    if (Debug.debug == true) {
		double dMin = CardStack.STACKVALUE_MIN - value;
		double dMax = CardStack.STACKVALUE_MAX - value;
		Debug.println(DefaultPlayer.this, String.format(
			"Stack value: abs(%s) dMax(+%.1f) dMin(-%.1f)",
			Double.toString(value), dMax, dMin));
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
			if (DefaultPlayer.this.cardStack.hasCard(cardToCheck) == false) {
			    int ratingValue = DefaultPlayer.this.cardRating
				    .getRating(cardToCheck)
				    + (matches * CardRating.INFLUENCE_SAME_COLOR);
			    DefaultPlayer.this.cardRating.uprate(
				    DefaultPlayer.this.cardStackNeed,
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
			// card
			// for matches
			if (DefaultPlayer.this.cardStack.hasCard(ownedCard)) {
			    typeChecked[DefaultPlayer.this.cardRating
				    .getRating(cardNum)] = 1;
			    for (int i = cardsColor; i < CardStack.CARDS_MAX_COLOR; i++) {
				int cardToCheck = cardNum + (i * cardOffset);
				if ((cardToCheck != ownedCard)
					&& DefaultPlayer.this.cardStack
						.hasCard(cardToCheck)) {
				    int ratingValue = DefaultPlayer.this.cardStackNeed.card
					    .getValue(cardToCheck)
					    + CardRating.INFLUENCE_SAME_TYPE
					    + (int) (Table.WORTH_THREE_OF_SAME_TYPE / 3);

				    for (int fixedTypeCard = 0; fixedTypeCard < CardStack.CARDS_MAX_COLOR; fixedTypeCard++) {
					int cardToRate = cardNum
						+ (fixedTypeCard * cardOffset);
					if (DefaultPlayer.this.cardStack
						.hasCard(cardToRate) == false) {
					    DefaultPlayer.this.cardRating
						    .uprate(DefaultPlayer.this.cardStackNeed,
							    cardToRate,
							    ratingValue);
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
    public void setCards(final byte[] cards) {
	super.setCards(cards);
	this.cardRating = new CardRating();
    }

    /**
     * Decide (if able to), if our initial card set is good enough.
     */
    @Override
    public boolean keepCardSet() {
	Debug.println(this, "Deciding on my current card set: "
		+ this.cardStack);

	if (this.stackRating.byValue() < DefaultPlayer.STACk_DROP_TRESHOLD) {
	    // drop immediately if blow threshold
	    Debug.println(this, String.format(
		    "Dropping (below threshold (%d))",
		    DefaultPlayer.STACk_DROP_TRESHOLD));

	    this.log("Uhm... no!");
	    return false;
	}

	Debug.println(this, "Ok, I'm keeping this card set");
	return true;
    }

    /**
     * Rate our current cards to decide witch we'll probably drop. Results go
     * into the needed cards array (as negative values) to save some space.
     */
    private void rateCards() {
	Debug.println(this, "Rating my current cards..");
	for (byte card : this.cardStack.getCards()) {
	    double ratingColor = this.cardRating.byColor(card);
	    double ratingType = this.cardRating.byType(card);
	    double ratingValue = this.cardRating.byValue(card);
	    double ratingAvailability = this.cardRating.byAvailability(card);
	    double ratingColorFrequency = this.cardRating
		    .byColorFrequency(card);
	    double ratingGoalDistance = this.cardRating.byGoalDistance(card);

	    int ratings = 6;
	    double overallRating = (ratingColor + ratingType + ratingValue
		    + ratingColorFrequency + ratingAvailability + ratingGoalDistance)
		    / ratings;
	    this.cardStackNeed.card.setValue(card, (byte) (overallRating * -1));

	    Debug.print(
		    this,
		    true,
		    String.format(
			    " %s: color(%2.2f) type(%2.2f) value(%2.2f) avail(%2.2f) colorFreq(%2.2f) distance(%2.2f)= %2.2f",
			    this.cardStack.card.toString(card), ratingColor,
			    ratingType, ratingValue, ratingAvailability,
			    ratingColorFrequency, ratingGoalDistance,
			    overallRating));
	    Debug.nl();
	}
    }

    /** Sort a card triple by values in the given stack */
    private byte[] sortCardTriple(CardStack stack, byte[] triple) {
	byte[] sortedTriple = new byte[] { CardStack.FLAG_UNINITIALIZED,
		CardStack.FLAG_UNINITIALIZED, CardStack.FLAG_UNINITIALIZED };

	for (int i = 0; i < 3; i++) {
	    for (int j = 0; j < 3; j++) {
		int sourceValue = (triple[i] == CardStack.FLAG_UNINITIALIZED) ? CardStack.FLAG_UNINITIALIZED
			: stack.card.getValue(triple[i]);
		int targetValue = (sortedTriple[j] == CardStack.FLAG_UNINITIALIZED) ? CardStack.FLAG_UNINITIALIZED
			: stack.card.getValue(sortedTriple[j]);
		if (sourceValue >= targetValue) {
		    // shift content..
		    switch (j) {
		    case 0:
			sortedTriple[2] = sortedTriple[1];
			sortedTriple[1] = sortedTriple[0];
			break;
		    case 1:
			sortedTriple[2] = sortedTriple[1];
			break;
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
    public void doMove(byte[] tableCards) {
	int index;
	double dropValue;
	Debug.println(this, "My stack: " + this.cardStack.toString());

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
	Debug.print(this, "My table cards rating: ");
	for (byte card : this.cardsTableTriple) {
	    Debug.print(String.format("%s:%d ",
		    this.cardStack.card.toString(card),
		    this.cardStackNeed.card.getValue(card)));
	}
	Debug.print("\n");
	// make a pick suggestion
	Debug.println(
		this,
		"Thought: I'll pick "
			+ this.cardStack.card
				.toString(this.cardsTableTriple[0]));

	// rate own cards
	this.rateCards();

	// dump rating stack
	if (Debug.debug == true) {
	    Debug.print(this, "My need stack:\n"
		    + this.cardStackNeed.dump().toString() + "\n");
	}

	// estimate goal distance
	if (Debug.debug == true) {
	    byte[] goalDistance = this.stackRating.goalDistance(this.cardStack
		    .getCards());
	    if ((goalDistance[0] != -1) || (goalDistance[2] != -1)) {
		Debug.print(this, "Goal distance(s): ");
		if (goalDistance[0] > -1) {
		    Debug.print(String.format("[%s?]:%d ",
			    CardStack.CARD_SYMBOLS[goalDistance[0]],
			    goalDistance[1]));
		}
		if (goalDistance[2] > -1) {
		    Debug.print(String.format("[?%s]:%d",
			    CardStack.CARD_NAMES[goalDistance[2]],
			    goalDistance[3]));
		}
		Debug.nl();
	    } else {
		Debug.println(this, "Goal not in sight.");
	    }
	}

	// make a drop suggestion
	byte[] cardToDrop = new byte[] { CardStack.FLAG_UNINITIALIZED,
		CardStack.FLAG_UNINITIALIZED };
	for (byte card : this.cardStack.getCards()) {
	    // be careful, card ratings are still noted as negative values here
	    byte cardValue = this.cardStackNeed.card.getValue(card);
	    if ((cardValue == 0) || (cardToDrop[1] == -1)
		    || (cardValue > cardToDrop[1])) {
		cardToDrop[0] = card;
		cardToDrop[1] = cardValue;
	    }
	}

	// end game?
	dropValue = this.stackRating.dropValue(this.cardStack.getCards());
	if ((this.gameIsClosed == false) && (dropValue > StackRating.NO_RESULT)) {
	    this.log("*knock!, knock!*");
	    SwimGame.getTable().interact(Table.Action.END_CALL,
		    this.cardStack.getCards());
	} else {
	    // drop & pick
	    if (SwimGame.getTable().interact(Table.Action.DROP_CARD,
		    cardToDrop[0])
		    && SwimGame.getTable().interact(Table.Action.PICK_CARD,
			    this.cardsTableTriple[0])) {
		this.cardStack.card.remove(cardToDrop[0]);
		this.cardStack.card.add(this.cardsTableTriple[0]);
	    }
	}
	// finished
	this.gameRound++;
	SwimGame.getTable().interact(Table.Action.MOVE_FINISHED);
    }

    @Override
    public void handleTableEvent(Table.Event event, Object data) {
	switch (event) {
	case INITIAL_CARDSTACK_DROPPED:
	    this.cardStackTable.card.add((byte[]) data);
	    break;
	case GAME_CLOSED:
	    this.gameIsClosed = true;
	    break;
	case GAME_START:
	    this.initialize();
	    break;
	case GAME_FINISHED:
	    this.gameIsFinished = true;
	    break;
	case CARD_DROPPED:
	    this.cardRating.cardSeen((byte) data);
	    break;
	}
    }
}
