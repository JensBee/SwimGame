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
	// Cards seen on the table
	protected CardStack cardStackTable;
	// Cards we want to get
	protected CardStack cardStackNeed;
	// Cards on the table to decide on (if it's our turn)
	private byte[] cardsTableTriple = new byte[] {
			CardStack.FLAG_UNINITIALIZED, CardStack.FLAG_UNINITIALIZED,
			CardStack.FLAG_UNINITIALIZED };

	// --- behavior defaults
	// below witch value should we drop an initial card stack immediately?
	private static final int STACk_DROP_TRESHOLD = 20;
	// initial rating for cards we've not seen on the table
	private static final int STACK_RATE_UNSEEN = -5;

	private final CardRating cardRating = new CardRating();
	private final StackRating stackRating = new StackRating();

	/** Constructor */
	public DefaultPlayer() {
		super();
		this.cardStackTable = new CardStack();
		this.cardStackNeed = new CardStack();
		this.cardStackTable.fill((byte) DefaultPlayer.STACK_RATE_UNSEEN);
	}

	/**
	 * General rating helper functions
	 * 
	 * @author Jens Bertram <code@jens-bertram.net>
	 * 
	 */
	private static class Rating {
		protected static int normalize(double max, double value) {
			return new Double(((value - 0) / (max - 0)) * 10).intValue();
		}
	}

	/**
	 * Card rating functions
	 * 
	 * @author Jens Bertram <code@jens-bertram.net>
	 * 
	 */
	private class CardRating {
		// rating influencing parameters
		private static final int INFLUENCE_SAME_COLOR = 5;
		private static final int INFLUENCE_SAME_TYPE = 10;
		// the maximum value a card rating by type may reach
		protected final int CARDRATING_MAX_BY_TYPE = (2 * CardRating.INFLUENCE_SAME_TYPE)
				+ this.getNormalizedCardValue(CardStack.CARDS_MAX)
				+ this.getNormalizedCardValue(CardStack.CARDS_MAX - 1)
				+ this.getNormalizedCardValue(CardStack.CARDS_MAX - 2);
		// the maximum value a card rating by color may reach
		private final int CARDRATING_MAX_BY_COLOR = (2 * CardRating.INFLUENCE_SAME_COLOR)
				+ this.getNormalizedCardValue(CardStack.CARDS_MAX)
				+ this.getNormalizedCardValue(CardStack.CARDS_MAX - 1)
				+ this.getNormalizedCardValue(CardStack.CARDS_MAX - 2);

		/** General function to rate a card based on it's color or value */
		private int rateColorOrType(int card, byte[] cardsArray, int influence) {
			int cardValue = 0;
			int same = 0;

			for (byte stackCard : cardsArray) {
				if (DefaultPlayer.this.cardStack.hasCard(stackCard) == true) {
					same++;
					cardValue = cardValue + this.getNormalizedCardValue(card);

					// modify rating if there are more cards
					cardValue = (same > 1) ? (cardValue + influence)
							: cardValue;
				}
			}
			return cardValue;
		}

		/** Rate a card based on other cards of the same color */
		protected int byColor(int card) {
			int cardValue = this.rateColorOrType(card,
					DefaultPlayer.this.cardStack
							.getCardsByColor(DefaultPlayer.this.cardStack
									.getCardColor(card)),
					CardRating.INFLUENCE_SAME_COLOR);
			return Rating.normalize(this.CARDRATING_MAX_BY_COLOR, cardValue);
		}

		/** Rate a card based on it's type and cards of the same type */
		protected int byType(int card) {
			int cardValue = this.rateColorOrType(card,
					DefaultPlayer.this.cardStack
							.getCardsByType(DefaultPlayer.this.cardStack
									.getCardType(card)),
					CardRating.INFLUENCE_SAME_TYPE);
			return Rating.normalize(this.CARDRATING_MAX_BY_TYPE, cardValue);
		}

		/** Rate a card based on it's card value */
		protected int byValue(int card) {
			return this.getCardValue(card);
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
		protected void uprate(CardStack stack, int card, int newValue) {
			int oldValue = stack.getCardValue(card);
			stack.setCardValue(card, (byte) (oldValue > newValue ? oldValue
					: newValue));
		}

		/** Get the real value of a card */
		protected int getCardValue(int card) {
			if (card < CardStack.CARDS_MAX_CARD) {
				return card;
			}
			return card
					- ((card / CardStack.CARDS_MAX_CARD) * CardStack.CARDS_MAX_CARD);
		}

		/** Get a normalized value of a card */
		protected int getNormalizedCardValue(int card) {
			int value = this.getCardValue(card);
			if (value > 2) {
				// B, D, K -> 3; A -> 4
				return value < 7 ? 3 : 4;
			} else {
				// 7 -> 0; 8 -> 1; 9 -> 2 (basically their array locations)
				return value;
			}
		}
	}

	/**
	 * Stack rating functions
	 * 
	 * @author Jens Bertram <code@jens-bertram.net>
	 * 
	 */
	private class StackRating {
		/**
		 * Check, how many cards are missing to reach a goal state. No
		 * calculation of values is involved here
		 * 
		 * @return How many cards are missing to reach a goal state
		 */
		protected byte[] getGoalDistance(byte[] cards) {
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
				int color = DefaultPlayer.this.cardStack.getCardColor(card);
				for (byte cardByColor : DefaultPlayer.this.cardStack
						.getCardsByColor(color)) {
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
				int type = DefaultPlayer.this.cardStack.getCardType(card);
				for (byte cardByType : DefaultPlayer.this.cardStack
						.getCardsByType(type)) {
					if (DefaultPlayer.this.cardStack.hasCard(cardByType)) {
						count++;
					}
				}
				// store max
				if (count > valueColor) {
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
				distances[3] = (byte) (valueType - 1);
			}

			return distances;
		}

		/** Returns the current value of the stack */
		protected double getStackValue() {
			StackIterator sI = DefaultPlayer.this.cardStack.new StackIterator();

			double value = 0;
			while (sI.hasNext()) {
				int sameType = 0;
				double currentValue = 0;
				// skip if we don't own this card
				if (sI.next() == CardStack.FLAG_NO_CARD) {
					continue;
				}

				// do we have the same card with another color?
				for (byte b : DefaultPlayer.this.cardStack.getCardsByType(sI
						.getCardType())) {
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
					// TODO: better decide on players "model" if he want's to
					// take
					// this route
					Debug.println("--> shortening decision [FIXME!]");
					value = (Table.PNT_THREE_OF_SAME_TYPE / 3) * 2;
					break;
				}

				// do we have another card in this row/of the same type?
				for (byte card : DefaultPlayer.this.cardStack
						.getCardsByColor(sI.getCardColor())) {
					if (DefaultPlayer.this.cardStack.hasCard(card)) {
						// calculate the real value here
						currentValue = currentValue
								+ DefaultPlayer.this.cardRating
										.getCardValue(card) + 7;
					}
				}

				value = (currentValue > value) ? currentValue : value;
			}

			if (Debug.debug == true) {
				double dMin = CardStack.STACKVALUE_MIN - value;
				double dMax = CardStack.STACKVALUE_MAX - value;
				Debug.println(
						DefaultPlayer.this,
						String.format(
								"Stack value: abs(%s) dMax(+%.1f) dMin(-%.1f) dMinMax(%.1f)",
								Double.toString(value), dMax, dMin, dMax - dMin));
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
						if (DefaultPlayer.this.cardStack.hasCard(cardToCheck) == false) {
							int ratingValue = DefaultPlayer.this.cardRating
									.getCardValue(cardToCheck)
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
									.getCardValue(cardNum)] = 1;
							for (int i = cardsColor; i < CardStack.CARDS_MAX_COLOR; i++) {
								int cardToCheck = cardNum + (i * cardOffset);
								if ((cardToCheck != ownedCard)
										&& DefaultPlayer.this.cardStack
												.hasCard(cardToCheck)) {
									int ratingValue = DefaultPlayer.this.cardStackNeed
											.getCardValue(cardToCheck)
											+ CardRating.INFLUENCE_SAME_TYPE
											+ (int) (Table.PNT_THREE_OF_SAME_TYPE / 3);

									for (int fixedTypeCard = 0; fixedTypeCard < CardStack.CARDS_MAX_COLOR; fixedTypeCard++) {
										int cardToRate = cardNum
												+ (fixedTypeCard * cardOffset);
										if (DefaultPlayer.this.cardStack
												.hasCard(cardToRate) == false) {
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

	/**
	 * Decide (if able to), if our initial card set is good enough.
	 */
	@Override
	public boolean keepCardSet() {
		Debug.println(this, "Deciding on my current card-set: "
				+ this.cardStack);

		if (this.stackRating.getStackValue() < DefaultPlayer.STACk_DROP_TRESHOLD) {
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
		Debug.println(this, "Rating my current cards..");
		for (byte card : this.cardStack.getCards()) {
			int ratingColor = this.cardRating.byColor(card);
			int ratingType = this.cardRating.byType(card);
			int ratingValue = this.cardRating.byValue(card);
			int overallRating = ratingColor + ratingType + ratingValue;
			this.cardStackNeed.setCardValue(card, (byte) (overallRating * -1));

			Debug.print(this, true, String.format(
					" %s: color(%d) type(%d) value(%d) = %d",
					CardStack.cardToString(card), ratingColor, ratingType,
					ratingValue, overallRating));
			Debug.print("\n");
		}
	}

	/** Sort a card triple by values in the given stack */
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
		Debug.println(this, "My stack: " + this.cardStack.toString());

		// regenerate priority list of needed cards
		this.stackRating.calculateNeededCards();

		// get cards on table
		index = 0;
		for (byte card : table.getCards()) {
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
		// make a pick suggestion
		Debug.println(
				this,
				"Thought: I'll pick "
						+ CardStack.cardToString(this.cardsTableTriple[0]));

		// rate own cards
		this.rateCards();

		// dump rating stack
		if (Debug.debug == true) {
			Debug.print(this, "My need stack:\n"
					+ this.cardStackNeed.dumpStack().toString() + "\n");
		}

		// estimate goal distance
		// TODO: goal reached?
		if (Debug.debug == true) {
			byte[] goalDistance = this.stackRating
					.getGoalDistance(this.cardStack.getCards());
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
				Debug.print("\n");
			} else {
				Debug.println(this, "Goal not in sight.");
			}
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

	@Override
	public void handleTableEvent(Table.Event event, Object data) {
		switch (event) {
		case INITIAL_CARDS_DROPPED:
			this.cardStackTable.addCards((byte[]) data);
			break;
		}
	}
}
