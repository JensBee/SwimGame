package swimGame.player;

import swimGame.cards.CardStack;
import swimGame.cards.CardStack.StackIterator;
import swimGame.cards.CardUtils;
import swimGame.out.Debug;

/**
 * A player playing the game. The default implementation of a player.
 * 
 * @author Jens Bertram <code@jens-bertram.net>
 * 
 */
public class DefaultPlayer extends AbstractPlayer {
	private final CardUtils cardUtils = new CardUtils();
	/**
	 * The following variables (self.*) define the players character, Their
	 * values values range between low:1 and high:9
	 */
	private final byte selfRiskyness = 9;

	// additional flags for the stack-array
	private static final byte FLAG_CARD_TAKEN = -2;
	private static final byte FLAG_CARD_DROPPED = -3;

	/** Constructor */
	public DefaultPlayer() {
		super();
	}

	/**
	 * Decide (if able to), if our initial card set is good enough.
	 */
	@Override
	public boolean keepCardSet() {
		// this.cards.setCardStack(this.cardStack);
		byte[] cards = this.rateCards();
		Debug.println(
				this,
				String.format(
						"Looks like I should drop %s.",
						this.cardUtils.cardToString(new int[] { cards[6],
								cards[7] })));
		try {
			Debug.println(this,
					String.format("Stack value is %.1f.", this.getStackValue()));
		} catch (IllegalStateException e) {
			// ok, we're not ready yet
			Debug.println(this,
					String.format("Whole stack is not dropable yet."));
		}

		return true;
	}

	/**
	 * Check if the cards we own can be used to end the game
	 * 
	 * @return True if we can drop
	 */
	private boolean cardsDropable() {
		return true;
	}

	/**
	 * Rate the cards in our stack to choose one we will probably drop
	 * 
	 * @return
	 */
	private byte[] rateCards() {
		// get a copy of out card-stack
		byte[][] cardStackArray = this.cardStack.getArray();
		// store three cards (x,y) with one rating (z) each
		final byte[] rating = new byte[9];
		// rating array index pointer
		int ratingCount = 0;

		Debug.println(this, "Rating my current cards..");

		StackIterator sI = this.cardStack.new StackIterator();
		while (sI.hasNext()) {
			// skip if we don't own this card
			if (sI.next() == CardStack.FLAG_NO_CARD) {
				continue;
			}
			// we do - so rate it
			rating[ratingCount++] = (byte) sI.getCard();
			rating[ratingCount++] = (byte) sI.getColor();
			rating[ratingCount++] = (byte) this.rateCard_byValue(new int[] {
					sI.getCard(), sI.getColor() });

			Debug.print(this, true, String.format(
					"Ratings for %s: color(%d) type(%d) value(%d)",
					this.cardUtils.cardToString(new int[] { sI.getCard(),
							sI.getColor() }),//
					this.rateCard_byColor(new int[] { sI.getCard(),
							sI.getColor() }),//
					this.rateCard_byType(new int[] { sI.getCard(),
							sI.getColor() }),//
					this.rateCard_byValue(new int[] { sI.getCard(),
							sI.getColor() })//
					));
			Debug.print("\n");
		}

		return this.rankCardRating(rating);
	}

	// rating influencing parameters
	private final byte INF_SAME_COLOR = 10;
	private final byte INF_SAME_TYPE = 0;

	// the maximum value a stack rating may reach
	private final int cardStackMaxRating = (2 * this.INF_SAME_TYPE)
			+ this.getStackCardValue(CardStack.CARDS_MAX)
			+ this.getStackCardValue(CardStack.CARDS_MAX - 1)
			+ this.getStackCardValue(CardStack.CARDS_MAX - 2);

	protected int normalizeRating(double max, double value) {
		return new Double(((value - 0) / (max - 0)) * 10).intValue();
	}

	/**
	 * Calculates the value of a card belonging to a stack
	 * 
	 * @param card
	 *            The number representing the card in the stack
	 * @return The card value
	 */
	protected int getStackCardValue(int card) {
		// normalize card values
		if (card > 2) {
			// B, D, K -> 3; A -> 4
			return card < 7 ? 3 : 4;
		} else {
			// 7 -> 0; 8 -> 1; 9 -> 2 (basically their array locations)
			return card;
		}
	}

	protected double getStackValue() throws IllegalStateException {
		int count = 0;
		double value = 0;
		for (byte color = 0; color < (CardStack.CARDS_MAX_COLOR); color++) {
			byte[] cardsByColor = this.cardStack.getCardsByColor(color);
			for (byte card : cardsByColor) {
				if (card == CardStack.FLAG_UNINITIALIZED) {
					continue;
				}
				count++;
				value = value + this.getStackCardValue(card) + 7;
			}
			if (count > 0) {
				// Debug.println(
				// this.getClass(),
				// "Color:" + color + " cards:" + count + " value:"
				// + value + " norm:"
				// + this.normalizeRating(31, value));
				if (count == 3) {
					// return the max, if we reached it
					if (value == 31) {
						return 31;
					}
				}
			}
			count = 0;
			value = 0;
		}

		for (byte cardType = 0; cardType < (CardStack.CARDS_MAX_CARD); cardType++) {
			byte[] cardsByType = this.cardStack.getCardsByType(cardType);
			for (byte card : cardsByType) {
				if (card == CardStack.FLAG_UNINITIALIZED) {
					continue;
				}
				count++;
				value = value + this.getStackCardValue(card) + 7;
			}
			if (count > 0) {
				// Debug.println(
				// this.getClass(),
				// "Card:" + cardType + " cards:" + count + " value:"
				// + value + " norm:"
				// + this.normalizeRating(30.5, value));
				if (count == 3) {
					// return the max, if we reached it
					return 30.5;
				}
			}
			count = 0;
			value = 0;
		}
		throw new IllegalStateException(
				"Your cardstack isn't ready to drop yet!");
	}

