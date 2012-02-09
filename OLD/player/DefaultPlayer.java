package swimgame.player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import swimgame.out.Console;
import swimgame.out.Debug;
import swimgame.table.CardStack;
import swimgame.table.logic.TableLogic;
import cardGame.card.CardDeck;
import cardGame.event.CardGameEvent;
import cardGame.games.swimming.PlayerAICard;
import cardGame.games.swimming.PlayerAIStackOLD;
import cardGame.player.CardPlayer;
import cardGame.util.Util;

/**
 * A player playing the game. The default implementation of a player.
 * 
 * <pre>
 * TODO:
 * 	- handle last round (drop & pick calculation)
 * 	- strategy: if player took lead with n points then drop the cards with 
 * 	  a probability of 0.5 if possible, just to keep the lead
 * 	- make use of modified biases
 * </pre>
 * 
 * @author <a href="mailto:code@jens-bertram.net">Jens Bertram</a>
 * 
 */
public class DefaultPlayer implements CardPlayer {
    /** List of predefined player names. */
    private static List<String> playerNames = new ArrayList<String>(
	    Arrays.asList("Bob", "Alice", "Carol", "Dave", "Ted", "Eve",
		    "Oscar", "Peggy", "Victor"));
    /** Cards seen on the table. */
    private CardStack cardStackTable;
    /** Cards we want to get. */
    private CardStack cardStackNeed;
    /** Cards stack owned by this player. */
    private CardStack cardStack;
    /** rating cards. */
    private PlayerAICard cardRating;
    /** Cards on the table to decide on (if it's our turn). */
    private CardDeck.Card[] cardsTableTriple = new CardDeck.Card[3];
    /** {@link TableLogic} of the game table. */
    private final TableLogic tableLogic;
    /** The name of this {@link CardPlayer} instance. */
    private final String name;
    /** Game close called? */
    private boolean gameIsClosed = false;
    /** Store modified {@link Bias} settings. */
    private final double[] bias = new double[Bias.values().length];
    /** Default value for an uninitialized bias setting. */
    private static final int BIAS_UNSET = -1;

    /**
     * Player behavior bias. These are the possible biases to set with their
     * default values. Modifying these values is possible by using
     * {@link DefaultPlayer#setBias(Bias, int)} or
     * {@link DefaultPlayer#setBias(Map)}
     */
    public enum Bias {
	/** TODO: document bias values. */
	STACKDROP_INITIAL(20),
	/** */
	STACKDROP(20),
	/** */
	FORCE_DROP(20),
	/** */
	WAIT_FOR_CARD(21);

	/** Current value for this {@link Bias} instance. */
	private double value;

	/**
	 * Constructor.
	 * 
	 * @param newValue
	 *            New value for this {@link Bias} instance
	 */
	Bias(final double newValue) {
	    this.value = newValue;
	}

	/**
	 * Get the value of this {@link Bias} instance.
	 * 
	 * @return Current value for this {@link Bias} instance
	 */
	public final double getValue() {
	    return this.value;
	}
    }

