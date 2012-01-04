package swimGame.player;

import swimGame.cards.CardStack;
import swimGame.cards.CardUtils;
import swimGame.out.Debug;

/**
 * A player playing the game. The default implementation of a player.
 * 
 * @author Jens Bertram <code@jens-bertram.net>
 * 
 */
public class Player extends AbstractPlayer {
	private final CardUtils cardUtils = new CardUtils();
	/**
	 * The following variables (self.*) define the players character, Their
	 * values values range between low:1 and high:9
	 */
	private final byte selfRiskyness = 9;

	private final Cards cards = new Cards();

	/** Constructor */
	public Player() {
		super();
	}

	/**
	 * Card Rating
	 * 
	 * Each rate.* function returns a value between 0 (bad) and 10 (best).
	 * 
	 * @author Jens Bertram <code@jens-bertram.net>
	 * 
	 */
	private class Cards {
		CardStack cs;

		// rating influencing parameters
		protected final byte INF_SAME_COLOR = 10;
		protected final byte INF_SAME_TYPE = 0;

		// the maximum value a stack rating may reach
		protected final int cardStackMaxRating = (2 * this.INF_SAME_TYPE)
				+ this.getStackCardValue(CardStack.CARDS_MAX)
				+ this.getStackCardValue(CardStack.CARDS_MAX - 1)
				+ this.getStackCardValue(CardStack.CARDS_MAX - 2);

		/**
		 * Constructor
		 */
		protected Cards() {
		}

		protected void setCardStack(CardStack cs) {
			this.cs = cs;
		}

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
				byte[] cardsByColor = this.cs.getCardsByColor(color);
				for (byte card : cardsByColor) {
					if (card == -1) {
						continue;
					}
					count++;
					value = value + this.getStackCardValue(card) + 7;
				}
				if (count > 0) {
					Debug.println(this.getClass(), "Color:" + color + " cards:"
							+ count + " value:" + value);
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
				byte[] cardsByType = this.cs.getCardsByType(cardType);
				for (byte card : cardsByType) {
					if (card == -1) {
						continue;
					}
					count++;
					value = value + this.getStackCardValue(card) + 7;
				}
				if (count > 0) {
					Debug.println(this.getClass(), "Card:" + cardType
							+ " cards:" + count + " value:" + value);
					if (count == 3) {
						// return the max, if we reached it
						return 30.5;
					}
				}
				count = 0;
				value = 0;
			}
			Debug.println(this.getClass(), "Stack isn't dropable!");
			throw new IllegalStateException(
					"Your cardstack isn't ready to drop yet!");
		}

		/**
		 * 
		 * @param card
		 *            int[card][color]
		 * @return
		 */
		protected int rateCard(int[] aCard) {
			// the rating for this card
			int cardValue = 0;
			// store occourances [card, color]
			byte same[] = new byte[] { 0, 0 };

			// go through cards by same type
			Debug.print("rateCard-type: ");
			for (int color = 0; color < (CardStack.CARDS_MAX_COLOR); color++) {
				// ..to check if we own it..
				if (this.cs.getArray()[aCard[0]][color] == 0) {
					// ..no we don't - check next
					continue;
				}
				same[0] = same[0]++;
				cardValue = cardValue + this.getStackCardValue(aCard[0]);
				Debug.print(Integer.toString(cardValue));

				// modify rating if there are more cards of this type
				if (same[0] > 1) {
					cardValue = cardValue + this.INF_SAME_TYPE;
					Debug.print("->" + cardValue);
				}
				Debug.print("|");
			}
			Debug.print("\n");

			// go through cards by same color
			Debug.print("rateCard-color: ");
			for (int card = 0; card < CardStack.CARDS_MAX_CARD; card++) {
				// ..to check if we own it..
				if (this.cs.getArray()[card][aCard[1]] == 0) {
					// ..no we don't - check next
					continue;
				}
				same[1] = same[1]++;
				cardValue = cardValue + this.getStackCardValue(card);
				Debug.print(Integer.toString(cardValue));

				// modify rating if there are more cards of this color
				if (same[1] > 1) {
					cardValue = cardValue + this.INF_SAME_COLOR;
					Debug.print("->" + cardValue);
				}
				Debug.print("|");
			}
			Debug.print("\n--\n");

			return this.normalizeRating(this.cardStackMaxRating, cardValue);
		}
	}

	/**
	 * Decide (if able to), if our initial card set is good enough.
	 */
	@Override
	public boolean keepCardSet() {
		this.cards.setCardStack(this.cardStack);
		byte[] cards = this.rateCards();
		Debug.println(
				this,
				String.format(
						"Looks like I should drop %s.",
						this.cardUtils.cardToString(new int[] { cards[6],
								cards[7] })));
		try {
			Debug.println(
					this,
					String.format("Stack value is %.1f.",
							this.cards.getStackValue()));
		} catch (IllegalStateException e) {
			// ok, we're not ready yet
			Debug.println(this, String.format("Stack is not dropable yet."));
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

		// dig through the card-stack by color-rows..
		for (int color = 0; color < (CardStack.CARDS_MAX_COLOR); color++) {
			// ..and iterate over every card
			for (int card = 0; card < CardStack.CARDS_MAX_CARD; card++) {
				if (cardStackArray[card][color] == 0) {
					// ..no we don't - check next
					continue;
				}
				// we do - so rate it
				rating[ratingCount++] = (byte) color;
				rating[ratingCount++] = (byte) this.cards.rateCard(new int[] {
						card, color });
			}
		}

		// debugging output
		if (Debug.debug == true) {
			Debug.print(this, true, "Card ratings: ");
			for (int i = 0; i < rating.length; i++) {
				Debug.print(this.cardUtils.cardToString(new int[] { rating[i],
						rating[i + 1] })
						+ ":" + rating[i + 2] + " ");
				i = i + 2;
			}
			Debug.print("\n");
		}
		return this.rankCardRating(rating);
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
