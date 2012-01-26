package swimgame.player.rating;

import swimgame.table.CardStack;
import swimgame.table.DefaultTableController;
import swimgame.table.logic.TableLogic;

public class Stack {
    public static final int NO_RESULT = -1;

    /**
     * Check, how many cards are missing to reach a goal state. No calculation
     * of values is involved here
     * 
     * @return How many cards are missing to reach a goal state
     *         [color][steps][type][steps]
     */
    public static final byte[] goalDistance(final CardStack searchStack,
	    final byte[] cards) {
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
	    final int color = CardStack.getCardColor(card);
	    for (byte cardByColor : CardStack.getCardsByColor(color)) {
		if (searchStack.hasCard(cardByColor)) {
		    count++;
		}
	    }
	    // store max
	    if (count > valueColor) {
		valueColor = count;
		positionColor = (byte) color;
	    }

	    count = 0;
	    final int type = CardStack.getCardType(card);
	    for (byte cardByType : CardStack.getCardsByType(type)) {
		if (searchStack.hasCard(cardByType)) {
		    count++;
		}
	    }
	    // store max
	    if (count > valueType) {
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
	    distances[3] = (byte) (TableLogic.RULE_GOAL_CARDS_BY_TYPE - valueType);
	}
	return distances;
    }

    public static final byte[] goalDistance(CardStack cardStack) {
	return Stack.goalDistance(cardStack, cardStack.getCards());
    }

    public static final double dropValue(final CardStack searchStack,
	    final byte[] cards) {
	double value;
	final byte[] goalDistances = Stack.goalDistance(searchStack, cards);
	if ((goalDistances[1] == 0) || (goalDistances[3] == 0)) {
	    value = searchStack.getValue();
	} else {
	    value = Stack.NO_RESULT;
	}
	return value;
    }

    public static final double dropValue(final CardStack cardStack) {
	return Stack.dropValue(cardStack, cardStack.getCards());
    }

    /**
     * Update a rating array for all currently not-owned cards. This may be
     * helpful to decide, witch card to pick from the table. This will directly
     * modify the {@link CardStack} given as second parameter.
     * 
     * @param searchStack
     *            {@link CardStack} with cards currently owned
     * @param needStack
     *            {@link CardStack} to fill with need-priority values
     */
    public static void calculateNeededCards(final CardStack searchStack,
	    final CardStack needStack) {
	needStack.fill(CardStack.FLAG_UNINITIALIZED);
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
		if (searchStack.hasCard(cardToCheck)) {
		    matches++;
		}
	    }

	    // skip further rating, if no card found in current color
	    if (matches > 0) {
		// color: rate the missing ones based on the number of
		// matches
		for (int cardNum = 0; cardNum < CardStack.CARDS_MAX_CARD; cardNum++) {
		    int cardToCheck = cardOffset + cardNum;
		    if (!searchStack.hasCard(cardToCheck)) {
			int ratingValue = Card.getCardRating(cardToCheck)
				+ (matches * Card.INFLUENCE_SAME_COLOR);
			Card.uprate(needStack, cardToCheck, ratingValue);
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
		    // card for matches
		    if (searchStack.hasCard(ownedCard)) {
			typeChecked[Card.getCardRating(cardNum)] = 1;
			for (int i = cardsColor; i < CardStack.CARDS_MAX_COLOR; i++) {
			    int cardToCheck = cardNum + (i * cardOffset);
			    if ((cardToCheck != ownedCard)
				    && searchStack.hasCard(cardToCheck)) {
				int ratingValue = needStack
					.getCardValue(cardToCheck)
					+ Card.INFLUENCE_SAME_TYPE
					+ (int) (DefaultTableController.WORTH_THREE_OF_SAME_TYPE / 3);

				for (int fixedTypeCard = 0; fixedTypeCard < CardStack.CARDS_MAX_COLOR; fixedTypeCard++) {
				    int cardToRate = cardNum
					    + (fixedTypeCard * cardOffset);
				    if (!searchStack.hasCard(cardToRate)) {
					Card.uprate(needStack, cardToRate,
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
