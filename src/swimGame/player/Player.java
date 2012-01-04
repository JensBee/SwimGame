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

		/**
		 * Constructor
		 */
		protected Cards() {
		}

		protected void setCardStack(CardStack cs) {
			this.cs = cs;
		}

		// public int rateStack(CardStack cardStack) {
		// return this.rateStackByArray(cardStack.getArray());
		// }
		//
		// private int rateStackByArray(byte[][] cardStack) {
		// this.rateMaxStackValue(cardStack);
		// return 0;
		// }

		/**
		 * Calculates the value of a card belonging to a stack
		 * 
		 * @param card
		 *            The number representing the card in the stack
		 * @return The card value
		 */
		public int getStackCardValue(int card) {
			// normalize card values
			if (card > 2) {
				// B, D, K -> 3; A -> 4
				return card < 7 ? 3 : 4;
			} else {
				// 7 -> 0; 8 -> 1; 9 -> 2 (basically their array locations)
				return card;
			}
		}

		// private int rateMaxStackValue(byte[][] cardStack) {
		// // discount by 9 for each card (9 is avg card value)
		// // missing a color-card counts two times the discount
		// int foundCards[] = new int[] { -1, -1, -1 };
		// int cardIndex = 0;
		//
		// // iterate over every color
		// for (int color = 0; color < (CardStack.CARDS_MAX_COLOR); color++) {
		// // iterate over every card
		// for (int card = 0; card < (CardStack.CARDS_MAX_CARD); card++) {
		// if (cardStack[card][color] == 1) {
		// foundCards[cardIndex++] = card;
		// }
		// }
		// }
		// return 0;
		// }

		/**
		 * 
		 * @param card
		 *            int[card][color]
		 * @return
		 */
		protected int rateCard(int[] aCard) {
			int cardValue = 0;
			byte same[] = new byte[] { 0, 0 }; // [card, color]

			// go through cards by same color
			for (int card = 0; card < CardStack.CARDS_MAX_CARD; card++) {
				// ..to check if we own it..
				if (this.cs.getArray()[card][aCard[1]] == 0) {
					// ..no we don't - check next
					continue;
				}
				same[1] = same[1]++;
				cardValue = cardValue + this.getStackCardValue(card);
			}

			// go through cards by same type
			for (int color = 0; color < (CardStack.CARDS_MAX_COLOR); color++) {
				// ..to check if we own it..
				if (this.cs.getArray()[aCard[0]][color] == 0) {
					// ..no we don't - check next
					continue;
				}
				same[0] = same[0]++;
				cardValue = cardValue + this.getStackCardValue(aCard[0]);
			}
			return cardValue;
		}
	}

	/**
	 * Decide (if able to), if our initial card set is good enough.
	 */
	@Override
	public boolean keepCardSet() {
		// TODO: base decision also on card colors
		byte[] cards = this.rateCards();
		Debug.println(
				this,
				String.format(
						"Looks I'll drop %s.",
						this.cardUtils.cardToString(new int[] { cards[6],
								cards[7] })));
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
		this.cards.setCardStack(this.cardStack);

		// dig through the card-stack by color-rows..
		for (int color = 0; color < (CardStack.CARDS_MAX_COLOR); color++) {
			// ..and iterate over every card
			for (int card = 0; card < CardStack.CARDS_MAX_CARD; card++) {
				if (cardStackArray[card][color] == 0) {
					// ..no we don't - check next
					continue;
				}
				// we do - so rate it
				int cardRating = this.cards.rateCard(new int[] { card, color });
				Debug.println(
						this,
						String.format(
								"* NEW Rating %s: %d",
								this.cardUtils.cardToString(new int[] { card,
										color }), cardRating));
				rating[ratingCount++] = (byte) card;
				rating[ratingCount++] = (byte) color;
				rating[ratingCount++] = (byte) cardRating;
			}
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
