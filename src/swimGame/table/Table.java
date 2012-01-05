package swimGame.table;

import java.util.Iterator;

import swimGame.cards.CardStack;
import swimGame.out.Console;
import swimGame.player.DefaultPlayer;
import swimGame.player.IPlayer;

public class Table {
	// Table will be closed, if full or the game has begun
	private boolean tableClosed = false;
	/** Card stack owned by this table (stack will be full, i.e. has all cards) */
	private final CardStack cardStack = new CardStack(true);
	// name for console out
	public static final String CNAME = "Table";
	// the current player
	// private int currentPlayer = 0;
	// the current round
	private int currentRound = 1;
	// the players
	private final TablePlayer players = new TablePlayer();
	private final PlayerIterator playerIt = new PlayerIterator();
	// round handling
	private boolean gameFinished = false;

	// points for three cards of same type, but different color
	public static final double PNT_THREE_OF_SAME_TYPE = 30.5;

	// the game is about to start
	public static final byte EVENT_GAME_START = 0;
	// three cards got dropped by the starting player
	public static final byte EVENT_CARDS_DROPPED = 1;
	// a card was dropped by a player
	public static final byte EVENT_CARD_DROPPED = 2;
	// next player is about to play
	public static final byte EVENT_NEXT_PLAYER = 3;
	// next round is about to start
	public static final byte EVENT_NEXT_ROUND = 4;

	enum Event {
		TABLE_CLOSED(0),
		// Data: null
		GAME_START(1),
		// Data: null
		INITIAL_CARDS_DROPPED(2)
		// Data: int[6] array with three cards 3*(card,color)
		;

		public final int id;

		Event(int id) {
			this.id = id;
		}
	}

	private class PlayerIterator implements Iterator {
		private int pointer = -1;

		@Override
		public boolean hasNext() {
			if ((this.pointer + 1) < Table.this.players.size()) {
				return true;
			}
			// if (Table.this.gameFinished == false) {
			// this.pointer = -1;
			// return true;
			// }
			return false;
		}

		@Override
		public IPlayer next() {
			if (this.hasNext()) {
				this.pointer++;
				IPlayer player = Table.this.players.get(this.pointer);
				Console.println(Table.CNAME, "It's your turn " + player);
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
	}

	/**
	 * Add a player to the table.
	 * 
	 * @param player
	 */
	public void addPlayer(final IPlayer player) throws Exception {
		// check if table is already full
		if ((this.tableClosed == true) || (this.players.size() == 9)) {
			this.close(); // just to be sure
			throw new Exception("Table is closed! No more player allowed.");
		}

		this.players.add(player);
		Console.println(Table.CNAME, '"' + player.toString()
				+ "\" joined the table");
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
	public void close() {
		if (this.tableClosed != true) {
			this.fireEvent(Event.TABLE_CLOSED, null);
			this.tableClosed = true;
			this.playerIt.reset();
			String playerNames = "";
			for (IPlayer p : this.players.getList()) {
				playerNames += p.toString() + ", ";
			}
			Console.println(Table.CNAME, "*** Table closed ***");
			Console.println(Table.CNAME, "Players (in order) are: "
					+ playerNames.subSequence(0, playerNames.length() - 2));
		}
	}

	/**
	 * final Start the game
	 */
	public void startGame() throws Exception {
		if (this.players.size() <= 1) {
			throw new Exception("No game without players!");
		}
		this.close();
		this.fireEvent(Event.GAME_START, null);
		this.dealOutCards();
	}

	/**
	 * Get a set of initial player cards
	 * 
	 * @return
	 */
	private int[] getPlayerCardSet() throws Exception {
		final int cardSet[] = new int[6];
		int card[];
		// first card
		card = this.cardStack.getRandomCard();
		cardSet[0] = card[0]; // card
		cardSet[1] = card[1]; // color
		// second card
		card = this.cardStack.getRandomCard();
		cardSet[2] = card[0]; // card
		cardSet[3] = card[1]; // color
		// second card
		card = this.cardStack.getRandomCard();
		cardSet[4] = card[0]; // card
		cardSet[5] = card[1]; // color
		return cardSet;
	}

	/**
	 * Prepare the next round
	 */
	private void nextRound() {
		this.currentRound++;
		Console.println(Table.CNAME, "-- Round " + this.currentRound + " --");
	}

	private void runGame() {
		this.currentRound = 0;
		while (this.gameFinished == false) {
			this.nextRound();

			while (this.playerIt.hasNext()) {
				this.playerIt.next().doMove();
			}
			this.playerIt.reset();

			// TODO remove testing restriction
			if (this.currentRound == 3) {
				this.gameFinished = true;
			}
		}
	}

	/**
	 * Deliver cards to players
	 */
	private void dealOutCards() throws Exception {
		int[] firstCards = new int[6];
		// player who starts the game
		final IPlayer firstPlayer = this.players.get(this.playerIt.getId());
		this.close(); // game starts now
		Console.println(Table.CNAME, "Dealing out cards..");
		for (IPlayer p : this.players.getList()) {
			if (p == firstPlayer) {
				firstCards = this.getPlayerCardSet();
				p.setCards(firstCards);
			} else {
				// pass initial cards to player
				p.setCards(this.getPlayerCardSet());
			}
		}

		Console.println(Table.CNAME, this.players.get(this.playerIt.getId())
				+ " begins!");

		// deal out a second set of cards for the first player, if he want so
		if (firstPlayer.keepCardSet() == false) {
			Console.println(Table.CNAME, firstPlayer
					+ " drops the initial card-set: "
					+ new CardStack(firstCards).toString());
			this.fireEvent(Event.INITIAL_CARDS_DROPPED, firstCards);
			Console.println(Table.CNAME, "Dealing out a second cardset for "
					+ firstPlayer + "..");
			firstPlayer.setCards(this.getPlayerCardSet());
		}
		this.runGame();
	}

	private void fireEvent(Event event, Object data) {
		for (IPlayer p : this.players.getList()) {
			p.handleTableEvent(event.id);
		}
	}
}
