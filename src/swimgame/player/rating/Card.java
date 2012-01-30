package swimgame.player.rating;

import java.util.Map;
import java.util.TreeMap;

import swimgame.Util;
import swimgame.out.Debug;
import swimgame.table.CardStack;

/**
 * Functions for rating card while playing. Most of the functions could be
 * accessed by static reference. However the Card object allows to build a
 * chained (or pipelined) rating. Further you're able to set weights for all
 * possible ratings.
 * 
 * @author <a href="mailto:code@jens-bertram.net">Jens Bertram</a>
 * 
 */
public class Card {
    // values that should be changeable
    public static final int AVAILABILITY_UNSEEN = -5;

    public static final int INFLUENCE_SAME_COLOR = 5;
    public static final int INFLUENCE_SAME_TYPE = 10;

    /** Rating bias configuration. */
    private final Map<String, Double> bias = new TreeMap<String, Double>();

    // fixed values
    /** Maximum value a card rating by type may reach. */
    protected final static int WORTH_MAX_BY_TYPE = (2 * INFLUENCE_SAME_TYPE)
	    + Card.getNormalizedCardRating(CardStack.STACK_SIZE - 1)
	    + Card.getNormalizedCardRating(CardStack.STACK_SIZE - 2)
	    + Card.getNormalizedCardRating(CardStack.STACK_SIZE - 3);
    /** Maximum value a card rating by color may reach. */
    private final static int WORTH_MAX_BY_COLOR = (2 * Card.INFLUENCE_SAME_COLOR)
	    + Card.getNormalizedCardRating(CardStack.STACK_SIZE - 1)
	    + Card.getNormalizedCardRating(CardStack.STACK_SIZE - 2)
	    + Card.getNormalizedCardRating(CardStack.STACK_SIZE - 3);

    // internal values
    /** {@link CardStack} used for pipelined processing. */
    private CardStack searchStack;
    /** {@link CardStack} with cards seen in a game. */
    private CardStack colorStack;
    /** Card used for pipelined processing. */
    private int card;
    /** Rating for all pipelined processed. */
    private double pipelineRating = 0;
    /** Steps taken in pipelined processing. */
    private int pipelineSteps = 0;

    /** Allow finer grained and controllable ratings. */
    public enum Bias {
	/** TODO: document bias. */
	AVAILABILITY(0.5),
	/** */
	SAME_COLOR(0.5),
	/** */
	SAME_TYPE(0.5),
	/** */
	VALUE(0.5),
	/** */
	GOAL_DISTANCE(0.5);

	private double value;

	Bias(final double newValue) {
	    this.setValue(newValue);
	}

	public double getValue() {
	    return this.value;
	}

	void setValue(final double newValue) {
	    this.value = newValue;
	}
    }

    public Card() {
	this.reset();
    }

    public void setBias(final Map<Bias, Double> biasMap) {
	for (Bias bias : biasMap.keySet()) {
	    bias.setValue(biasMap.get(bias));
	}
    }

    public void setBias(final Bias bias, final double value) {
	bias.setValue(value);
    }

    /**
     * Setup the variables needed to run the pipeline. This should be run at the
     * beginning of each pipelined processing.
     * 
     * 
     * <pre>
     * 	Example:
     *  cardPipe = new Card();
     * 	cardPipe.pipeline(CardStack, Card).pipe1().pipe2().pipe..
     * 	double rating = cardPipe.getRating();
     * </pre>
     * 
     * @param newSearchStack
     *            {@link CardStack} to use
     * @param newCard
     *            Card to check
     * @return Card
     */
    public final Card pipeline(final CardStack newSearchStack, final int newCard) {
	this.resetPipeline();
	// init pipe variables
	this.searchStack = newSearchStack;
	this.card = newCard;
	// ready to run
	return this;
    }

    private void resetPipeline() {
	// reset previous ratings
	this.pipelineSteps = 0;
	this.pipelineRating = 0;
    }

    public void reset() {
	this.resetPipeline();
	this.colorStack = new CardStack();
	this.searchStack = new CardStack();
	this.card = 0;
    }