	/**
	 * Rate a card based on other cards of the same color
	 * 
	 * @param aCard
	 * @return
	 */
	private int rateCard_byColor(int[] aCard) {
		// the rating for this card
		int cardValue = 0;
		// store occourances [card, color]
		int same = 0;

		// go through cards by same color
		for (int card = 0; card < CardStack.CARDS_MAX_CARD; card++) {
			// ..to check if we own it..
			if (this.cardStack.getArray()[card][aCard[1]] == 0) {
				// ..no we don't - check next
				continue;
			}
			same++;
			cardValue = cardValue + this.getStackCardValue(card);

			// modify rating if there are more cards of this color
			if (same > 1) {
				cardValue = cardValue + this.INF_SAME_COLOR;
			}
		}

		return this.normalizeRating(this.cardStackMaxRating, cardValue);
	}

	/**
	 * Rate a card based on it's type and cards of the same type
	 * 
	 * @param aCard
	 * @return
	 */
	private int rateCard_byType(int[] aCard) {
		// the rating for this card
		int cardValue = 0;
		// store occourances [card, color]
		int same = 0;

		// go through cards by same type
		for (int color = 0; color < (CardStack.CARDS_MAX_COLOR); color++) {
			// ..to check if we own it..
			if (this.cardStack.getArray()[aCard[0]][color] == 0) {
				// ..no we don't - check next
				continue;
			}
			same++;
			cardValue = cardValue + this.getStackCardValue(aCard[0]);

			// modify rating if there are more cards of this type
			if (same > 1) {
				cardValue = cardValue + this.INF_SAME_TYPE;
			}
		}

		return this.normalizeRating(this.cardStackMaxRating, cardValue);
	}

	/**
	 * Rate a card based on it's card value
	 * 
	 * @param card
	 *            int[card][color]
	 * @return
	 */
	protected int rateCard_byValue(int[] aCard) {
		// the rating for this card
		int cardValue = 0;
		// store occourances [card, color]
		byte same[] = new byte[] { 0, 0 };

		// go through cards by same type
		for (int color = 0; color < (CardStack.CARDS_MAX_COLOR); color++) {
			// ..to check if we own it..
			if (this.cardStack.getArray()[aCard[0]][color] == 0) {
				// ..no we don't - check next
				continue;
			}
			same[0] = same[0]++;
			cardValue = cardValue + this.getStackCardValue(aCard[0]);

			// modify rating if there are more cards of this type
			if (same[0] > 1) {
				cardValue = cardValue + this.INF_SAME_TYPE;
			}
		}

		// go through cards by same color
		for (int card = 0; card < CardStack.CARDS_MAX_CARD; card++) {
			// ..to check if we own it..
			if (this.cardStack.getArray()[card][aCard[1]] == 0) {
				// ..no we don't - check next
				continue;
			}
			same[1] = same[1]++;
			cardValue = cardValue + this.getStackCardValue(card);

			// modify rating if there are more cards of this color
			if (same[1] > 1) {
				cardValue = cardValue + this.INF_SAME_COLOR;
			}
		}

		return this.normalizeRating(this.cardStackMaxRating, cardValue);
	}

	/**
	 * Check, how many cards are missing to reach a goal state
	 * 
	 * @return
	 */
	private int rateStack_goalDistance() {
		return 0;
	}

	/**
	 * Sort the ranked cards by their rating value (best-first).
	 * 
	 * @param rating
	 *            The ranked cards as flat byte array
	 * @return The ranked cards as flat byte-array
	 */
	private byte[] rankCardRating(byte[] rating) {
		// shortcuts do clean it up a bit
		byte val1 = rating[2];
		byte val2 = rating[5];
		byte val3 = rating[8];
		if (val1 > val2) {
			// val1 > val2
			if (val1 > val3) {
				// (val1 > val2) && (val1 > val3)
				if (val2 > val3) {
					// val1 > val2 > val3
					// no changes needed
					return rating;
				} else {
					// val1 > val3 > val2
					return new byte[] {
							// val1
							rating[0], rating[1], rating[2],
							// val3
							rating[6], rating[7], rating[8],
							// val2
							rating[3], rating[4], rating[5] };

				}
			} else {
				// val3 > val1 > val2
				return new byte[] {
						// val3
						rating[6], rating[7], rating[8],
						// val1
						rating[0], rating[1], rating[2],
						// val2
						rating[3], rating[4], rating[5] };
			}
		} else {
			// val2 > val1
			if (val2 > val3) {
				// (val2 > val1) && (val2 > val3)
				if (val1 > val3) {
					// val2 > val1 > val3
					return new byte[] {
							// val2
							rating[3], rating[4], rating[5],
							// val1
							rating[0], rating[1], rating[2],
							// val3
							rating[6], rating[7], rating[8] };
				} else {
					// val2 > val3 > val1
					return new byte[] {
							// val2
							rating[3], rating[4], rating[5],
							// val3
							rating[6], rating[7], rating[8],
							// val1
							rating[0], rating[1], rating[2] };
				}
			} else {
				// val3 > val2 > val1
				return new byte[] {
						// val3
						rating[6], rating[7], rating[8],
						// val2
						rating[3], rating[4], rating[5],
						// val1
						rating[0], rating[1], rating[2] };
			}
		}
	}
}