    /**
     * Set player bias values.
     * 
     * @param biasMap
     *            Map with {@link Bias} as key and a {@link Double} as value.
     *            The double must be in the range 0-1.
     */
    public final void setBias(final Map<Bias, Double> biasMap) {
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
    public final void setBiasValue(final Bias biasName, final double value) {
	if ((value < 0) || (value > CardStack.STACKVALUE_MAX)) {
	    throw new IllegalArgumentException(String.format(
		    "Bias value %f not in the range 0-1.", value));
	}
	this.bias[biasName.ordinal()] = value;
    }

    public void setCardBiasValue(final Map<PlayerAICard.Bias, Double> biasMap) {
	this.cardRating.setBiasValue(biasMap);
    }

    public void setCardBiasValue(final PlayerAICard.Bias biasName, final double value) {
	this.cardRating.setBiasValue(biasName, value);
    }

    public final double getBiasValue(final Bias biasName) {
	double biasValue = this.bias[biasName.ordinal()];
	if (biasValue != -1) {
	    return biasValue;
	}
	return biasName.getValue();
    }

    /**
     * Default constructor.
     * 
     * @param newTableLogic
     *            {@link TableLogic} of the game table
     */
    public DefaultPlayer(final TableLogic newTableLogic) {
	this.name = this.getRandomName();
	this.tableLogic = newTableLogic;
	this.initialize();
    }

    /**
     * Constructor that allows direct setting of the players name.
     * 
     * @param newTableLogic
     *            {@link TableLogic} of the game table
     * @param playerName
     *            Name of this player
     */
    public DefaultPlayer(final TableLogic newTableLogic, final String playerName) {
	this.tableLogic = newTableLogic;
	this.name = playerName;
	this.initialize();
    }

    /**
     * Get the {@link CardStack} owned by this player instance.
     * 
     * @return {@link CardStack} owned by this player instance
     */
    CardStack getCardStack() {
	return this.cardStack;
    }

    private void initialize() {
	this.gameIsClosed = false;
	// set the configurable bias values as uninitialized
	Arrays.fill(this.bias, DefaultPlayer.BIAS_UNSET);
	// init game variables
	this.cardStackTable = new CardStack();
	this.cardStackNeed = new CardStack();
	this.cardStackTable.fill((byte) PlayerAICard.Rate.AVAILABILITY_UNSEEN);

	if (this.cardRating != null) {
	    this.cardRating.reset();
	} else {
	    this.cardRating = new PlayerAICard();
	}
    }

    /**
     * Decide (if able to), if our initial card set is good enough.
     * 
     * @return True if player wants to keep his cards
     */
    public final boolean keepCardSet() {
	Debug.println(Debug.TALK, this, "Deciding on my current card set: "
		+ this.cardStack);

	if (this.cardStack.getValue() < this
		.getBiasValue(Bias.STACKDROP_INITIAL)) {
	    // drop immediately if below threshold
	    Debug.println(Debug.TALK, this, String.format(
		    "Dropping (below threshold (%f))",
		    Bias.STACKDROP_INITIAL.getValue()));

	    this.log("Uhm... no!");
	    return false;
	}

	Debug.println(Debug.TALK, this, "Ok, I'm keeping this card set");
	return true;
    }

    /**
     * Rate our current cards to decide witch we'll probably drop. Results go
     * into the needed cards array to save space.
     */
    private void rateCards() {
	Debug.println(Debug.TALK, this, "Rating my current cards..");
	for (CardDeck.Card card : this.cardStack.getCards()) {
	    this.cardStackNeed.setCardValue(card,
		    (byte) this.cardRating.getRating(this.cardStack, card));
	}
    }

    /**
     * Sort a triple of cards according to values in a stack.
     * 
     * @param stack
     *            The stack to get the card values from
     * @param triple
     *            Array containing the card-indices for the cards to sort
     * @return The input array sorted by card-values
     */
    private CardDeck.Card[] sortCardTriple(final CardStack stack,
	    final CardDeck.Card[] triple) {
	CardDeck.Card[] sortedTriple = new CardDeck.Card[3];

	for (int i = 0; i < 3; i++) {
	    for (int j = 0; j < 3; j++) {
		int sourceValue =
			(triple[i] == null ? CardStack.FLAG_UNINITIALIZED
				: stack.getCardValue(triple[i]));
		int targetValue =
			(sortedTriple[j] == null ? CardStack.FLAG_UNINITIALIZED
				: stack.getCardValue(sortedTriple[j]));
		if (sourceValue >= targetValue) {
		    // shift content..
		    if (j == 0) {
			sortedTriple[2] = sortedTriple[1];
			sortedTriple[1] = sortedTriple[0];
		    } else if (j == 1) {
			sortedTriple[2] = sortedTriple[1];
		    }
		    // ..to insert the new
		    sortedTriple[j] = triple[i];
		    break;
		}
	    }
	}
	return sortedTriple;
    }

    public final void doMove(final CardDeck.Card[] tableCards) {
	int index;
	double dropValue;
	Debug.println(Debug.INFO, this,
		"My stack: " + this.cardStack.toString());

	// regenerate priority list of needed cards
	PlayerAIStackOLD.calculateNeededCards(this.cardStack, this.cardStackNeed);

	// get cards on table
	index = 0;
	for (CardDeck.Card card : tableCards) {
	    this.cardsTableTriple[index++] = card;
	}

	// rate table cards
	this.cardsTableTriple =
		this.sortCardTriple(this.cardStackNeed, this.cardsTableTriple);
	if (Debug.debug && (Debug.debugLevel == Debug.INFO)) {
	    Debug.print(Debug.INFO, this, "My table cards rating: ");
	    for (CardDeck.Card card : this.cardsTableTriple) {
		Debug.print(
			Debug.INFO,
			String.format("%s:%d ", card,
				this.cardStackNeed.getCardValue(card)));
	    }
	    Debug.print(Debug.INFO, "\n");
	}
	// make a pick suggestion
	Debug.println(Debug.INFO, this, "?pick " + this.cardsTableTriple[0]);

	// rate own cards
	this.rateCards();

	// dump rating stack
	Debug.print(Debug.TALK, this, "My need stack:\n"
		+ this.cardStackNeed.dump().toString() + "\n");

	// estimate goal distance
	Object[] goalDistance = PlayerAIStackOLD.goalDistance(this.cardStack);
	if (Debug.debug) {
	    if ((goalDistance[0] != null) || (goalDistance[2] != null)) {
		Debug.print(Debug.INFO, this, "Goal distance(s): ");
		if ((CardDeck.Color) goalDistance[0] != null) {
		    Debug.print(Debug.INFO, String.format("[%s?]:%d ",
			    goalDistance[0], goalDistance[1]));
		}
		if ((CardDeck.Type) goalDistance[2] != null) {
		    Debug.print(Debug.INFO, String.format("[?%s]:%d",
			    goalDistance[2], goalDistance[3]));
		}
		Debug.nl(Debug.INFO);
	    } else {
		Debug.println(Debug.TALK, this, "Goal not in sight.");
	    }
	}

	// stack value
	dropValue = PlayerAIStackOLD.dropValue(this.cardStack);
	Debug.println(Debug.INFO, this, "Drop value: " + dropValue);

	// make a drop suggestion
	Object[] cardToDrop = new Object[2];
	cardToDrop[1] = new Integer(127); // make sure all cards will be lower
	for (CardDeck.Card card : this.cardStack.getCards()) {
	    Integer cardValue =
		    new Integer(this.cardStackNeed.getCardValue(card));
	    if ((cardValue == -1) || (cardValue < (Integer) cardToDrop[1])) {
		cardToDrop[0] = card;
		cardToDrop[1] = cardValue;
	    }
	}
	Debug.println(Debug.INFO, this, "?drop " + cardToDrop[0]);

	// end game?
	boolean endCalled = false;
	if (PlayerAIStackOLD.canDrop(this.cardStack)) {
	    if (Debug.debug) {
		Debug.println(Debug.INFO, this, "<<knocking!>>");
	    }
	    this.log("*knock!, knock!*");
	    endCalled =
		    this.tableLogic.interact(TableLogic.Action.END_CALL,
			    this.cardStack.getCards());
	}

	if (!endCalled) {
	    if (cardToDrop[0] == null) {
		// TODO: evaluate skip action
	    } else {
		// drop & pick
		System.out.println("PickDrop..");
		if (this.tableLogic.interact(TableLogic.Action.DROP_CARD,
			cardToDrop[0])
			&& this.tableLogic.interact(
				TableLogic.Action.PICK_CARD,
				this.cardsTableTriple[0])) {
		    try {
			Debug.printf(Debug.INFO, this, "Drop %s Pick %s\n",
				cardToDrop[0], this.cardsTableTriple[0]);
			this.cardStack
				.removeCard((CardDeck.Card) cardToDrop[0]);
			this.cardStack.addCard(this.cardsTableTriple[0]);
			System.out.println("PickDrop!");
		    } catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		    }
		}
	    }
	}
	// finished
	this.tableLogic.interact(TableLogic.Action.MOVE_FINISHED);
    }

