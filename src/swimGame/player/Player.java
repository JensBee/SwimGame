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

	/** Constructor */
	public Player() {
		super();
	}

	/**
	 * Decide (if able to), if our initial card set is good enough.
	 */
	@Override
	public boolean keepCardSet() {
		// TODO: base decision also on card colors
		this.rateCards();
		return true;
	}

	/**
	 * Rate the cards in our stack to choose one we will probably drop
	 * 
	 * @return
	 */
	private byte[] rateCards() {
		// get a copy of out card-stack
		byte[][] cardStack = this.cardStack.getArray();
		// store three cards (x,y) with one rating (z) each
		final byte[] rating = new byte[9];
		// rating array index pointer
		int ratingCount = 0;

		Debug.println(this, "Rating my current cards..");

		// dig through the card-stack by color-rows..
		for (int color = 0; color < (CardStack.CARDS_MAX_COLOR); color++) {
			// track, how many cards of one color we own
			int sameColor = 0;
			// store the last card we've seen
			byte[] lastCard = new byte[] { 0, 0 };

			// ..and iterate over every card..
			for (int card = 0; card < (CardStack.CARDS_MAX_CARD); card++) {
				// the value witch represents the rating for this card
				int cardValue = 0;
				// is our card already rated?
				boolean rated = false;

				// ..to check if we own it..
				if (cardStack[card][color] == 0) {
					// ..no we don't
					continue;
				}

				// We found one, so check surroundings. Going back makes no
				// sense. Going forward is already handled by the wrapping
				// loop, so check downwards (if possible).
				int sameType = 0;
				sameColor++;

				// normalize card values
				if (card > 2) {
					// B, D, K -> 3; A -> 4
					cardValue = (byte) (card < 7 ? 3 : 4);
				} else {
					// 7 -> 0; 8 -> 1; 9 -> 2 (basically their array places)
					cardValue = (card);
				}

				// see if we already own a card of the current color
				if (sameColor > 1) {
					// we do, so rate up the previous and the current card
					if (sameColor == 2) {
						// upgrade last seen card of current color
						ratingCount = ratingCount - 3;
						rating[ratingCount++] = lastCard[0];
						rating[ratingCount++] = (byte) color;
						rating[ratingCount++] = (byte) (lastCard[1] + (11 - this.selfRiskyness));

						Debug.println(
								this,
								"Re-rating "
										+ this.cardUtils
												.cardToString(new int[] {
														lastCard[0], color })
										+ " (color): "
										+ rating[ratingCount - 1]);
					}
					cardValue = (byte) (cardValue + (15 - this.selfRiskyness));
				}

				for (int colDig = color + 1; colDig < (CardStack.CARDS_MAX_COLOR); colDig++) {
					if (this.cardStack.getArray()[card][colDig] == 1) {
						// current row
						rating[ratingCount++] = (byte) card;
						rating[ratingCount++] = (byte) color;
						rating[ratingCount++] = (byte) (cardValue + (11 - this.selfRiskyness));
						Debug.println(
								this,
								"Rating "
										+ this.cardUtils
												.cardToString(new int[] { card,
														color })
										+ " (similarity): "
										+ rating[ratingCount - 1]);

						// one row down
						rating[ratingCount++] = (byte) card;
						rating[ratingCount++] = (byte) colDig;
						rating[ratingCount++] = (byte) (cardValue + (10 - this.selfRiskyness));
						Debug.println(
								this,
								"Rating "
										+ this.cardUtils
												.cardToString(new int[] { card,
														colDig })
										+ " (similarity): "
										+ rating[ratingCount - 1]);
						// remove this card from out temporary stack so we don't
						// inspect it twice
						cardStack[card][colDig] = 0;
						sameType++;
						rated = true;
					}
				}

				// do some final testing for special / general case
				if (sameType == 2) {
					// 30.5 points all of a type
					Debug.println(this, "Yay, JACKPOT! (all cards same type)");
				} else if ((sameType == 0) && (rated == false)) {
					// nothing special, just a single unrated card
					// rating[card][color] = cardValue;
					rating[ratingCount++] = (byte) card;
					rating[ratingCount++] = (byte) color;
					rating[ratingCount++] = (byte) cardValue;
					Debug.println(
							this,
							"Rating "
									+ this.cardUtils.cardToString(new int[] {
											card, color }) + ": "
									+ rating[ratingCount - 1]);
				}

				if (sameColor > 2) {
					Debug.println(this, "Yay, COLORPOT! (all cards same color)");
				}

				lastCard = new byte[] { (byte) card, (byte) cardValue };
			}
		}

		Debug.println(this, "Looks I'll drop " + rating.length + ".");
		return rating;
	}
}
