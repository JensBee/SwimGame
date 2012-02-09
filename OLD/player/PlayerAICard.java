package cardGame.games.swimming;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import cardGame.card.CardDeck;
import cardGame.out.Debug;
import cardGame.util.Util;

/**
 * Functions for rating cards while playing. It's possible to set {@link Bias}
 * values to weight individual ratings handled by {@link Rate}.
 * 
 * @author <a href="mailto:code@jens-bertram.net">Jens Bertram</a>
 * 
 */
public class PlayerAICard {
    /** Toggle debugging output for this class. */
    private static final boolean DEBUG = false;

    private static final List<cardGame.card.CardDeck.Card> CARDDECK =
	    cardGame.card.CardDeck.Deck.SKAT.getCards();

    public static final int INFLUENCE_SAME_COLOR = 5;
    public static final int INFLUENCE_SAME_TYPE = 10;

    // fixed values
    /** Maximum value a card rating by type may reach. */
    protected static final int WORTH_MAX_BY_TYPE =
	    (2 * INFLUENCE_SAME_TYPE)
		    + PlayerAICard
			    .getNormalizedCardRating(cardGame.card.CardDeck.Card.CLUB_ACE)
		    + PlayerAICard
			    .getNormalizedCardRating(cardGame.card.CardDeck.Card.DIAMOND_ACE)
		    + PlayerAICard
			    .getNormalizedCardRating(cardGame.card.CardDeck.Card.HEART_ACE);
    /** Maximum value a card rating by color may reach. */
    private static final int WORTH_MAX_BY_COLOR =
	    (2 * PlayerAICard.INFLUENCE_SAME_COLOR)
		    + PlayerAICard
			    .getNormalizedCardRating(cardGame.card.CardDeck.Card.HEART_ACE)
		    + PlayerAICard
			    .getNormalizedCardRating(cardGame.card.CardDeck.Card.HEART_KING)
		    + PlayerAICard
			    .getNormalizedCardRating(cardGame.card.CardDeck.Card.HEART_QUEEN);

    // internal values
    /** {@link CardStack} used for pipelined processing. */
    private CardStack searchStack;
    /** {@link CardStack} with cards seen in a game. */
    private CardStack colorStack;
    /** Card used for pipelined processing. */
    private CardDeck.Card card;
    /** Rating for all pipelined processed. */
    private double pipelineRating = 0;
    /** Steps taken in pipelined processing. */
    private int pipelineSteps = 0;
    /** Store modified {@link Bias} settings */
    private final double[] bias = new double[Bias.values().length];
    /** Default value for an uninitialized bias setting. */
    private final int BIAS_UNSET = -1;

    /**
     * Allow finer grained and controllable ratings. These are the possible
     * biases to set with their default values. Modifying these values is
     * possible by using {@link PlayerAICard#setBiasValue(Bias, int)} or
     * {@link PlayerAICard#setBiasValue(Map)}
     * */
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

	/** Current value for this {@link Bias} instance. */
	private double value;

	/**
	 * Constructor.
	 * 
	 * @param newValue
	 *            New value for this {@link Bias} instance
	 */
	Bias(final double newValue) {
	    this.value = newValue;
	}

