package swimGame.table;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.Iterator;

import swimGame.out.Console;
import swimGame.out.Debug;
import swimGame.player.DefaultPlayer;
import swimGame.player.IPlayer;

public class Table {
	// Table will be closed, if full or the game has begun
	private boolean tableClosed = false;
	/** Card stack owned by this table (stack will be full, i.e. has all cards) */
	private final CardStack cardStack = new CardStack(true);
	// cards on the table
	private final CardStack cardStackTable = new CardStack(false);
	// name for console out
	private static final String CNAME = "Table";
	// the current round
	private int currentRound = 1;
	// round handling
	private boolean gameFinished = false;
	// Tracks if a player has finished his current move
	private boolean playerMoveFinished = false;
	// after how many rounds should the game be interrupted? (to catch
	// non-ending games)
	public static int MAX_ROUNDS = 32;
	// player who closed the game
	private IPlayer playerClosedTheGame;

	// nested classes
	private final Players player;
	private final LogWriter logWriter = new LogWriter();

	// points for three cards of same type, but different color
	public static final double WORTH_THREE_OF_SAME_TYPE = 30.5;

	/** Events fired to the players wile in-game */
	public enum Event {
		TABLE_CLOSED,
		// Table is closed, game will start shortly
		GAME_START,
		// Game is starting now
		INITIAL_CARDSTACK_DROPPED,
		// A player dropped his initial card set, to get another one
		// Data: int[6] array with three cards 3*(card,color)
		GAME_CLOSED,
		// A player called for closing the game. The last round is running now
		GAME_FINISHED,
		// Game has finished
		CARD_DROPPED,
		// A card has been dropped by a user
		// Data: (byte) card
		;
	}

	/** Player actions to interact with the table */
	public enum Action {
		DROP_CARD,
		// Player drops a card to the table
		// Data: (byte) card
		DROP_CARDSTACK_INITIAL,
		// Player drops his initial card set, to get another one
		MOVE_FINISHED,
		// Player has finished his move
		END_CALL,
		// Player called to end this round
		PICK_CARD,
		// Player wants to pick a card from the table
		// Data: (byte) card
	}

	/**
	 * Handles player on this table
	 * 
	 * @author Jens Bertram <code@jens-bertram.net>
	 * 
	 */
	private class Players {
		/** List of players joined this table */
		private final ArrayList<IPlayer> list = new ArrayList<IPlayer>();
		protected final PlayerIterator iterator = new PlayerIterator();

		protected void add(IPlayer player) {
			this.list.add(player);
		}

		protected IPlayer get(int index) {
			return ((index > -1) && (index <= this.list.size())) ? this.list
					.get(index) : null;
		}

		protected int size() {
			return this.list.size();
		}

		/**
		 * Manage players in game-rounds
		 * 
		 * @author Jens Bertram <code@jens-bertram.net>
		 * 
		 */
		private class PlayerIterator implements Iterator<IPlayer> {
			private int pointer = -1;
			private int closingPointer = -1;

			@Override
			public boolean hasNext() {
				if ((this.pointer + 1) < Table.this.player.size()) {
					if ((this.pointer + 1) == this.closingPointer) {
						return false;
					}
					return true;
				}
				return false;
			}

			protected boolean nextIsClosing() {
				if ((this.pointer + 1) == this.closingPointer) {
					return true;
				}
				return false;
			}

			@Override
			public IPlayer next() {
				if (this.hasNext()) {
					this.pointer++;
					IPlayer player = this.get();
					Console.println(Table.CNAME,
							String.format("It's your turn %s", player));
					Table.this.playerMoveFinished = false;
					return player;
				}
				return null;
			}

			protected void reset() {
				this.pointer = -1;
			}

			@Override
			public void remove() throws IllegalStateException {
				throw new IllegalStateException("Operation not supported.");
			}

			protected int getId() {
				return (this.pointer == -1) ? 0 : this.pointer;
			}

			protected IPlayer get() {
				return Table.this.player.get(this.getId());
			}

			/**
			 * Set the current player as the one who closed the game.
			 * 
			 * @return Any second try to close will be signaled by returning
			 *         false;
			 */
			protected boolean setAsClosing() {
				if (this.closingPointer == -1) {
					this.closingPointer = this.getId();
					return true;
				}
				return false;
			}
		}
	}

	/**
	 * Ease handling of lag entries
	 * 
	 * @author Jens Bertram <code@jens-bertram.net>
	 * 
	 */
	private class LogWriter {
		private String getPlayerName(IPlayer player) {
			return String.format("<%s> ", player.toString());
		}

