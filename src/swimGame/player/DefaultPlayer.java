package swimGame.player;

import swimGame.cards.CardStack;
import swimGame.cards.CardStack.StackIterator;
import swimGame.out.Debug;
import swimGame.table.Table;

/**
 * A player playing the game. The default implementation of a player.
 * 
 * @author Jens Bertram <code@jens-bertram.net>
 * 
 */
public class DefaultPlayer extends AbstractPlayer {
	// Cards owned by the table
	protected CardStack cardStackTable;
	// Cards we want to get
	protected CardStack cardStackNeed;
	// Cards on the table to decide on (if it's our turn)
	private byte[] cardsTableTriple = new byte[] {
			CardStack.FLAG_UNINITIALIZED, CardStack.FLAG_UNINITIALIZED,
			CardStack.FLAG_UNINITIALIZED };

	// rating influencing parameters
	private static final int INFLUENCE_SAME_COLOR = 5; // the higher the more
														// risky
	private static final int INFLUENCE_SAME_TYPE = 10;

	// behavior defaults
	private static final int STACk_DROP_TRESHOLD = 20;

	// the maximum value a card rating by type may reach
	private static final int CARDRATING_MAX_BY_TYPE = (2 * DefaultPlayer.INFLUENCE_SAME_TYPE)
			+ DefaultPlayer.getNormalizedCardValue(CardStack.CARDS_MAX)
			+ DefaultPlayer.getNormalizedCardValue(CardStack.CARDS_MAX - 1)
			+ DefaultPlayer.getNormalizedCardValue(CardStack.CARDS_MAX - 2);

	// the maximum value a card rating by color may reach
	private static final int CARDRATING_MAX_BY_COLOR = (2 * DefaultPlayer.INFLUENCE_SAME_COLOR)
			+ DefaultPlayer.getNormalizedCardValue(CardStack.CARDS_MAX)
			+ DefaultPlayer.getNormalizedCardValue(CardStack.CARDS_MAX - 1)
			+ DefaultPlayer.getNormalizedCardValue(CardStack.CARDS_MAX - 2);

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
		Debug.println(this, "Deciding on my current card-set: "
				+ this.cardStack);

		if (this.calcStack_value() < DefaultPlayer.STACk_DROP_TRESHOLD) {
			// drop immediately if blow threshold
			Debug.println(this, String.format(
					"Dropping (below threshold (%d))",
					DefaultPlayer.STACk_DROP_TRESHOLD));
			return false;
		}

