package swimgame.player;

import swimgame.out.Debug;
import swimgame.table.CardStack;
import swimgame.table.CardStack.CardIterator;
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
    private byte[] dropColorIndex = new byte[CardStack.CARDS_MAX_COLOR];
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
    private CardRating cardRating = null;
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
	this.cardStackTable
		.fill((byte) DefaultPlayer.CardRating.INFLUENCE_DEFAULT_UNSEEN);
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
     * Card rating functions.
     * 
     * @author <a href="mailto:code@jens-bertram.net">Jens Bertram</a>
     * 
     */
    private class CardRating {
	private static final double RATING_DEFAULT = -1;
	/** Stores ratings for later calculations. */
	private final double[] ratings = new double[] { RATING_DEFAULT,
		RATING_DEFAULT, RATING_DEFAULT, RATING_DEFAULT, RATING_DEFAULT,
		RATING_DEFAULT };
	private static final int RATING_COLOR = 0;
	private static final int RATING_COLORFREQUENCY = 1;
	private static final int RATING_TYPE = 2;
	private static final int RATING_VALUE = 3;
	private static final int RATING_AVAILABILITY = 4;
	private static final int RATING_GOALDISTANCE = 5;

	// rating influencing parameters
	private static final int INFLUENCE_SAME_COLOR = 5;
	private static final int INFLUENCE_SAME_TYPE = 10;
	/** Initial rating for cards we've not seen on the table. */
	private static final int INFLUENCE_DEFAULT_UNSEEN = -5;
	/** Maximum value a card rating by type may reach. */
	protected final int worth_max_by_type = (2 * CardRating.INFLUENCE_SAME_TYPE)
		+ this.getNormalizedCardRating(CardStack.STACK_SIZE - 1)
		+ this.getNormalizedCardRating(CardStack.STACK_SIZE - 2)
		+ this.getNormalizedCardRating(CardStack.STACK_SIZE - 3);
	/** Maximum value a card rating by color may reach. */
	private final int worth_max_by_color = (2 * CardRating.INFLUENCE_SAME_COLOR)
		+ this.getNormalizedCardRating(CardStack.STACK_SIZE - 1)
		+ this.getNormalizedCardRating(CardStack.STACK_SIZE - 2)
		+ this.getNormalizedCardRating(CardStack.STACK_SIZE - 3);

	/** General function to rate a card based on it's color or value. */
	private int rateColorOrType(final int card, final byte[] cardsArray,
		final int influence) {
	    int cardValue = 0;
	    int same = 0;

	    for (byte stackCard : cardsArray) {
		if (DefaultPlayer.this.cardStack.hasCard(stackCard)) {
		    same++;
		    cardValue = cardValue + this.getNormalizedCardRating(card);

		    // modify rating if there are more cards
		    cardValue = (same > 1) ? (cardValue + influence)
			    : cardValue;
		}
	    }
	    return cardValue;
	}

	/** Rate a card based on other cards of the same color. */
	protected double byColor(final int card) {
	    final int cardValue = this.rateColorOrType(card, CardStack
		    .getCardsByColor(DefaultPlayer.this.cardStack
			    .getCardColor(card)),
		    CardRating.INFLUENCE_SAME_COLOR);
	    this.ratings[RATING_COLOR] = Rating.normalize(
		    this.worth_max_by_color, cardValue);
	    return this.ratings[RATING_COLOR];
	}

	protected double byColorFrequency(final int card) {
	    int maxValue = 0;
	    for (byte value : DefaultPlayer.this.dropColorIndex) {
		maxValue = (value > maxValue) ? value : maxValue;
	    }

	    if (maxValue == 0) {
		this.ratings[RATING_COLORFREQUENCY] = 0;
	    } else {
		this.ratings[RATING_COLORFREQUENCY] = Rating
			.normalize(
				maxValue,
				DefaultPlayer.this.dropColorIndex[DefaultPlayer.this.cardStack
					.getCardColor(card)]);
	    }
	    return this.ratings[RATING_COLORFREQUENCY];
	}

	/** Rate a card based on it's type and cards of the same type. */
	protected double byType(final int card) {
	    final int cardValue = this.rateColorOrType(card,
		    CardStack.getCardsByType(CardStack.getCardType(card)),
		    CardRating.INFLUENCE_SAME_TYPE);
	    this.ratings[RATING_TYPE] = Rating.normalize(
		    this.worth_max_by_type, cardValue);
	    return this.ratings[RATING_TYPE];
	}

	/** Rate a card based on it's card value. */
	protected double byValue(final int card) {
	    this.ratings[RATING_VALUE] = Rating.normalize(
		    CardStack.CARDS_MAX_CARD - 1, this.getCardRating(card));
	    return this.ratings[RATING_VALUE];
	}

	protected double byAvailability(final int card) {
	    int maxValue = 0;
	    System.out.println("[C]csTable:"
		    + DefaultPlayer.this.cardStackTable.toString());
	    for (byte stackCard : DefaultPlayer.this.cardStackTable.getCards()) {
		maxValue = DefaultPlayer.this.cardStackTable
			.getCardValue(stackCard) > maxValue ? DefaultPlayer.this.cardStackTable
			.getCardValue(stackCard) : maxValue;
	    }
	    this.ratings[RATING_AVAILABILITY] = Rating.normalize(
		    DefaultPlayer.CardRating.INFLUENCE_DEFAULT_UNSEEN,
		    maxValue,
		    DefaultPlayer.this.cardStackTable.getCardValue(card));
	    return this.ratings[RATING_AVAILABILITY];
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
	protected void uprate(final CardStack stack, final int card,
		final int newValue) {
	    final int oldValue = stack.getCardValue(card);
	    stack.setCardValue(card, (byte) (oldValue > newValue ? oldValue
		    : newValue));
	}

	/**
	 * Get the rating for this card
	 * 
	 */
	protected byte getCardRating(final int card) {
	    byte rating;

	    CardStack.validateCardIndex(card);
	    if (card < CardStack.CARDS_MAX_CARD) {
		rating = (byte) card;
	    } else {
		rating = (byte) (card - ((card / CardStack.CARDS_MAX_CARD) * CardStack.CARDS_MAX_CARD));
	    }
	    return rating;
	}

	/**
	 * Get a normalized value of a card
	 * 
	 * <pre>
	 * J, Q, K -> 3; A -> 4 
	 * 7 -> 0; 8 -> 1; 9 -> 2 (basically their array locations)
	 * </pre>
	 */
	protected int getNormalizedCardRating(final int card) {
	    final int value = this.getCardRating(card);
	    return (value > 2) ? (value < 7 ? 3 : 4) : value;
	}

	protected void reset() {
	    DefaultPlayer.this.dropColorIndex = new byte[CardStack.CARDS_MAX_COLOR];
	}

	protected void cardSeen(final byte card) {
	    DefaultPlayer.this.cardStackTable.addCard(card);
	    DefaultPlayer.this.dropColorIndex[DefaultPlayer.this.cardStack
		    .getCardColor(card)] = (byte) (DefaultPlayer.this.dropColorIndex[DefaultPlayer.this.cardStack
		    .getCardColor(card)] + 1);
	}

	protected double byGoalDistance(final int card) {
	    double rating;
	    this.ratings[RATING_GOALDISTANCE] = 0;

	    final byte[] distances = DefaultPlayer.this.stackRating
		    .goalDistance(DefaultPlayer.this.cardStack.getCards());

	    // bad
	    if (distances[1] == -1) {
		this.ratings[RATING_GOALDISTANCE] = Rating.normalize(-1, 1,
			distances[1]);
		rating = this.ratings[RATING_GOALDISTANCE];
	    } else {

		double colorValue = 0;
		double typeValue = 0;
		if (distances[0] == DefaultPlayer.this.cardStack
			.getCardColor(card)) {
		    colorValue = Rating.normalize(-1, 1, distances[1]);
		} else if (distances[2] == CardStack.getCardType(card)) {
		    typeValue = Rating.normalize(-1, 1, distances[3]);
		}

		this.ratings[RATING_GOALDISTANCE] = (colorValue > typeValue) ? colorValue
			: typeValue;
		rating = this.ratings[RATING_GOALDISTANCE];
	    }
	    return rating;
	}

	/**
	 * Get the overall rating for all single ratings processed for a card
	 * between two resets
	 * 
	 * @return
	 */
	protected double getOverallRating() {
	    double overall = 0;
	    int ratingCount = 0;
	    for (int i = 0; i < this.ratings.length; i++) {
		if (this.ratings[i] != RATING_DEFAULT) {
		    ratingCount++;
		    overall = overall + this.ratings[i];
		}
	    }
	    return overall / ratingCount;
	}

	protected double getOverallRating(final int card) {
	    this.byAvailability(card);
	    this.byColor(card);
	    this.byColorFrequency(card);
	    this.byGoalDistance(card);
	    this.byType(card);
	    this.byValue(card);
	    return this.getOverallRating();
	}

	/**
	 * Dump the rating currently stored for a card
	 * 
	 * @return
	 */
	protected String dumpRating() {
	    StringBuffer ratingString = new StringBuffer();

	    if (this.ratings[RATING_COLOR] != RATING_DEFAULT) {
		ratingString.append(String.format(" color(%.2f)",
			this.ratings[RATING_COLOR]));
	    }
	    if (this.ratings[RATING_TYPE] != RATING_DEFAULT) {
		ratingString.append(String.format(" type(%.2f)",
			this.ratings[RATING_TYPE]));
	    }
	    if (this.ratings[RATING_VALUE] != RATING_DEFAULT) {
		ratingString.append(String.format(" value(%.2f)",
			this.ratings[RATING_VALUE]));
	    }
	    if (this.ratings[RATING_AVAILABILITY] != RATING_DEFAULT) {
		ratingString.append(String.format(" availability(%.2f)",
			this.ratings[RATING_AVAILABILITY]));
	    }
	    if (this.ratings[RATING_COLORFREQUENCY] != RATING_DEFAULT) {
		ratingString.append(String.format(" color-frequency(%.2f)",
			this.ratings[RATING_COLORFREQUENCY]));
	    }
	    if (this.ratings[RATING_GOALDISTANCE] != RATING_DEFAULT) {
		ratingString.append(String.format(" goal-distance(%.2f)",
			this.ratings[RATING_GOALDISTANCE]));
	    }

	    return String.format("%.2f =%s", this.getOverallRating(),
		    ratingString.toString());
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
		final int color = DefaultPlayer.this.cardStack
			.getCardColor(card);
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
		value = this.byValue();
	    } else {
		value = StackRating.NO_RESULT;
	    }
	    return value;
	}

	/** Returns the current value of the stack. */
	protected double byValue() {
	    final CardIterator sI = DefaultPlayer.this.cardStack.new CardIterator();

	    double value = 0;
	    int sameType = 0;

	    while (sI.hasNext()) {
		sameType = 0;
		double currentValue = 0;
		// skip if we don't own this card
		if (sI.next() == CardStack.FLAG_NO_CARD) {
		    continue;
		}

		// do we have the same card type?
		for (byte b : CardStack.getCardsByType(sI.getCardType())) {
		    if (DefaultPlayer.this.cardStack.getCardValue(b) == CardStack.FLAG_HAS_CARD) {
			sameType++;
		    }
		}

		// same type in three colors is fixed value
		if (sameType >= 3) {
		    return DefaultTableController.WORTH_THREE_OF_SAME_TYPE;
		}

		// do we have another card of the same color?
		for (byte card : CardStack.getCardsByColor(sI.getCardColor())) {
		    if (DefaultPlayer.this.cardStack.hasCard(card)) {
			// calculate the real value here
			currentValue = currentValue
				+ DefaultPlayer.this.cardRating
					.getNormalizedCardRating(card)
				+ (CardStack.CARDS_MAX_CARD - 1);
		    }
		}

		value = (currentValue > value) ? currentValue : value;
	    }

	    if ((sameType == 2) && (B_RISKYNESS >= 2)) {
		// MODEL:risk drop for three of same type
		Debug.println(DefaultPlayer.this,
			"#MODEL:risk Waiting for third card.");
		if (value < DefaultPlayer.this.behavior
			.get(PlayerConfiguration.WAIT_FOR_CARD)) {
		    value = (DefaultTableController.WORTH_THREE_OF_SAME_TYPE / 3) * 2;
		} else if (B_RISKYNESS > 2) {
		    Debug.println(DefaultPlayer.this,
			    "#MODEL:risk Waiting for third card, even threshold looks good.");
		}
	    }

	    if (Debug.debug) {
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
			if (!DefaultPlayer.this.cardStack.hasCard(cardToCheck)) {
			    int ratingValue = DefaultPlayer.this.cardRating
				    .getCardRating(cardToCheck)
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
				    .getCardRating(cardNum)] = 1;
			    for (int i = cardsColor; i < CardStack.CARDS_MAX_COLOR; i++) {
				int cardToCheck = cardNum + (i * cardOffset);
				if ((cardToCheck != ownedCard)
					&& DefaultPlayer.this.cardStack
						.hasCard(cardToCheck)) {
				    int ratingValue = DefaultPlayer.this.cardStackNeed
					    .getCardValue(cardToCheck)
					    + CardRating.INFLUENCE_SAME_TYPE
					    + (int) (DefaultTableController.WORTH_THREE_OF_SAME_TYPE / 3);

				    for (int fixedTypeCard = 0; fixedTypeCard < CardStack.CARDS_MAX_COLOR; fixedTypeCard++) {
					int cardToRate = cardNum
						+ (fixedTypeCard * cardOffset);
					if (!DefaultPlayer.this.cardStack
						.hasCard(cardToRate)) {
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
    public final void setCards(final byte[] cards) {
	super.setCards(cards);
	this.cardRating = new CardRating();
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
	Debug.println(this, "Deciding on my current card set: "
		+ this.cardStack);

	if (this.stackRating.byValue() < this.behavior
		.get(PlayerConfiguration.STACKDROP_INITIAL)) {
	    // drop immediately if blow threshold
	    Debug.println(this, String.format(
		    "Dropping (below threshold (%f))",
		    this.behavior.get(PlayerConfiguration.STACKDROP_INITIAL)));

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
	    this.cardRating = new CardRating();
	    this.cardStackNeed.setCardValue(card,
		    (byte) this.cardRating.getOverallRating(card));
	    if (Debug.debug) {
		Debug.println(this, String.format("Rating %-5s: %s",
			this.cardStack.cardToString(card),
			this.cardRating.dumpRating()));
	    }
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
		    this.cardStack.cardToString(card),
		    this.cardStackNeed.getCardValue(card)));
	}
	Debug.print("\n");
	// make a pick suggestion
	Debug.println(
		this,
		"Thought: I'll pick "
			+ this.cardStack.cardToString(this.cardsTableTriple[0]));

	// rate own cards
	this.rateCards();

	// dump rating stack
	if (Debug.debug) {
	    Debug.print(this, "My need stack:\n"
		    + this.cardStackNeed.dump().toString() + "\n");
	}

	// estimate goal distance
	byte[] goalDistance = this.stackRating.goalDistance(this.cardStack
		.getCards());
	if (Debug.debug) {
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

	// stack value
	dropValue = this.stackRating.dropValue(this.cardStack.getCards());
	Debug.println(this, "Drop value: " + dropValue);

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
			Debug.println(this,
				"#MODEL:risk Waiting for third card.");
			continue;
		    } else if (B_RISKYNESS > 2) {
			Debug.println(this,
				"#MODEL:risk Waiting for third card, event threshold is good.");
		    } else {
			Debug.println(this,
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