		private String getPlayerName() {
			return this.getPlayerName(Table.this.player.iterator.get());
		}

		protected void player(String format, Object... args) {
			Console.println(Table.CNAME, this.getPlayerName()
					+ (new Formatter().format(format, args).toString()));
		}

		protected void player(String message) {
			this.player(message, (Object[]) null);
		}

		protected void player(IPlayer player, String format, Object... args) {
			Console.println(Table.CNAME, this.getPlayerName(player)
					+ (new Formatter().format(format, args).toString()));
		}

		protected void player(IPlayer player, String message) {
			this.player(player, message, (Object[]) null);
		}

		protected void write(String format, Object... args) {
			Console.println(Table.CNAME, new Formatter().format(format, args)
					.toString());
		}

		protected void write(String message) {
			this.write(message, (Object[]) null);
		}
	}

	private static class Cards {
		protected static boolean verifyGoal(byte[] cards) {
			CardStack userCardStack;
			int cardsCount;

			try {
				userCardStack = new CardStack(cards);
			} catch (Exception e) {
				return false;
			}

			// three of a color?
			cardsCount = 0;
			for (byte card : userCardStack.getCardsByColor(userCardStack.card
					.getColor(cards[0]))) {
				if (userCardStack.hasCard(card)) {
					cardsCount++;
				}
			}
			if (cardsCount == 3) {
				Debug.println(Table.class, "Three of a color!");
				return true;
			}

			// three of a type?
			cardsCount = 0;
			for (byte card : userCardStack.getCardsByType(userCardStack.card
					.getType(cards[0]))) {
				if (userCardStack.hasCard(card)) {
					cardsCount++;
				}
			}
			if (cardsCount == 3) {
				Debug.println(Table.class, "Three of a type!");
				return true;
			}

			return false;
		}
	}

	public Table() {
		this.player = new Players();
	}

	/**
	 * Add a single player to the table
	 * 
	 * @param player
	 * @throws Exception
	 *             Thrown if table is full or game has already begun
	 */
	public void addPlayer(final IPlayer player) throws Exception {
		// check if table is already full
		if ((this.tableClosed == true) || (this.player.size() == 9)) {
			this.close(); // just to be sure
			throw new Exception("Table is closed! No more player allowed.");
		}

		this.player.add(player);
		this.logWriter.player(player, "Joined the table");
	}

	/**
	 * Batch add a bunch of players to the game
	 * 
	 * @param amount
	 *            The number of players to add (max 9)
	 */
	public void addPlayers(final int amount) throws Exception {
		for (int i = 0; i < amount; i++) {
			this.addPlayer(new DefaultPlayer());
		}
	}