		Debug.println(this, "Ok, I'm keeping this cardset");
		return true;
	}

	/**
	 * Rate our current cards to decide witch we'll probably drop. Results go
	 * into the needed cards array (as negative values) to save some space.
	 */
	private void rateCards() {
		StackIterator sI;

		Debug.println(this, "Rating my current cards..");
		sI = this.cardStack.new StackIterator();
		while (sI.hasNext()) {
			// skip if we don't own this card
			if (sI.next() == CardStack.FLAG_NO_CARD) {
				continue;
			}
			int card = sI.getCard();

			int ratingColor = this.rateCard_byColor(card);
			int ratingType = this.rateCard_byType(card);
			int ratingValue = this.rateCard_byValue(new int[] {
					sI.getCardType(), sI.getCardColor() });
			int overallRating = ratingColor + ratingType + ratingValue;
			this.cardStackNeed.setCardValue(sI.getCard(),
					(byte) (overallRating * -1));

			Debug.print(this, true, String.format(
					" %s: color(%d) type(%d) value(%d) = %d",
					CardStack.cardToString(sI.getCard()), ratingColor,
					ratingType, ratingValue, overallRating));
			Debug.print("\n");
		}
	}

	protected int normalizeRating(double max, double value) {
		return new Double(((value - 0) / (max - 0)) * 10).intValue();
	}

	/**
	 * Get the real value of a card
	 */
	private static int getCardValue(int card) {
		if (card < CardStack.CARDS_MAX_CARD) {
			return card;
		}
		return card
				- ((card / CardStack.CARDS_MAX_CARD) * CardStack.CARDS_MAX_CARD);
	}

	/**
	 * Get a normalized value of a card
	 */
	private static int getNormalizedCardValue(int card) {
		int value = DefaultPlayer.getCardValue(card);
		if (value > 2) {
			// B, D, K -> 3; A -> 4
			return value < 7 ? 3 : 4;
		} else {
			// 7 -> 0; 8 -> 1; 9 -> 2 (basically their array locations)
			return value;
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
				value = value + DefaultPlayer.getNormalizedCardValue(card) + 7;
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
				value = value + DefaultPlayer.getNormalizedCardValue(card) + 7;
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
	 * General function to rate a card based on it's color or value.
	 */
	private int rateCard_ColorOrValue(int card, byte[] cardsArray,
			int influence, int maxRating) {
		int cardValue = 0;
		int same = 0;

		for (byte stackCard : cardsArray) {
			if (this.cardStack.hasCard(stackCard) == true) {
				same++;
				cardValue = cardValue
						+ DefaultPlayer.getNormalizedCardValue(card);

				// modify rating if there are more cards
				cardValue = (same > 1) ? (cardValue + influence) : cardValue;
			}
		}
		return this.normalizeRating(maxRating, cardValue);
	}

	/**
	 * Rate a card based on other cards of the same color
	 */
	private int rateCard_byColor(int card) {
		return this.rateCard_ColorOrValue(card, this.cardStack
				.getCardsByColor(this.cardStack.getCardColor(card)),
				DefaultPlayer.INFLUENCE_SAME_COLOR,
				DefaultPlayer.CARDRATING_MAX_BY_COLOR);
	}

	/**
	 * Rate a card based on it's type and cards of the same type
	 */
	private int rateCard_byType(int card) {
		return this
				.rateCard_ColorOrValue(card, this.cardStack
						.getCardsByType(this.cardStack.getCardType(card)),
						DefaultPlayer.INFLUENCE_SAME_TYPE,
						DefaultPlayer.CARDRATING_MAX_BY_TYPE);
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
			if (this.cardStack.getArray()[aCard[0]
					+ (CardStack.CARDS_MAX_CARD * color)] == CardStack.FLAG_NO_CARD) {
				// ..no we don't - check next
				continue;
			}
			same[0] = same[0]++;
			cardValue = cardValue
					+ DefaultPlayer.getNormalizedCardValue(aCard[0]);

			// modify rating if there are more cards of this type
			if (same[0] > 1) {
				cardValue = cardValue + DefaultPlayer.INFLUENCE_SAME_TYPE;
			}
		}

		// go through cards by same color
		for (int card = 0; card < CardStack.CARDS_MAX_CARD; card++) {
			// ..to check if we own it..
			if (this.cardStack.getArray()[card
					+ (CardStack.CARDS_MAX_CARD * aCard[1])] == CardStack.FLAG_NO_CARD) {
				// ..no we don't - check next
				continue;
			}
			same[1] = same[1]++;
			cardValue = cardValue + DefaultPlayer.getNormalizedCardValue(card);

			// modify rating if there are more cards of this color
			if (same[1] > 1) {
				cardValue = cardValue + DefaultPlayer.INFLUENCE_SAME_COLOR;
			}
		}

		return this.normalizeRating(DefaultPlayer.CARDRATING_MAX_BY_TYPE,
				cardValue);
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
			for (byte b : this.cardStack.getCardsByColor(sI.getCardColor())) {
				if (b == CardStack.FLAG_HAS_CARD) {
					nearBy++;
				}
			}
			// do we have another card in this col?
			for (byte b : this.cardStack.getCardsByType(sI.getCardType())) {
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
			for (byte b : this.cardStack.getCardsByType(sI.getCardType())) {
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
			for (byte card : this.cardStack.getCardsByColor(sI.getCardColor())) {
				if (this.cardStack.hasCard(card)) {
					// calculate the real value here
					currentValue = currentValue
							+ DefaultPlayer.getCardValue(card) + 7;
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
	 * Sort a card triple by values in the given stack
	 * 
	 * @param stack
	 *            The stack to get the card values from
	 * @param triple
	 *            The triple to sort
	 * @return The sorted triple
	 */
	private byte[] sortCardTriple(CardStack stack, byte[] triple) {
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
	public boolean doMove(CardStack table) {
		int index;
		this.cardStackTable = table;
		Debug.println(this, "My stack: " + this.cardStack.toString());

		// regenerate priority list of needed cards
		this.recalcNeededCards();

		// get cards on table
		index = 0;
		for (byte card : this.cardStackTable.getCards()) {
			this.cardsTableTriple[index++] = card;
		}

		// rate table cards
		this.cardsTableTriple = this.sortCardTriple(this.cardStackNeed,
				this.cardsTableTriple);
		Debug.print(this, "My table cards rating: ");
		for (byte card : this.cardsTableTriple) {
			Debug.print(String.format("%s:%d ", CardStack.cardToString(card),
					this.cardStackNeed.getCardValue(card)));
		}
		Debug.print("\n");

		// rate own cards
		this.rateCards();

		// dump rating stack
		if (Debug.debug == true) {
			Debug.print(this, "My need stack:\n"
					+ this.cardStackNeed.dumpStack().toString() + "\n");
		}

		// estimate goal distance
		if (Debug.debug == true) {
			int goalDistance = this.calcStack_goalDistance();
			Debug.println(
					this,
					String.format("Goal distance: %d card"
							+ (goalDistance > 1 ? "s" : ""), goalDistance));
		}

		// make a drop suggestion
		byte[] cardToDrop = new byte[] { CardStack.FLAG_UNINITIALIZED,
				CardStack.FLAG_UNINITIALIZED };
		for (byte card : this.cardStack.getCards()) {
			// be careful, card ratings are still noted as negative values here
			byte cardValue = this.cardStackNeed.getCardValue(card);
			if ((cardValue == 0) || (cardToDrop[1] == -1)
					|| (cardValue > cardToDrop[1])) {
				cardToDrop[0] = card;
				cardToDrop[1] = cardValue;
			}
		}
		Debug.println(this,
				"Thought: I'll drop " + CardStack.cardToString(cardToDrop[0]));

		return true;
	}

	/**
	 * Shortcut to assign a new rating to a card, only if it's higher than the
	 * existing rating
	 * 
	 * @param stack
	 *            The stack to search in
	 * @param card
	 *            The card to work with
	 * @param newValue
	 *            The new value we try to assign
	 */
	private void uprateCard(CardStack stack, int card, int newValue) {
		int oldValue = stack.getCardValue(card);
		stack.setCardValue(card, (byte) (oldValue > newValue ? oldValue
				: newValue));
	}

	/**
	 * Create a rating array for all currently not-owned cards. This may be
	 * helpful to decide, witch card to pick from the table.
	 */
	private void recalcNeededCards() {
		this.cardStackNeed.fill(CardStack.FLAG_UNINITIALIZED);
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
				if (this.cardStack.hasCard(cardToCheck)) {
					matches++;
				}
			}
			// skip further rating, if no card found in current color
			if (matches > 0) {
				// color: rate the missing ones based on the number of matches
				for (int cardNum = 0; cardNum < CardStack.CARDS_MAX_CARD; cardNum++) {
					int cardToCheck = cardOffset + cardNum;
					if (this.cardStack.hasCard(cardToCheck) == false) {
						int ratingValue = DefaultPlayer
								.getCardValue(cardToCheck)
								+ (matches * DefaultPlayer.INFLUENCE_SAME_COLOR);
						this.uprateCard(this.cardStackNeed, cardToCheck,
								ratingValue);
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
					// if we found a card check remaining colors for this card
					// for matches
					if (this.cardStack.hasCard(ownedCard)) {
						typeChecked[DefaultPlayer.getCardValue(cardNum)] = 1;
						for (int i = cardsColor; i < CardStack.CARDS_MAX_COLOR; i++) {
							int cardToCheck = cardNum + (i * cardOffset);
							if ((cardToCheck != ownedCard)
									&& this.cardStack.hasCard(cardToCheck)) {
								int ratingValue = this.cardStackNeed
										.getCardValue(cardToCheck)
										+ DefaultPlayer.INFLUENCE_SAME_TYPE
										+ (int) (Table.PNT_THREE_OF_SAME_TYPE / 3);

								for (int fixedTypeCard = 0; fixedTypeCard < CardStack.CARDS_MAX_COLOR; fixedTypeCard++) {
									int cardToRate = cardNum
											+ (fixedTypeCard * cardOffset);
									if (this.cardStack.hasCard(cardToRate) == false) {
										this.uprateCard(this.cardStackNeed,
												cardToRate, ratingValue);
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
	public void handleTableEvent(Table.Event event, Object data) {
		switch (event) {
		case INITIAL_CARDS_DROPPED:
			this.cardStackTable.addCards((byte[]) data);
			break;
		}
	}
}
