package cardGame.games.swimming;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import cardGame.card.CardDeck.Card;
import cardGame.event.CardGameEvent;
import cardGame.event.EventBus;
import cardGame.out.Debug;
import cardGame.player.GeneralCardPlayer;
import cardGame.table.GeneralGameTable;

/**
 * Computer AI player.
 * 
 * @author <a href="mailto:code@jens-bertram.net">Jens Bertram</a>
 * 
 */
class AIPlayer extends GeneralCardPlayer {
    /** Store modified {@link Bias} settings. */
    private final float[] bias = new float[Bias.values().length];
    /** Players cards. */
    private final CardStack cardStack = new CardStack();
    /** Cards on the table. */
    private final CardStack cardsTable = new CardStack();
    /** Cards wanted by the player. */
    private final CardStack cardsWanted = new CardStack();
    /** Cards seen by the player. */
    private final CardStack cardsSeen = new CardStack();
    /** Card rating functions. */
    private AIPlayerRating rating;

    /**
     * Player behavior bias. These are the possible biases to set with their
     * default values. Modifying these values is possible by using {@link
     * setBias(Bias, int)} or {@link setBias(Map)}
     */
    enum Bias {
	/** TODO: document bias values. */
	STACKDROP_INITIAL(20),
	/** */
	STACKDROP(20),
	/** */
	FORCE_DROP(20),
	/** */
	WAIT_FOR_CARD(21);

	/** Current value for this {@link Bias} instance. */
	private float value;

	/**
	 * Constructor.
	 * 
	 * @param newValue
	 *            New value for this {@link Bias} instance
	 */
	Bias(final float newValue) {
	    this.value = newValue;
	}

	/**
	 * Get the value of this {@link Bias} instance.
	 * 
	 * @return Current value for this {@link Bias} instance
	 */
	final float getValue() {
	    return this.value;
	}
    }

    /** Empty Constructor. */
    AIPlayer() {
	super();
	this.initialize();
    }

    /**
     * @param newName
     *            Players name
     */
    AIPlayer(final String newName) {
	super(newName);
	this.initialize();
    }

    /**
     * Get a bias value.
     * 
     * @param biasName
     *            The bias to get
     * @return The bias value
     */
    final float getBiasValue(final Bias biasName) {
	float biasValue = this.bias[biasName.ordinal()];
	if (biasValue != -1) {
	    return biasValue;
	}
	return biasName.getValue();
    }

    /**
     * Set player bias values.
     * 
     * @param biasMap
     *            Map with Bias as key and a Float as value. The float must be
     *            in the range 0-1.
     */
    final void setBias(final Map<Bias, Float> biasMap) {
	for (Bias biasName : biasMap.keySet()) {
	    this.setBiasValue(biasName, biasMap.get(biasName));
	}
    }

    /**
     * Set a single player bias value.
     * 
     * @param biasName
     *            The bias to modify
     * @param value
     *            The new value to set
     */
    final void setBiasValue(final Bias biasName, final float value) {
	if ((value < 0) || (value > CardStack.STACKVALUE_MAX)) {
	    throw new IllegalArgumentException(String.format(
		    "Bias value %f not in the range 0-1.", value));
	}
	this.bias[biasName.ordinal()] = value;
    }

    /** Register to events listening to. */
    private void initialize() {
	// TODO: reduce event listeners
	EventBus.INSTANCE.registerEventReceiver(this, GameLogic.Event.values());
	EventBus.INSTANCE.registerEventReceiver(this, Table.Event.values());
	// pass on the shared objects to the rating class
	this.rating =
		new AIPlayerRating(this.cardStack, this.cardsWanted,
			this.cardsTable, this.cardsSeen);
    }