    @Override
    public final void handleEvent(final Enum<? extends CardGameEvent> event,
	    final Object data) {

	if (event.getClass().equals(TableLogic.Event.class)) {
	    switch ((TableLogic.Event) event) {
	    case INITIAL_CARDSTACK_DROPPED:
		this.cardStackTable.addCard((CardDeck.Card[]) data);
		break;
	    case GAME_CLOSED:
		this.gameIsClosed = true;
		break;
	    case GAME_START:
		this.initialize();
		break;
	    case CARD_DROPPED:
		this.cardRating.cardSeen((CardDeck.Card) data);
		break;
	    default:
		break;
	    }
	}
    }

    @Override
    public final String getName() {
	return this.name;
    }

    /**
     * Set the {@link CardStack} owned by this player instance.
     * 
     * @param newCardStack
     *            New {@link CardStack} to set for this player
     */
    void setCardStack(CardStack newCardStack) {
	this.cardStack = newCardStack;
    }

    @Override
    public cardGame.card.CardDeck.Card[] getCards() {
	return this.cardStack.getCards();
    }

    /**
     * Return the name of this player.
     * 
     * @return The name of this player
     */
    @Override
    public String toString() {
	return this.getName();
    }

    /**
     * Log a message to the console.
     * 
     * @param message
     *            The message to log
     */
    void log(final String message) {
	Console.println(this, message);
    }

    /**
     * Get a random player name.
     * 
     * @return Player name chosen from a predefined set
     */
    String getRandomName() {
	return playerNames.remove(Util.getRandomInt(playerNames.size() - 1));
    }

    @Override
    public void setCards(final CardDeck.Card[] cards) {
	this.setCardStack(new CardStack(cards));
	Debug.printf(Debug.INFO, this, "Recieved cards: %s\n",
		this.getCardStack());
	this.cardRating.reset();
	// store our own cards as being in the game
	for (cardGame.card.CardDeck.Card card : cards) {
	    this.cardRating.cardSeen(card);
	}
    }
}