    /**
     * Get the overall rating for a pipelined processing.
     * 
     * @return Overall rating of all pipelined ratings
     */
    public final double getRating() {
	if (this.pipelineSteps == 0) {
	    // run the default pipeline
	    this.byAvailability();
	    this.byColor();
	    this.byColorFrequency();
	    this.byGoalDistance();
	    this.byType();
	    this.byValue();
	}
	if (Debug.debug) {
	    double rating = this.pipelineRating / this.pipelineSteps;
	    Debug.printf(Debug.INFO, Card.class,
		    "getRating: %s: (%.2f/%d) = %.2f\n",
		    CardStack.cardToString(this.card), this.pipelineRating,
		    this.pipelineSteps, rating);
	    return rating;
	}
	return this.pipelineRating / this.pipelineSteps;
    }

    public final double getRating(final CardStack newSearchStack, final int card) {
	Debug.printf(Debug.INFO, Card.class, "getRating: %s>>>\n",
		CardStack.cardToString(card));
	this.resetPipeline();
	this.searchStack = newSearchStack;
	this.card = card;
	if (Debug.debug) {
	    double rating = this.getRating();
	    Debug.printf(Debug.INFO, Card.class, "getRating: <<<%s\n",
		    CardStack.cardToString(card));
	    return rating;
	}
	return this.getRating();
    }

    /**
     * Rate a card by it's probability to be in the game. This means, if we have
     * seen a card dropped on the table there's a possibility to get this single
     * card to complete our stack.
     * 
     * @param searchStack
     *            The card stack with all seen cards to search in
     * @param card
     *            Card to check
     * @return Rating
     */
    static double byAvailability(final CardStack searchStack, final int card) {
	int maxValue = 0;
	for (byte stackCard : searchStack.getCards()) {
	    byte searchStackValue = searchStack.getCardValue(stackCard);
	    if (searchStackValue > maxValue) {
		maxValue = searchStackValue;
	    }
	}
	if (Debug.debug) {
	    double rating = Util.normalize(AVAILABILITY_UNSEEN, maxValue,
		    searchStack.getCardValue(card));
	    Debug.printf(Debug.INFO, Card.class, "byAvailability: %s %.2f\n",
		    CardStack.cardToString(card), rating);
	    return rating;
	}
	return Util.normalize(AVAILABILITY_UNSEEN, maxValue,
		searchStack.getCardValue(card));
    }

    /**
     * Pipelined version of {@link Card#byAvailability(CardStack, int)}.
     * 
     * @return Card
     */
    final Card byAvailability() {
	this.pipelineSteps++;
	double rating = (Card.byAvailability(this.searchStack, this.card) * Bias.AVAILABILITY
		.getValue());
	this.pipelineRating += rating;
	if (Debug.debug) {
	    Debug.printf(Debug.INFO, Card.class,
		    "byAvailability (bias): %s %.2f\n",
		    CardStack.cardToString(this.card), rating);
	}
	return this;
    }

    /**
     * General function to rate a card based on it's color or value.
     * 
     * @param searchStack
     *            CardStack to search in
     * @param card
     *            Card to check
     * @param cardsArray
     * @param influence
     * @return
     */
    private static int rateColorOrType(final CardStack searchStack,
	    final int card, final byte[] cardsArray, final int influence) {
	int cardValue = 0;
	int same = 0;

	for (byte stackCard : cardsArray) {
	    if (searchStack.hasCard(stackCard)) {
		same++;
		cardValue = cardValue + Card.getNormalizedCardRating(card);

		// modify rating if there are more cards
		cardValue = (same > 1) ? (cardValue + influence) : cardValue;
	    }
	}
	return cardValue;
    }

    /**
     * Rate a card based on other cards of the same color.
     * 
     * @param searchStack
     *            CardStack to search in
     * @param card
     *            Card to rate
     * @return Rating
     */
    static double bySameColor(final CardStack searchStack, final int card) {
	final int cardValue = Card.rateColorOrType(searchStack, card,
		CardStack.getCardsByColor(CardStack.getCardColor(card)),
		Card.INFLUENCE_SAME_COLOR);
	if (Debug.debug) {
	    double rating = Util.normalize(WORTH_MAX_BY_COLOR, cardValue);
	    Debug.printf(Debug.INFO, Card.class, "byColor: %s %.2f\n",
		    CardStack.cardToString(card), rating);
	    return rating;
	}
	return Util.normalize(WORTH_MAX_BY_COLOR, cardValue);
    }

