package swimGame.player;

import swimGame.cards.CardStack;
import swimGame.cards.CardStack.StackIterator;
import swimGame.out.Debug;
import swimGame.table.Table;

/**
 * A player playing the game. The default implementation of a player.
 * 
 * Basic naming scheme for rating functions:<br/>
 * All rate(Card|Stack)_* functions return normalized values between 0-10 from
 * worst to best.<br/>
 * All calc(Card|Stack)_* function return absolute values.
 * 
 * @author Jens Bertram <code@jens-bertram.net>
 * 
 */
public class DefaultPlayer extends AbstractPlayer {
	// /**
	// * The following variables (self.*) define the players character, Their
	// * values values range between low:1 and high:9
	// */
	// private final byte selfRiskyness = 9;

	/** Cards owned by the table */
	protected CardStack cardStackTable;
	/** Cards we want to get */
	protected CardStack cardStackNeed;

	// additional flags for the stack-array
	// private static final byte FLAG_CARD_TAKEN = -2;
	// private static final byte FLAG_CARD_DROPPED = -3;

	// rating influencing parameters
	private static final int INF_SAME_COLOR = 5;
	private static final int INF_SAME_TYPE = 0;

	// behaviour defaults
	private static final int STACk_DROP_TRESHOLD = 20;

	// the maximum value a card rating by type may reach
	private static final int CR_MAX_TYPE = (2 * DefaultPlayer.INF_SAME_TYPE)
			+ DefaultPlayer.getStackCardValue(CardStack.CARDS_MAX)
			+ DefaultPlayer.getStackCardValue(CardStack.CARDS_MAX - 1)
			+ DefaultPlayer.getStackCardValue(CardStack.CARDS_MAX - 2);

	// the maximum value a card rating by color may reach
	private static final int CR_MAX_COLOR = (2 * DefaultPlayer.INF_SAME_COLOR)
			+ DefaultPlayer.getStackCardValue(CardStack.CARDS_MAX)
			+ DefaultPlayer.getStackCardValue(CardStack.CARDS_MAX - 1)
			+ DefaultPlayer.getStackCardValue(CardStack.CARDS_MAX - 2);

	/** Constructor */
	public DefaultPlayer() {
		super();
		this.cardStackTable = new CardStack();
		this.cardStackNeed = new CardStack();
	}

	/**
	 * Decide (if able to), if our initial card set is good enough.
	 */
	@Override
	public boolean keepCardSet() {
		// byte[] cards = this.rateCards();

		Debug.println(this, "Deciding..");

		if (this.calcStack_value() < DefaultPlayer.STACk_DROP_TRESHOLD) {
			// drop immediately if blow threshold
			Debug.println(this, String.format(
					"Dropping (below threshold (%d))",
					DefaultPlayer.STACk_DROP_TRESHOLD));
			return false;
		}

		Debug.println(this, "Keeping cardset");
		return true;
	}