    @SuppressWarnings("unchecked")
    @Override
    public final void handleEvent(final CardGameEvent event, final Object data) {
	Class<? extends CardGameEvent> eventClass = event.getClass();
	// Table event
	if (eventClass.equals(GeneralGameTable.Event.class)) {
	    switch ((GeneralGameTable.Event) event) {
	    case CLOSED:
		break;
	    default:
		break;
	    }
	}
	if (eventClass.equals(Table.Event.class)) {
	    switch ((Table.Event) event) {
	    case CARDS:
		// Initial cards on the table. This means also a new game has
		// begun.
		this.cardsTable.setCards((Set<Card>) data);
		this.cardsSeen.setCards((Set<Card>) data);
		break;
	    case CARD_DROP:
		this.cardsTable.addCard((Card) data);
		this.cardsSeen.addCard((Card) data);
		break;
	    case CARD_PICK:
		this.cardsTable.removeCard((Card) data);
		break;
	    default:
		break;
	    }
	} else if (eventClass.equals(GameLogic.Event.class)) {
	    switch ((GameLogic.Event) event) {
	    case NEXTPLAYER:
		if (data.equals(this)) {
		    this.play();
		}
		break;
	    default:
		break;
	    }
	}
    }

    @Override
    public final boolean setCards(final Collection<Card> newCards) {
	// TODO: only accept cards prior to first round, otherwise throw an
	// exception
	this.cardStack.resetCardValues();
	this.cardStack.setCards(newCards);
	Debug.printfn(Debug.Level.TALK, "<%s> Received cards:\n%s", this,
		this.cardStack.dump());
	return true;
    }

    @Override
    public final Collection<Card> getCards() {
	// TODO: only respond after last round, throw exception otherwise
	// TODO Auto-generated method stub
	return null;
    }

    /** Game interaction function. */
    private void play() {
	Card cardToPick = null;
	Card cardToDrop = null;
	Object[] goalDistance = this.rating.goalDistance();
	Debug.printfn(Debug.Level.INFO, "<%s> My Cards: %s", this,
		this.cardStack);

	Debug.printfn(Debug.Level.TALK, "<%s> May table view:\n%s", this,
		this.cardsTable.dump());

	// now "intelligently" decide on cards
	if ((goalDistance[0] != null) || (goalDistance[2] != null)) {
	    // can we get three of a type?
	    if (goalDistance[2] != null) {
		for (Card card : this.cardsTable.getCards()) {
		    if (card.getType().equals(goalDistance[2])) {
			cardToPick = card;
			// no matter witch card it is . the points are fixed
			break;
		    }
		}
	    }
	    // can we get three of a color?
	    if ((goalDistance[0] != null) && (cardToPick == null)) {
		for (Card card : this.cardsTable.getCards()) {
		    if (card.getColor().equals(goalDistance[0])) {
			// check all cards to find the highest
			if ((cardToPick == null)
				|| (Table.getCardValue(cardToPick) < Table
					.getCardValue(card))) {
			    cardToPick = card;
			}
		    }
		}
	    }
	}

	// if we are in goal state: simply close the round
	if (((goalDistance[0] != null) && ((Integer) goalDistance[1] == 0))
		|| ((goalDistance[2] != null) && ((Integer) goalDistance[3] == 0))) {
	    this.table.addInteraction(this, Table.Action.CLOSE, null);
	    this.table.addInteraction(this, Table.Action.FINISHED, null);
	    if (this.table.commitInteraction(this) == null) {
		System.out.println("I shouldn't be here!");
		return;
	    }
	}

	if (cardToPick != null) {
	    Debug.printfn(Debug.Level.INFO, "<%s> Pick suggestion: %s", this,
		    cardToPick);
	    this.table.addInteraction(this, Table.Action.CARD_PICK, cardToPick);
	} else {
	    cardToPick = this.rating.suggestRandomPick();
	    Debug.printfn(Debug.Level.INFO, "<%s> Random pick suggestion: %s",
		    this, cardToPick);
	    this.table.addInteraction(this, Table.Action.CARD_PICK, cardToPick);
	}

	// TODO: simple drop
	for (Card card : this.cardStack.getCards()) {
	    if ((!card.getType().equals(goalDistance[2]))
		    && (!card.getColor().equals(goalDistance[0]))) {
		cardToDrop = card;
		break;
	    }
	}
	if (cardToDrop != null) {
	    Debug.printfn(Debug.Level.INFO, "<%s> Drop suggestion: %s", this,
		    cardToDrop);
	    this.table.addInteraction(this, Table.Action.CARD_DROP, cardToDrop);
	}

	if (this.table.commitInteraction(this) == null) {
	    // pick & drop successful, save new cards
	    this.cardStack.removeCard(cardToDrop);
	    this.cardStack.addCard(cardToPick);
	}
    }
}