    /**
     * Pipelined version of {@link Card#bySameColor(CardStack, int)}
     * 
     * @return Card
     */
    final Card byColor() {
	this.pipelineSteps++;
	double rating = (Card.bySameColor(this.searchStack, this.card) * Bias.SAME_COLOR
		.getValue());
	this.pipelineRating += rating;
	if (Debug.debug) {
	    Debug.printf(Debug.INFO, Card.class, "byColor (bias): %s %.2f\n",
		    CardStack.cardToString(this.card), rating);
	}
	return this;
    }

    /**
     * 
     * @param colorIndex
     *            Byte array with each position relates to a color ({@see
     *            CardStack}). At each position the amount of cards dropped with
     *            this color should be stored.
     * 
     *            <pre>
     *            For example:
     *            ♦ [0:amount]
     * 		  ♥ [1:amount]
     * 		  ♠ [2:amount]
     * 		  ♣ [3:amount]
     * </pre>
     * @param card
     *            Card to test against the collected color amount
     * @return Rating
     */
    static double byColorFrequency(final byte[] colorIndex, final int card) {
	int maxValue = 0;
	for (byte value : colorIndex) {
	    if (value > maxValue) {
		maxValue = value;
	    }
	}

	if (maxValue == 0) {
	    return 0;
	}

	if (Debug.debug) {
	    double rating = Util.normalize(maxValue,
		    colorIndex[CardStack.getCardColor(card)]);
	    Debug.printf(Debug.INFO, Card.class, "byColorFrequency: %s %.2f\n",
		    CardStack.cardToString(card), rating);
	    return rating;
	}
	return Util.normalize(maxValue,
		colorIndex[CardStack.getCardColor(card)]);
    }

    public final Card byColorFrequency() {
	this.pipelineSteps++;
	byte[] colorIndex = new byte[CardStack.CARDS_MAX_COLOR];
	byte colorValue;
	for (byte currentCard : this.colorStack.getCards()) {
	    colorValue = colorIndex[CardStack.getCardColor(currentCard)];
	    colorIndex[CardStack.getCardColor(this.card)] = colorValue++;
	}
	double rating = (Card.byColorFrequency(colorIndex, this.card) * Bias.SAME_COLOR
		.getValue());
	this.pipelineRating += rating;
	if (Debug.debug) {
	    Debug.printf(Debug.INFO, Card.class,
		    "byColorFrequency (bias): %s %.2f\n",
		    CardStack.cardToString(this.card), rating);
	}
	return this;
    }

    /**
     * Get a normalized value of a card.
     * 
     * <pre>
     * J, Q, K -> 3; A -> 4 
     * 7 -> 0; 8 -> 1; 9 -> 2 (basically their array locations)
     * </pre>
     * 
     * @param card
     *            Card to rate
     * @return Rating
     */
    public static final int getNormalizedCardRating(final int card) {
	final int value = Card.getCardRating(card);
	int rating = 0;
	// CHECKSTYLE:OFF
	if (value > 2) {
	    if (value < 7) {
		rating = 3;
	    } else {
		rating = 4;
	    }
	} else {
	    rating = value;
	}
	// CHECKSTYLE:ON
	if (Debug.debug) {
	    Debug.printf(Debug.INFO, Card.class,
		    "getNormalizedCardRating: %s %d\n",
		    CardStack.cardToString(card), rating);
	}
	return rating;
    }

    /**
     * Get the rating for a card.
     * 
     * @param card
     *            Card to rate
     * @return Rating
     */
    public static final byte getCardRating(final int card) {
	byte rating;

	CardStack.validateCardIndex(card);
	if (card < CardStack.CARDS_MAX_CARD) {
	    rating = (byte) card;
	} else {
	    rating = (byte) (card - ((card / CardStack.CARDS_MAX_CARD) * CardStack.CARDS_MAX_CARD));
	}

	// if (Debug.debug) {
	// Debug.printf(Debug.INFO, Card.class, "getCardRating: %s %d\n",
	// CardStack.cardToString(card), rating);
	// }
	return rating;
    }

