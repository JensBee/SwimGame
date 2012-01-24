package swimgame.player.rating;

import swimgame.table.CardStack;
import swimgame.table.logic.TableLogic;

public class Stack {
    /**
     * Check, how many cards are missing to reach a goal state. No calculation
     * of values is involved here
     * 
     * @return How many cards are missing to reach a goal state
     *         [color][steps][type][steps]
     */
    static byte[] goalDistance(final CardStack searchStack, final byte[] cards) {
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

}
