package swimgame.table.logic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import swimgame.table.logic.TableLogic.Event;
import cardGame.player.CardPlayer;

/**
 * Handles player playing on a table.
 * 
 * @author <a href="mailto:code@jens-bertram.net">Jens Bertram</a>
 * 
 */
public class Player {
    /** List of {@link CardPlayer} joined the associated {@link Table} instance. */
    // LinkedHashMap to ensure keeping the element ordering
    private final Map<CardPlayer, Double> list =
	    new LinkedHashMap<CardPlayer, Double>();
    /** Has current {@link CardPlayer} performed an action before passing on? */
    private boolean tookAction = false;
    /** Tracks if an {@link CardPlayer} has finished his current turn. */
    private boolean finishedTurn = false;
    /** Stores {@link CardPlayer} with their game points. */
    private final PlayerComparator playerComparator = new PlayerComparator();

    /** Compares {@link CardPlayer} by their overall game points. */
    private class PlayerComparator implements Comparator<CardPlayer> {
	@Override
	public int compare(final CardPlayer o1, final CardPlayer o2) {
	    Double p1 = Player.this.list.get(o1);
	    Double p2 = Player.this.list.get(o2);
	    if (p1 < p2) {
		return -1;
	    }
	    if (p1.equals(p2)) {
		return 0;
	    }
	    return 1;
	}
    }

    /**
     * Has the current {@link CardPlayer} already taken an action while in turn?
     * 
     * @return True if he has
     */
    public final boolean getTookAction() {
	return this.tookAction;
    }

    /**
     * Set flag that the current {@link CardPlayer} took an
     * {@link TableLogic.Action} on his turn to true.
     */
    final void setTookAction() {
	this.setTookAction(true);
    }

    /**
     * Set flag that {@link CardPlayer} took an {@link TableLogic.Action} on his
     * turn.
     * 
     * @param state
     *            True if he has taken an {@link TableLogic.Action}
     */
    final void setTookAction(final boolean state) {
	this.tookAction = state;
    }

    /**
     * Check if the current {@link CardPlayer} has finished his turn.
     * 
     * @return True if {@link CardPlayer} has finished his turn
     */
    public final boolean isTurnFinished() {
	return this.finishedTurn;
    }

    /** Set the current {@link CardPlayer} turn state to finished. */
    final void setTurnFinished() {
	this.finishedTurn = true;
    }

    /**
     * Get the list of {@link CardPlayer} playing on the associated {@link Table}.
     * 
     * @return List of {@link Player} currently playing on this table. The order
     *         of players is not guaranteed to be the playing order.
     */
    public final List<CardPlayer> asList() {
	return new ArrayList<CardPlayer>(this.list.keySet());
    }

    /**
     * Add a {@link CardPlayer} to the list.
     * 
     * @param player
     *            The new {@link CardPlayer} to add
     */
    final void add(final CardPlayer player) {
	this.list.put(player, new Double(0));
    }

    /**
     * Get a new {@link PlayerIterator} iterator for all players currently in
     * the game.
     * 
     * @param wrapAround
     *            If true the iterator will wrap around at the end.
     * @see PlayerIterator#PlayerIterator(boolean)
     * @return A new {@link PlayerIterator}
     */
    public final PlayerIterator iterator(final boolean wrapAround) {
	return new PlayerIterator(wrapAround);
    }

    /**
     * Add points to the overall points counter.
     * 
     * @param player
     *            The {@link CardPlayer} witch will get the points
     * @param pointsToAdd
     *            The points to add
     */
    final void addPoints(final CardPlayer player, final Double pointsToAdd) {
	final Double currentPoints = this.getPoints(player);
	this.list.put(player, currentPoints + pointsToAdd);
    }

    /**
     * Get the all-games points for a {@link CardPlayer}.
     * 
     * @param player
     *            The {@link CardPlayer} whose points should be returned
     * @return The points for the given player
     */
    public final double getPoints(final CardPlayer player) {
	return this.list.get(player);
    }

    /**
     * Get the list of {@link CardPlayer} ranked by their overall game-points.
     * 
     * @return List of {@link CardPlayer}, ordered by their game points
     */
    public final Map<CardPlayer, Double> getRanked() {
	final List<CardPlayer> keys = new ArrayList<CardPlayer>(this.list.size());
	keys.addAll(this.list.keySet());
	// get rating from high to low (reverse!)
	Collections.sort(keys, Collections.reverseOrder(this.playerComparator));
	final Map<CardPlayer, Double> rankedMap =
		new LinkedHashMap<CardPlayer, Double>();
	for (CardPlayer player : keys) {
	    rankedMap.put(player, this.list.get(player));
	}
	return rankedMap;
    }