    /**
     * Rate a card based on it's influence on the possible goal-state.
     * 
     * @param searchStack
     *            The stack with the cards available
     * @param card
     *            Card to rate
     * @return Rating
     */
    static double byGoalDistance(final CardStack searchStack, final int card) {
	double rating;

	final byte[] distances = Stack.goalDistance(searchStack,
		searchStack.getCards());

	// bad
	if (distances[1] == -1) {
	    rating = Util.normalize(-1, 1, distances[1]);
	} else {

	    double colorValue = 0;
	    double typeValue = 0;
	    if (distances[0] == CardStack.getCardColor(card)) {
		colorValue = Util.normalize(-1, 1, distances[1]);
	    } else if (distances[2] == CardStack.getCardType(card)) {
		typeValue = Util.normalize(-1, 1, distances[3]);
	    }

	    if (colorValue > typeValue) {
		rating = colorValue;
	    } else {
		rating = typeValue;
	    }
	}

	if (Debug.debug) {
	    Debug.printf(Debug.INFO, Card.class, "byGoalDistance: %s %.2f\n",
		    CardStack.cardToString(card), rating);
	}
	return rating;
    }

    /**
     * Pipelined version of {@link Card#byGoalDistance(CardStack, int)}.
     * 
     * @return Card
     */
    final Card byGoalDistance() {
	this.pipelineSteps++;
	double rating = (Card.byGoalDistance(this.searchStack, this.card) * Bias.GOAL_DISTANCE
		.getValue());
	this.pipelineRating += rating;
	if (Debug.debug) {
	    Debug.printf(Debug.INFO, Card.class,
		    "byGoalDistance (bias): %s %.2f\n",
		    CardStack.cardToString(this.card), rating);
	}
	return this;
    }

    /**
     * Rate a card based on it's type and cards of the same type.
     * 
     * @param searchStack
     *            CardStack to search in
     * @param card
     *            Card to check
     * @return Rating
     */
    static double bySameType(final CardStack searchStack, final int card) {
	final int cardValue = Card.rateColorOrType(searchStack, card,
		CardStack.getCardsByType(CardStack.getCardType(card)),
		INFLUENCE_SAME_TYPE);

	if (Debug.debug) {
	    double rating = Util.normalize(WORTH_MAX_BY_TYPE, cardValue);
	    Debug.printf(Debug.INFO, Card.class, "byType: %s %.2f\n",
		    CardStack.cardToString(card), rating);
	    return rating;
	}

	return Util.normalize(WORTH_MAX_BY_TYPE, cardValue);
    }

    /**
     * Pipelined version of {@link Card#bySameType(CardStack, int)}.
     * 
     * @return Card
     */
    final Card byType() {
	this.pipelineSteps++;
	double rating = (Card.bySameType(this.searchStack, this.card) * Bias.SAME_TYPE
		.getValue());
	this.pipelineRating += rating;
	if (Debug.debug) {
	    Debug.printf(Debug.INFO, Card.class, "byType (bias): %s %.2f\n",
		    CardStack.cardToString(this.card), rating);
	}
	return this;
    }

    /**
     * Rate a card based on it's card value.
     * 
     * @param card
     *            Card to rate
     * @return Rating
     */
    static double byValue(final int card) {
	if (Debug.debug) {
	    double rating = Util.normalize(CardStack.CARDS_MAX_CARD - 1,
		    Card.getCardRating(card));
	    Debug.printf(Debug.INFO, Card.class, "byValue: %s %.2f\n",
		    CardStack.cardToString(card), rating);
	    return rating;
	}

	return Util.normalize(CardStack.CARDS_MAX_CARD - 1,
		Card.getCardRating(card));
    }

    public void cardSeen(byte card) {
	this.colorStack.addCard(card);
    }

    /**
     * Pipelined version of {@link Card#byValue(int)}.
     * 
     * @return Card
     */
    final Card byValue() {
	this.pipelineSteps++;
	double rating = (Card.byValue(this.card) * Bias.VALUE.getValue());
	this.pipelineRating += rating;
	if (Debug.debug) {
	    Debug.printf(Debug.INFO, Card.class, "byValue (bias): %s %.2f\n",
		    CardStack.cardToString(this.card), rating);
	}
	return this;
    }

    /**
     * Shortcut to assign a new rating to a card, only if it's higher than the
     * existing rating.
     * 
     * @param searchStack
     *            The stack to search in
     * @param card
     *            The card to work with
     * @param newValue
     *            The new value we try to assign
     */
    public static final void uprate(final CardStack searchStack,
	    final int card, final int newValue) {
	final int oldValue = searchStack.getCardValue(card);
	searchStack.setCardValue(card, (byte) (oldValue > newValue ? oldValue
		: newValue));
    }
}