	/**
	 * Rate the cards in our stack to choose one we will probably drop
	 * 
	 * @return
	 */
	private byte[] rateCards() {
		// get a copy of out card-stack
		// byte[][] cardStackArray = this.cardStack.getArray();
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
					CardStack.cardToString(new int[] { sI.getCard(),
							sI.getColor() }),
					this.rateCard_byColor(new int[] { sI.getCard(),
							sI.getColor() }),
					this.rateCard_byType(new int[] { sI.getCard(),
							sI.getColor() }),
					this.rateCard_byValue(new int[] { sI.getCard(),
							sI.getColor() })));
			Debug.print("\n");
		}

		if (Debug.debug == true) {
			Debug.println(
					this,
					String.format("Goal distance: %d",
							this.calcStack_goalDistance()));

		}
		return this.rankCardRating(rating);
	}

	protected int normalizeRating(double max, double value) {
		return new Double(((value - 0) / (max - 0)) * 10).intValue();
	}

	/**
	 * Calculates the value of a card belonging to a stack. To get the real
	 * values add 7 to the retrieved value
	 * 
	 * @param card
	 *            The number representing the card in the stack
	 * @return The card value
	 */
	private static int getStackCardValue(int card) {
		// normalize card values
		if (card > 2) {
			// B, D, K -> 3; A -> 4
			return card < 7 ? 3 : 4;
		} else {
			// 7 -> 0; 8 -> 1; 9 -> 2 (basically their array locations)
			return card;
		}
	}

	private double getStackValue() throws IllegalStateException {
		int count = 0;
		double value = 0;
		for (byte color = 0; color < (CardStack.CARDS_MAX_COLOR); color++) {
			byte[] cardsByColor = this.cardStack.getCardsByColor(color);
			for (byte card : cardsByColor) {
				if (card == CardStack.FLAG_UNINITIALIZED) {
					continue;
				}
				count++;
				value = value + DefaultPlayer.getStackCardValue(card) + 7;
			}
			if (count > 0) {
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
				value = value + DefaultPlayer.getStackCardValue(card) + 7;
			}
			if (count > 0) {
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
	 * @return The normalized rating value
	 */
	private int rateCard_byColor(int[] aCard) {
		// the rating for this card
		int cardValue = 0;
		// store occourances [card, color]
		int same = 0;

		// go through cards by same color
		for (int card = 0; card < CardStack.CARDS_MAX_CARD; card++) {
			// ..to check if we own it..
			if (this.cardStack.getArray()[card][aCard[1]] == CardStack.FLAG_NO_CARD) {
				// ..no we don't - check next
				continue;
			}
			same++;
			cardValue = cardValue + DefaultPlayer.getStackCardValue(card);

			// modify rating if there are more cards of this color
			if (same > 1) {
				cardValue = cardValue + DefaultPlayer.INF_SAME_COLOR;
			}
		}

		return this.normalizeRating(DefaultPlayer.CR_MAX_COLOR, cardValue);
	}

	/**
	 * Rate a card based on it's type and cards of the same type
	 * 
	 * @param aCard
	 * @return The normalized rating value
	 */
	private int rateCard_byType(int[] aCard) {
		// the rating for this card
		int cardValue = 0;
		// store occourances [card, color]
		int same = 0;

		// go through cards by same type
		for (int color = 0; color < (CardStack.CARDS_MAX_COLOR); color++) {
			// ..to check if we own it..
			if (this.cardStack.getArray()[aCard[0]][color] == CardStack.FLAG_NO_CARD) {
				// ..no we don't - check next
				continue;
			}
			same++;
			cardValue = cardValue + DefaultPlayer.getStackCardValue(aCard[0]);

			// modify rating if there are more cards of this type
			if (same > 1) {
				cardValue = cardValue + DefaultPlayer.INF_SAME_TYPE;
			}
		}

		return this.normalizeRating(DefaultPlayer.CR_MAX_TYPE, cardValue);
	}

	/**
	 * Rate a card based on it's card value
	 * 
	 * @param card
	 *            int[card][color]
	 * @return The normalized rating value
	 */
	private int rateCard_byValue(int[] aCard) {
		// the rating for this card
		int cardValue = 0;
		// store occourances [card, color]
		byte same[] = new byte[] { 0, 0 };

		// go through cards by same type
		for (int color = 0; color < (CardStack.CARDS_MAX_COLOR); color++) {
			// ..to check if we own it..
			if (this.cardStack.getArray()[aCard[0]][color] == CardStack.FLAG_NO_CARD) {
				// ..no we don't - check next
				continue;
			}
			same[0] = same[0]++;
			cardValue = cardValue + DefaultPlayer.getStackCardValue(aCard[0]);

			// modify rating if there are more cards of this type
			if (same[0] > 1) {
				cardValue = cardValue + DefaultPlayer.INF_SAME_TYPE;
			}
		}

		// go through cards by same color
		for (int card = 0; card < CardStack.CARDS_MAX_CARD; card++) {
			// ..to check if we own it..
			if (this.cardStack.getArray()[card][aCard[1]] == CardStack.FLAG_NO_CARD) {
				// ..no we don't - check next
				continue;
			}
			same[1] = same[1]++;
			cardValue = cardValue + DefaultPlayer.getStackCardValue(card);

			// modify rating if there are more cards of this color
			if (same[1] > 1) {
				cardValue = cardValue + DefaultPlayer.INF_SAME_COLOR;
			}
		}

		return this.normalizeRating(DefaultPlayer.CR_MAX_TYPE, cardValue);
	}

	/**
	 * Check, how many cards are missing to reach a goal state. No calculation
	 * of values is involved here
	 * 
	 * @return How many cards are missing to reach a goal state
	 */
	private int calcStack_goalDistance() {
		int pair = 0;
		StackIterator sI = this.cardStack.new StackIterator();
		while (sI.hasNext()) {
			int nearBy = 0;
			// skip if we don't own this card
			if (sI.next() == CardStack.FLAG_NO_CARD) {
				continue;
			}
			// do we have another card in this row?
			for (byte b : this.cardStack.getRow(sI.getColor())) {
				if (b == CardStack.FLAG_HAS_CARD) {
					nearBy++;
				}
			}
			// do we have another card in this col?
			for (byte b : this.cardStack.getColumn(sI.getCard())) {
				if (b == CardStack.FLAG_HAS_CARD) {
					nearBy++;
				}
			}
			pair = (nearBy > pair) ? nearBy : pair;
		}
		return (6 - pair) / 2; // 6 -> 3 cards = 3*color + 3*type
	}

	/**
	 * Returns the current value of the stack
	 * 
	 * @return The absolute value
	 */
	private double calcStack_value() {
		StackIterator sI = this.cardStack.new StackIterator();
		double value = 0;
		while (sI.hasNext()) {
			int sameType = 0;
			double currentValue = 0;
			// skip if we don't own this card
			if (sI.next() == CardStack.FLAG_NO_CARD) {
				continue;
			}

			// do we have the same card with another color?
			for (byte b : this.cardStack.getColumn(sI.getCard())) {
				if (b == CardStack.FLAG_HAS_CARD) {
					sameType++;
				}
			}

			// same type in three colors is fixed value
			if (sameType == 3) {
				value = Table.PNT_THREE_OF_SAME_TYPE;
				break;
			} else if (sameType == 2) {
				// we only missing one card of our type
				// TODO: better decide on players "model" if he want's to take
				// this route
				Debug.println("--> shortening decision [FIXME!]");
				value = (Table.PNT_THREE_OF_SAME_TYPE / 3) * 2;
				break;
			}

			// do we have another card in this row/of the same type?
			byte[] row = this.cardStack.getRow(sI.getColor());
			for (int i = 0; i < CardStack.CARDS_MAX_CARD; i++) {
				byte b = row[i];
				if (b == CardStack.FLAG_HAS_CARD) {
					// calculate the real value here
					currentValue = currentValue
							+ DefaultPlayer.getStackCardValue(i) + 7;
				}
			}
			value = (currentValue > value) ? currentValue : value;
		}

		if (Debug.debug == true) {
			double dMin = CardStack.STACKVALUE_MIN - value;
			double dMax = CardStack.STACKVALUE_MAX - value;
			Debug.println(
					this,
					String.format(
							"Stack value: abs(%s) dMax(+%.1f) dMin(-%.1f) dMinMax(%.1f)",
							Double.toString(value), dMax, dMin, dMax - dMin));
		}
		return value;
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

	@Override
	public boolean doMove() {
		Debug.println(this, "My stack: " + this.cardStack.toString());
		Debug.println(this, "Table stack: " + this.cardStackTable.toString());
		Debug.println(this, "Table stack: " + this.cardStackTable.toString());
		this.recalcNeededCards();
		this.rateCards();
		return true;
	}

	private void recalcNeededCards() {
		StackIterator sI = this.cardStack.new StackIterator();

		this.cardStackNeed.clear();
		while (sI.hasNext()) {
			// skip if we don't own this card or row has been already checked
			if ((sI.next() == CardStack.FLAG_NO_CARD)) {
				continue;
			}

			int rowValue;
			byte[] row;

			// scan this row to collect all matches
			rowValue = 0;
			row = this.cardStack.getRow(sI.getColor());
			for (int i = 0; i < CardStack.CARDS_MAX_CARD; i++) {
				byte b = row[i];
				if (b == CardStack.FLAG_HAS_CARD) {
					rowValue = rowValue + DefaultPlayer.getStackCardValue(i);
				}
			}
			// so we know the base value, lets calculate missing card values and
			// add those to the cardStackNeed
			row = this.cardStack.getRow(sI.getColor());
			for (int i = 0; i < CardStack.CARDS_MAX_CARD; i++) {
				byte b = row[i];
				if (b == CardStack.FLAG_NO_CARD) {
					this.cardStackNeed.setCardValue(
							new int[] { i, sI.getColor() }, (byte) (rowValue
									+ DefaultPlayer.getStackCardValue(i) + 7));
				}
			}

			if (sI.hasNextRow()) {
				sI.nextRow();
			} else {
				break;
			}
		}

		if (Debug.debug == true) {
			Debug.print(this, "My need stack:\n"
					+ this.cardStackNeed.dumpStack().toString() + "\n");
		}
	}

	@Override
	public void handleTableEvent(Table.Event event, Object data) {
		switch (event) {
		case INITIAL_CARDS_DROPPED:
			this.cardStackTable.addCards((int[]) data);
			break;
		}
	}
}