	/**
	 * Close player participation opportunity and prepare game start.
	 */
	private void close() {
		if (this.tableClosed != true) {
			this.fireEvent(Event.TABLE_CLOSED, null);
			this.tableClosed = true;
			this.player.iterator.reset();
			String playerNames = "";
			for (IPlayer p : this.player.list) {
				playerNames += p.toString() + ", ";
			}
			Console.nl();
			this.logWriter.write("*** Table closed ***");
			if (Debug.debug == true) {
				Debug.println(this.getClass(), String.format(
						"Players: %d  Maximum rounds: %d", this.player.size(),
						Table.MAX_ROUNDS));
			}
			this.logWriter.write(String.format("Players (in order) are: %s",
					playerNames.subSequence(0, playerNames.length() - 2)));

			if (Console.ask) {
				System.out.println("\nPress return to start...");
				try {
					System.in.read();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Get the game started
	 */
	public void startGame() throws Exception {
		if (this.player.size() <= 1) {
			throw new Exception("No game without players!");
		}

		// no more players allowed
		this.close();
		// notify players
		this.fireEvent(Event.GAME_START, null);
		// deal cards
		this.dealOutCards();

		// run tings
		this.currentRound = 0;
		while ((this.gameFinished == false)
				&& (this.currentRound < Table.MAX_ROUNDS)
				&& this.player.iterator.hasNext()) {
			this.nextRound();

			while (this.player.iterator.hasNext()) {
				this.logWriter
						.write("Cards: " + this.cardStackTable.toString());

				this.player.iterator.next().doMove(
						this.cardStackTable.getCards());
				while (this.playerMoveFinished != true) {
				}
			}

			if (this.player.iterator.nextIsClosing()) {
				this.gameFinished = true;
				break;
			}

			this.player.iterator.reset();
		}

		this.fireEvent(Event.GAME_FINISHED);
		if (this.currentRound == Table.MAX_ROUNDS) {
			this.logWriter
					.write("Sorry players, I'll stop here! You reached the maximum of %d rounds without anyone winning.",
							Table.MAX_ROUNDS);
		} else {
			this.generateRating();
		}
	}

	/** Generate a simple rating for all players */
	private void generateRating() {
		Console.nl();
		this.logWriter.write("Player-rating:");
		for (IPlayer player : this.player.list) {
			byte[] playerCards = player.getCards();
			String playerName = (player == this.playerClosedTheGame) ? "* "
					+ player.toString() : player.toString();
			if (playerCards == null) {
				this.logWriter.write(" %s gave us no card information",
						playerName);
			} else {
				CardStack playerCardStack = new CardStack(playerCards);
				this.logWriter.write(" %s's cards: %s value: %f", playerName,
						playerCardStack.toString(), playerCardStack.getValue());
			}
		}
	}

	/**
	 * Generate a random card set for a player
	 * 
	 * @return The randomly generated card set
	 * @throws Exception
	 */
	private byte[] getPlayerCardSet() throws Exception {
		return new byte[] { this.cardStack.card.getRandom(),
				this.cardStack.card.getRandom(),
				this.cardStack.card.getRandom() };
	}

	/**
	 * Prepare for the next round
	 */
	private void nextRound() {
		this.currentRound++;
		Console.nl();
		this.logWriter.write("*** Round %d ***", this.currentRound);
	}

	public boolean interact(Action action) {
		return this.interact(action, null);
	}

	public boolean interact(Action action, Object data) {
		switch (action) {
		case DROP_CARDSTACK_INITIAL:
			return true;
		case DROP_CARD:
			CardStack.checkCard((byte) data);
			this.logWriter.player("dropped card %s",
					this.cardStack.card.toString((byte) data));
			this.fireEvent(Event.CARD_DROPPED, data);
			// TODO: tell why it failed, if it does
			return this.cardStackTable.card.add((byte) data);
		case END_CALL:
			byte[] userCards = (byte[]) data;
			if (Cards.verifyGoal(userCards)
					&& this.player.iterator.setAsClosing()) {
				this.playerClosedTheGame = this.player.iterator.get();
				this.logWriter.player("is closing. Last call!");
				this.fireEvent(Event.GAME_CLOSED);
				return true;
			}
			return false;
		case MOVE_FINISHED:
			this.playerMoveFinished = true;
			return true;
		case PICK_CARD:
			this.logWriter.player("picked card %s",
					this.cardStack.card.toString((byte) data));
			// TODO: tell why it failed, if it does
			return this.cardStackTable.card.remove((byte) data);
		}
		return false;
	}

	/**
	 * Deliver cards to players
	 */
	private void dealOutCards() throws Exception {
		byte[] firstCards = new byte[3];
		// player who starts the game
		final IPlayer firstPlayer = this.player.get(this.player.iterator
				.getId());
		this.close(); // game starts now
		this.logWriter.write("Dealing out cards..");
		for (IPlayer p : this.player.list) {
			if (p == firstPlayer) {
				firstCards = this.getPlayerCardSet();
				p.setCards(firstCards);
			} else {
				// pass initial cards to player
				p.setCards(this.getPlayerCardSet());
			}
		}

		this.logWriter.player("begins!");

		// deal out a second set of cards for the first player, if he want so
		if (firstPlayer.keepCardSet() == false) {
			// player is about to drop his cards - make them public
			this.logWriter.player("drops the initial card set: %s",
					new CardStack(firstCards).toString());
			this.fireEvent(Event.INITIAL_CARDSTACK_DROPPED, firstCards);

			// update cards on table
			this.cardStackTable.card.add(firstCards);

			// deal out second card set
			this.logWriter.write("Dealing out a second card set for %s..",
					firstPlayer);
			firstPlayer.setCards(this.getPlayerCardSet());
		} else {
			// player took first cards - make a second public
			byte cards[] = this.getPlayerCardSet();
			this.fireEvent(Event.INITIAL_CARDSTACK_DROPPED, cards);
			// update cards on table
			this.cardStackTable.card.add(cards);
		}
	}

	/**
	 * Fire an event to all registered players.
	 * 
	 * TODO: data needs to be generalized
	 * 
	 * @param event
	 *            The event
	 * @param data
	 *            Some sort of data associated with an event
	 */
	private void fireEvent(Event event, Object data) {
		for (IPlayer p : this.player.list) {
			p.handleTableEvent(event, data);
		}
	}

	private void fireEvent(Event event) {
		this.fireEvent(event, null);
	}
}