	/**
	 * Get the value of this {@link Bias} instance.
	 * 
	 * @return Current value for this {@link Bias} instance
	 */
	public final double getValue() {
	    return this.value;
	}
    }

    /**
     * These are the actual rating functions. They're kept in an {@link Enum}
     * type to be easy extensible and chained together.
     */
    public enum Rate {
	/**
	 * Rate a card by it's probability to be in the game. This means, if we
	 * have seen a card dropped on the table there's a possibility to get
	 * this single card to complete our stack.
	 */
	AVAILABILITY {
	    @Override
	    double rate(final CardStack stack, final CardDeck.Card cardToRate) {
		int maxValue = 0;
		for (cardGame.card.CardDeck.Card stackCard : stack.getCards()) {
		    byte searchStackValue = stack.getCardValue(stackCard);
		    if (searchStackValue > maxValue) {
			maxValue = searchStackValue;
		    }
		}
		if (DEBUG) {
		    double rating =
			    Util.normalize(AVAILABILITY_UNSEEN, maxValue,
				    stack.getCardValue(cardToRate));
		    Debug.printf(Debug.Level.INFO, PlayerAICard.class,
			    "byAvailability: %s %.2f\n", cardToRate, rating);
		    return rating;
		}
		return Util.normalize(AVAILABILITY_UNSEEN, maxValue,
			stack.getCardValue(cardToRate));
	    }
	},

	/** Rate a card based on it's influence on the possible goal-state. */
	GOAL_DISTANCE {
	    @Override
	    double rate(final CardStack stack, final CardDeck.Card cardToRate) {
		double rating;
		final Object[] distances =
			PlayerAIStackOLD.goalDistance(stack, stack.getCards());
		// bad
		if (distances[1] == null) {
		    rating = Util.normalize(-1, 1, -1);
		} else {
		    double colorValue = 0;
		    double typeValue = 0;
		    CardDeck.Color color = (CardDeck.Color) distances[0];
		    CardDeck.Type type = (CardDeck.Type) distances[2];
		    if (color.equals(cardToRate.getColor())) {
			colorValue =
				Util.normalize(-1, 1, (Integer) distances[1]);
		    } else if ((distances[2] != null)
			    && (type.equals(cardToRate.getType()))) {
			typeValue =
				Util.normalize(-1, 1, (Integer) distances[3]);
		    }

		    if (colorValue > typeValue) {
			rating = colorValue;
		    } else {
			rating = typeValue;
		    }
		}

		if (DEBUG) {
		    Debug.printf(Debug.INFO, PlayerAICard.class,
			    "byGoalDistance: %s %.2f\n", cardToRate, rating);
		}
		return rating;
	    }

	},

	/** Rate a card based on other cards of the same color. */
	SAME_COLOR {
	    @Override
	    double rate(final CardStack stack, final CardDeck.Card cardToRate) {
		final int cardValue =
			PlayerAICard
				.rateColorOrType(stack, cardToRate,
					CardStack.getCardsByColor(cardToRate
						.getColor()),
					PlayerAICard.INFLUENCE_SAME_COLOR);
		if (DEBUG) {
		    double rating =
			    Util.normalize(WORTH_MAX_BY_COLOR, cardValue);
		    Debug.printf(Debug.INFO, PlayerAICard.class,
			    "byColor: %s %.2f\n", cardToRate, rating);
		    return rating;
		}
		return Util.normalize(WORTH_MAX_BY_COLOR, cardValue);
	    }

	},

	/** Rate a card based on it's type and cards of the same type. */
	SAME_TYPE {
	    @Override
	    double rate(final CardStack stack, final CardDeck.Card cardToRate) {
		final int cardValue =
			PlayerAICard.rateColorOrType(stack, cardToRate,
				CardStack.getCardsByType(cardToRate.getType()),
				INFLUENCE_SAME_TYPE);

		if (DEBUG) {
		    double rating =
			    Util.normalize(WORTH_MAX_BY_TYPE, cardValue);
		    Debug.printf(Debug.INFO, PlayerAICard.class,
			    "byType: %s %.2f\n", cardToRate, rating);
		    return rating;
		}

		return Util.normalize(WORTH_MAX_BY_TYPE, cardValue);
	    }
	};

	// TODO: this is ugly!
	public static final int AVAILABILITY_UNSEEN = -5;

	/**
	 * General rating function.
	 * 
	 * @param stack
	 *            {@link CardStack} to use
	 * @param cardToRate
	 *            Card to rate
	 * @return Rating result
	 */
	abstract double rate(CardStack stack, CardDeck.Card cardToRate);
    }

    /** Empty constructor. */
    public PlayerAICard() {
	Arrays.fill(this.bias, this.BIAS_UNSET);
	this.reset();
    }

    /**
     * Set bias values for biased rating.
     * 
     * @param biasMap
     *            Map with {@link Bias} as key and a {@link Double} as value.
     *            The double must be in the range 0-1.
     */
    public final void setBiasValue(final Map<Bias, Double> biasMap) {
	for (Bias currentBias : biasMap.keySet()) {
	    this.setBiasValue(currentBias, biasMap.get(currentBias));
	}
    }

    /**
     * Set a single bias value.
     * 
     * @param biasName
     *            The bias to modify
     * @param value
     *            The new value to set
     */
    public final void setBiasValue(final Bias biasName, final double value) {
	if ((value < 0) || (value > 1)) {
	    throw new IllegalArgumentException(String.format(
		    "Bias value %f not in the range 0-1.", value));
	}
	this.bias[biasName.ordinal()] = value;
    }

    /**
     * Get a value for the given rating {@link Bias}.
     * 
     * @param biasName
     *            {@link Bias} to get the value for
     * @return Value set for the given {@link Bias}
     */
    public final double getBiasValue(final Bias biasName) {
	double biasValue = this.bias[biasName.ordinal()];
	if (biasValue > this.BIAS_UNSET) {
	    return biasValue;
	}
	return biasName.getValue();
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
    public final PlayerAICard pipeline(final CardStack newSearchStack,
	    final CardDeck.Card newCard) {
	this.reset();
	this.searchStack = newSearchStack;
	this.card = newCard;
	return this;
    }

    /**
     * Reset the rating of the pipeline. This is normally used between two
     * ratings in a single game.
     */
    private void resetPipeline() {
	// reset previous ratings
	this.pipelineSteps = 0;
	this.pipelineRating = 0;
    }

    /**
     * Reset the whole pipeline. This is normally used before running a new
     * game.
     */
    public final void reset() {
	this.resetPipeline();
	this.colorStack = new CardStack();
	this.searchStack = new CardStack();
	this.card = null;
    }

    /**
     * Get the overall rating for a pipelined processing. This function will
     * return the result of the last pipeline run. If there are no results of a
     * previous run it will run the default set of piped ratings and return its
     * results.
     * 
     * @return Overall rating of all pipelined ratings
     */
    public final double getRating() {
	if (this.pipelineSteps == 0) {
	    // run the default pipeline
	    for (Rate rating : Rate.values()) {
		this.pipelineSteps++;
		this.pipelineRating += this.biasRate(rating);
	    }
	}
	if (DEBUG) {
	    double rating = this.pipelineRating / this.pipelineSteps;
	    Debug.printf(Debug.INFO, PlayerAICard.class,
		    "getRating: %s: (%.2f/%d) = %.2f\n", this.card,
		    this.pipelineRating, this.pipelineSteps, rating);
	    return rating;
	}
	return this.pipelineRating / this.pipelineSteps;
    }

    /**
     * @see #getRating().
     * 
     * @param newSearchStack
     *            {@link CardStack} to use for rating
     * @param newCard
     *            The card to rate
     * @return Rating value for the given card
     */
    public final double getRating(final CardStack newSearchStack,
	    final CardDeck.Card newCard) {
	if (DEBUG) {
	    Debug.printf(Debug.INFO, PlayerAICard.class, "getRating: %s>>>\n",
		    newCard);
	}
	this.resetPipeline();
	this.searchStack = newSearchStack;
	this.card = newCard;
	if (DEBUG) {
	    double rating = this.getRating();
	    Debug.printf(Debug.INFO, PlayerAICard.class, "getRating: <<<%s\n",
		    newCard);
	    return rating;
	}
	return this.getRating();
    }

    /**
     * Rate a card using the defined {@link Bias} values.
     * 
     * @param rating
     *            The {@link Rate} rating to use
     * @return Rating result
     */
    public final double biasRate(final Rate rating) {
	double ratingResult =
		rating.rate(this.searchStack, this.card)
			* this.getBiasValue(Bias.valueOf(rating.name()));
	if (DEBUG) {
	    // need to wrap parameters in Object[] to avoid ambiguity
	    Debug.printf(
		    Debug.INFO,
		    "\t ^^ BIASED: %s(%.2f) = %.2f\n",
		    new Object[] { rating.name(),
			    this.getBiasValue(Bias.valueOf(rating.name())),
			    ratingResult });
	}
	return ratingResult;
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
     * @return Rating result
     */
    private static int rateColorOrType(final CardStack searchStack,
	    final CardDeck.Card card, final CardDeck.Card[] cardsArray,
	    final int influence) {
	int cardValue = 0;
	int same = 0;

	for (CardDeck.Card stackCard : cardsArray) {
	    if (searchStack.hasCard(stackCard)) {
		same++;
		cardValue =
			cardValue + PlayerAICard.getNormalizedCardRating(card);

		// modify rating if there are more cards
		cardValue = (same > 1) ? (cardValue + influence) : cardValue;
	    }
	}
	return cardValue;
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
    static double byColorFrequency(final byte[] colorIndex,
	    final CardDeck.Card card) {
	int maxValue = 0;
	for (byte value : colorIndex) {
	    if (value > maxValue) {
		maxValue = value;
	    }
	}

	if (maxValue == 0) {
	    return 0;
	}

	if (DEBUG) {
	    double rating =
		    Util.normalize(maxValue, colorIndex[card.getColor()
			    .ordinal()]);
	    Debug.printf(Debug.INFO, PlayerAICard.class,
		    "byColorFrequency: %s %.2f\n", card, rating);
	    return rating;
	}
	return Util.normalize(maxValue, colorIndex[card.getColor().ordinal()]);
    }

    /**
     * Rate a card based on it's color.
     * 
     * The rating is based on the amount of cards already seen by it's color.
     * The more cards of it's color were seen (in the game) the better the
     * rating gets. This is, because the probability to get a card of this color
     * is high.
     * 
     * @return Rating result
     */
    public final PlayerAICard byColorFrequency() {
	this.pipelineSteps++;
	byte[] colorIndex = new byte[CardStack.CARDS_MAX_COLOR];
	byte colorValue;
	for (CardDeck.Card currentCard : this.colorStack.getCards()) {
	    colorValue = colorIndex[currentCard.getColor().ordinal()];
	    colorIndex[this.card.getColor().ordinal()] = colorValue++;
	}
	double rating =
		(PlayerAICard.byColorFrequency(colorIndex, this.card) * this
			.getBiasValue(Bias.SAME_COLOR));
	this.pipelineRating += rating;
	if (DEBUG) {
	    Debug.printf(Debug.INFO, PlayerAICard.class,
		    "byColorFrequency (bias %.2f): %s %.2f\n",
		    this.getBiasValue(Bias.SAME_COLOR), this.card, rating);
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
    public static final int getNormalizedCardRating(final CardDeck.Card card) {
	final int value = PlayerAICard.getCardRating(card);
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
	if (DEBUG) {
	    Debug.printf(Debug.INFO, PlayerAICard.class,
		    "getNormalizedCardRating: %s %d\n", card, rating);
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
    public static final byte getCardRating(final CardDeck.Card card) {
	byte rating;
	byte cardIndex = (byte) CARDDECK.indexOf(card);

	if (cardIndex < CardStack.CARDS_MAX_CARD) {
	    rating = (byte) CARDDECK.indexOf(card);
	} else {
	    rating =
		    (byte) (cardIndex - ((cardIndex / CardStack.CARDS_MAX_CARD) * CardStack.CARDS_MAX_CARD));
	}

	if (DEBUG) {
	    Debug.printf(Debug.INFO, PlayerAICard.class,
		    "getCardRating: %s %d\n", card, rating);
	}
	return rating;
    }

    /**
     * Rate a card based on it's card value.
     * 
     * @param card
     *            Card to rate
     * @return Rating
     */
    static double byValue(final CardDeck.Card card) {
	if (DEBUG) {
	    double rating =
		    Util.normalize(CardStack.CARDS_MAX_CARD - 1,
			    PlayerAICard.getCardRating(card));
	    Debug.printf(Debug.INFO, PlayerAICard.class, "byValue: %s %.2f\n",
		    card, rating);
	    return rating;
	}

	return Util.normalize(CardStack.CARDS_MAX_CARD - 1,
		PlayerAICard.getCardRating(card));
    }

    /**
     * Add a card to the list of already seen cards. This list will be used by
     * {@link #byColorFrequency()}.
     * 
     * @param card
     *            Card seen
     */
    public final void cardSeen(final cardGame.card.CardDeck.Card card) {
	this.colorStack.addCard(card);
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
	    final CardDeck.Card card, final int newValue) {
	final int oldValue = searchStack.getCardValue(card);
	searchStack.setCardValue(card, (byte) (oldValue > newValue ? oldValue
		: newValue));
    }
}
