package swimgame.player.rating;

import swimgame.Util;
import swimgame.table.CardStack;

/**
 * 
 * @author <a href="mailto:code@jens-bertram.net">Jens Bertram</a>
 * 
 */
public class Card {
    // values that should be changeable
    static final int AVAILABILITY_UNSEEN = -5;
    private static final int INFLUENCE_SAME_COLOR = 5;
    private static final int INFLUENCE_SAME_TYPE = 10;

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
    /** Card used for pipelined processing. */
    private int card;
    /** Rating for all pipelined processed. */
    private double pipelineRating = 0;
    /** Steps taken in pipelined processing. */
    private int pipelineSteps = 0;

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
	// reset previous ratings
	this.pipelineSteps = 0;
	this.pipelineRating = 0;
	// init pipe variables
	this.searchStack = newSearchStack;
	this.card = newCard;
	// ready to run
	return this;
    }

    /**
     * Get the overall rating for a pipelined processing.
     * 
     * @return Overall rating of all pipelined ratings
     */
    public double getRating() {
	return this.pipelineRating / this.pipelineSteps;
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
	this.pipelineRating += Card.byAvailability(this.searchStack, this.card);
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
    static double byColor(final CardStack searchStack, final int card) {
	final int cardValue = Card.rateColorOrType(searchStack, card,
		CardStack.getCardsByColor(CardStack.getCardColor(card)),
		Card.INFLUENCE_SAME_COLOR);
	return Util.normalize(WORTH_MAX_BY_COLOR, cardValue);
    }

    /**
     * Pipelined version of {@link Card#byColor(CardStack, int)}
     * 
     * @return Card
     */
    final Card byColor() {
	this.pipelineSteps++;
	this.pipelineRating += Card.byColor(this.searchStack, this.card);
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
	return Util.normalize(maxValue,
		colorIndex[CardStack.getCardColor(card)]);
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
    static int getNormalizedCardRating(final int card) {
	final int value = Card.getCardRating(card);
	if (value > 2) {
	    if (value < 7) {
		return 3;
	    } else {
		return 4;
	    }
	} else {
	    return value;
	}
    }

    /**
     * Get the rating for a card.
     * 
     * @param card
     *            Card to rate
     * @return Rating
     */
    static byte getCardRating(final int card) {
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
	return rating;
    }

    /**
     * Pipelined version of {@link Card#byGoalDistance(CardStack, int)}.
     * 
     * @return Card
     */
    final Card byGoalDistance() {
	this.pipelineSteps++;
	this.pipelineRating += Card.byGoalDistance(this.searchStack, this.card);
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
    static double byType(final CardStack searchStack, final int card) {
	final int cardValue = Card.rateColorOrType(searchStack, card,
		CardStack.getCardsByType(CardStack.getCardType(card)),
		INFLUENCE_SAME_TYPE);
	return Util.normalize(WORTH_MAX_BY_TYPE, cardValue);
    }

    /**
     * Pipelined version of {@link Card#byType(CardStack, int)}.
     * 
     * @return Card
     */
    final Card byType() {
	this.pipelineSteps++;
	this.pipelineRating += Card.byType(this.searchStack, this.card);
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
	return Util.normalize(CardStack.CARDS_MAX_CARD - 1,
		Card.getCardRating(card));
    }

    /**
     * Pipelined version of {@link Card#byValue(int)}.
     * 
     * @return Card
     */
    final Card byValue() {
	this.pipelineSteps++;
	this.pipelineRating += Card.byValue(this.card);
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
    protected void uprate(final CardStack searchStack, final int card,
	    final int newValue) {
	final int oldValue = searchStack.getCardValue(card);
	searchStack.setCardValue(card, (byte) (oldValue > newValue ? oldValue
		: newValue));
    }
}
