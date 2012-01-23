package swimgame.table.logic;

import swimgame.out.Debug;
import swimgame.table.CardStack;
import swimgame.table.ITableController;

/**
 * Handles all the stuff thats needed to get the game running. Parts of the game
 * are implemented by specific Classes in the same package.
 * 
 * @author <a href="mailto:code@jens-bertram.net">Jens Bertram</a>
 * 
 */
public class TableLogic {
    /** The {@link Table} handling the game. */
    private final Table table;
    /** Defines, how many cards do each player get at the start of the game. */
    public static final int INITIAL_CARDS = 3;
    /** Maximum number of allowed players. */
    public static final int MAX_PLAYER = 9;
    /** How many rounds per game should be played as default in maximum. */
    public static final int DEFAULT_MAX_ROUNDS = 32;
    /** How many cards needed by type to be in goal state? */
    public static final int RULE_GOAL_CARDS_BY_TYPE = 3;
    /** How many cards needed by color to be in goal state? */
    public static final int RULE_GOAL_CARDS_BY_COLOR = 3;
    /** {@link ITableController} instance controlling this logic. */
    private final ITableController tableController;

    /** Possible events a player can fire players during the game. */
    public enum Event {
	/** Table is closed, game will start shortly. */
	TABLE_CLOSED,
	/** Game is starting now. */
	GAME_START,
	/**
	 * A player dropped his initial card set, to get another one.<br>
	 * Data: int[6] array with three cards 3*(card,color)
	 */
	INITIAL_CARDSTACK_DROPPED,
	/** A player called for closing the game. The last round is running now. */
	GAME_CLOSED,
	/** Game has finished. */
	GAME_FINISHED,
	/**
	 * A card has been dropped by a user. <br>
	 * Data: (byte) card
	 */
	CARD_DROPPED, ;
    }

    /** Player actions to interact with the table. */
    public enum Action {
	/**
	 * Player drops a card to the table.<br>
	 * Data: (byte) card {@see CardStack}
	 */
	DROP_CARD,
	/** Player drops his initial card set, to get another one. */
	DROP_CARDSTACK_INITIAL,
	/** Player has finished his move. */
	MOVE_FINISHED,
	/** Player called to end this round. */
	END_CALL,
	/**
	 * Player wants to pick a card from the table. <br>
	 * Data: (byte) card {@see CardStack}
	 */
	PICK_CARD,
	/** A player has picked the initial card stack. */
	INITIAL_CARDSTACK_PICKED,
    }

    /**
     * Default constructor.
     * 
     * @param newTableController
     *            The Controller for this Table
     */
    public TableLogic(final ITableController newTableController) {
	this.tableController = newTableController;
	this.table = new Table(this);
    }

    /**
     * Constructor with setting the number of games to play.
     * 
     * @param newTableController
     *            The {@link ITableController} for this Table
     * @param gamesToPlay
     *            The number of games to play
     */
    public TableLogic(final ITableController newTableController,
	    final int gamesToPlay) {
	this.tableController = newTableController;
	this.table = new Table(this, gamesToPlay);
    }

    /**
     * Constructor with setting the number of games and maximum number of rounds
     * per game.
     * 
     * @param newTableController
     *            The {@link ITableController} for this Table
     * @param maxRoundsPerGame
     *            The maximum number of rounds per game
     * @param gamesToPlay
     *            The number of games to play
     */
    public TableLogic(final ITableController newTableController,
	    final int maxRoundsPerGame, final int gamesToPlay) {
	this.tableController = newTableController;
	this.table = new Table(this, gamesToPlay, maxRoundsPerGame);
    }

    /**
     * Get the {@link Table} instance associated with this {@link TableLogic}
     * instance.
     * 
     * @return The {@link ITableController} associated with this TableLogic.
     */
    public final Table getTable() {
	return this.table;
    }