    /**
     * Amount of players on this table.
     * 
     * @return The amount of players
     */
    public final int amount() {
	return this.list.size();
    }

    /**
     * Fire an {@link TableLogic.Event} to all registered players.
     * 
     * @param event
     *            The {@link TableLogic.Event} to fire
     * @param data
     *            Data associated with this {@link TableLogic.Event}
     */
    final void fireEvent(final Event event, final Object data) {
	for (CardPlayer player : this.list.keySet()) {
	    player.handleEvent(event, data);
	}
    }

    /**
     * @see Player#fireEvent(Event, Object)
     * 
     * @param event
     *            The {@link TableLogic.Event} to fire
     */
    final void fireEvent(final Event event) {
	this.fireEvent(event, null);
    }

    /**
     * Manage players in game-rounds.
     * 
     * @author <a href="mailto:code@jens-bertram.net">Jens Bertram</a>
     * 
     */
    class PlayerIterator implements Iterator<CardPlayer> {
	/** Pointer to current player. */
	private byte pointer = -1;
	/** Pointer to the player who closed the current game. */
	private byte closingPointer = -1;
	/** Restart iteration from the beginning, if we hit the end? */
	private boolean wrapAround = false;
	/** has last next() call wrapped the pointer? */
	private boolean hasWrapped = false;
	/** Get the IPlayer objects as array for easy index handling. */
	private List<CardPlayer> player;
	/** Track, if we have already build the player array. */
	private boolean initialized = false;

	/**
	 * @param doWrapAround
	 *            If true, the iterator should restart at the beginning, if
	 *            hitting the end of the player list
	 */
	protected PlayerIterator(final boolean doWrapAround) {
	    this.wrapAround = doWrapAround;
	}

	/**
	 * Initialize the iterator. This must be called before actually using it
	 */
	private void initialize() {
	    this.player = Collections.unmodifiableList(Player.this.asList());
	    this.initialized = true;
	}

	@Override
	public final boolean hasNext() {
	    if (!this.initialized) {
		this.initialize();
	    }

	    if (this.wrapAround) {
		// this one is never ending
		return true;
	    }

	    if ((this.pointer + 1) < this.player.size()) {
		if ((this.pointer + 1) == this.closingPointer) {
		    return false;
		}
		return true;
	    }
	    return false;
	}

	/**
	 * Set the current player-pointer to a new value.
	 * 
	 * @param newPpointer
	 *            The new index
	 */
	void setPointer(final int newPpointer) {
	    this.pointer = (byte) newPpointer;
	}

	@Override
	public CardPlayer next() {
	    if (!this.initialized) {
		this.initialize();
	    }

	    if (this.hasNext()) {
		this.pointer++;

		if (this.pointer == this.player.size()) {
		    this.hasWrapped = true;
		    this.reset();
		    this.pointer++;
		} else {
		    this.hasWrapped = false;
		}

		CardPlayer nextPlayer = this.get();
		Player.this.finishedTurn = false;
		return nextPlayer;
	    }
	    return null;
	}

	/** Resets the iterator to the beginning of the list. */
	private void reset() {
	    this.pointer = -1;
	}

	@Override
	public void remove() {
	    throw new IllegalStateException("Operation not supported.");
	}

	/**
	 * Index of the current player in the player list.
	 * 
	 * @return The index of the current playing player
	 */
	private byte getIndex() {
	    if (!this.initialized) {
		this.initialize();
	    }
	    if (this.pointer == -1) {
		return 0;
	    }
	    return this.pointer;
	}

	/**
	 * Get the {@link CardPlayer} the iterator is currently pointing at.
	 * 
	 * @return The {@link CardPlayer} the iterator is currently pointing at
	 */
	CardPlayer get() {
	    return this.player.get(this.getIndex());
	}

	/**
	 * Set the current player as the one who closed the game.
	 * 
	 * @return Any second try to close will be signaled by returning false;
	 */
	boolean setAsClosing() {
	    // if (this.closingPointer == -1) {
	    this.closingPointer = this.getIndex();
	    return true;
	    // }
	    // return false;
	}

	/**
	 * @return True, if while getting the current player the iterator has
	 *         wrapped around
	 */
	boolean hasWrapped() {
	    return this.hasWrapped;
	}
    }
}