    /**
     * Verifies the goal state of a given list of cards.
     * 
     * @param cards
     *            The cards, for witch the goal state should be verified
     * @return True if in goal state (cards might be dropped)
     */
    static boolean verifyGoal(final byte[] cards) {
	CardStack userCardStack;
	byte cardsCount;

	try {
	    userCardStack = new CardStack(cards);
	} catch (Exception e) {
	    return false;
	}

	// three of a color?
	cardsCount = 0;
	for (byte card : CardStack.getCardsByColor(CardStack
		.getCardColor(cards[0]))) {
	    if (userCardStack.hasCard(card)) {
		cardsCount++;
	    }
	}
	if (cardsCount == RULE_GOAL_CARDS_BY_COLOR) {
	    Debug.println(Debug.INFO, Table.class, "Three of a color!");
	    return true;
	}

	// three of a type?
	cardsCount = 0;
	for (byte card : CardStack.getCardsByType(CardStack
		.getCardType(cards[0]))) {
	    if (userCardStack.hasCard(card)) {
		cardsCount++;
	    }
	}
	if (cardsCount == RULE_GOAL_CARDS_BY_TYPE) {
	    Debug.println(Debug.INFO, Table.class, "Three of a type!");
	    return true;
	}

	return false;
    }

    /**
     * Player callback function to respond to a {@link TableLogic#Event} fired
     * by the associated {@link Table}.
     * 
     * @param action
     *            The {@link TableLogic#Action} that has been taken by the
     *            player
     * @param data
     *            The data associated with the {@link TableLogic#Action}
     * @return True if {@link TableLogic#Action} was accepted
     */
    public final boolean interact(final Action action, final Object data) {
	switch (action) {
	case DROP_CARDSTACK_INITIAL:
	    this.proxyInteractionEvent(action, data);
	    return true;
	case DROP_CARD:
	    CardStack.validateCardIndex((byte) data);
	    this.table.getCardStack().addCard((byte) data);
	    this.table.player.setTookAction();
	    this.table.player.fireEvent(Event.CARD_DROPPED, data);
	    this.proxyInteractionEvent(action, data);
	    break;
	case END_CALL:
	    byte[] userCards = (byte[]) data;
	    try {
		if (TableLogic.verifyGoal(userCards)
			&& this.table.game.round.close()) {
		    this.table.game.round.close();
		    this.table.player.fireEvent(Event.GAME_CLOSED);
		    this.table.player.setTookAction();
		    this.proxyInteractionEvent(action, data);
		    return true;
		}
	    } catch (TableLogicException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
		return false;
	    }
	    return false;
	case MOVE_FINISHED:
	    this.table.player.setTurnFinished();
	    this.proxyInteractionEvent(action, data);
	    return true;
	case PICK_CARD:
	    try {
		this.table.getCardStack().removeCard((byte) data);
		this.table.player.setTookAction();
		this.proxyInteractionEvent(action, data);
		return true;
	    } catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		return false;
	    }
	default:
	    return false;
	}
	return false;
    }

    /**
     * Player callback function to respond to a {@link TableLogic#Event} fired
     * by the associated {@link Table}.
     * 
     * @param action
     *            The {@link TableLogic#Action} that has been taken by the
     *            player
     * @return True if {@link TableLogic#Action} was accepted
     */
    public final boolean interact(final Action action) {
	return this.interact(action, null);
    }

    /**
     * Proxy player events to the {@link ITableController} instance associated
     * with this {@link TableLogic} instance.
     * 
     * @param action
     *            The {@link TableLogic#Action} that should be forwarded
     * @param data
     *            The data associated with this {@link TableLogic#Action}
     */
    final void proxyInteractionEvent(final Action action, final Object data) {
	this.tableController.handleTableLogicEvent(action, data);
    }

    /** Setup the table logic. */
    public final void initialize() {
	if (!this.table.isClosed()) {
	    this.table.close();
	    this.table.player.fireEvent(Event.TABLE_CLOSED, null);
	    if (Debug.debug) {
		Debug.println(Debug.INFO, this.getClass(), String.format(
			"Players: %d  Maximum rounds: %d",
			this.table.player.amount(),
			this.table.game.round.getMaxLength()));
	    }
	}
	this.table.player.fireEvent(TableLogic.Event.GAME_START);
    }
}
