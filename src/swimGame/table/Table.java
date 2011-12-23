package swimGame.table;

import java.util.ArrayList;

import swimGame.cards.CardStack;
import swimGame.out.Console;
import swimGame.player.Player;

public class Table {
	/** List of players joined this table */
	private final ArrayList<Player> players = new ArrayList<Player>();
	// Table will be closed, if full or the game has begun
	private boolean tableClosed = false;
	/** Card stack owned by this table (stack will be full, i.e. has all cards) */
	private final CardStack cardStack = new CardStack(true);
	// name for console out
	public static final String CNAME = "Table";
	// the current player
	private int currentPlayer = 0;
	// the current round
	private int currentRound = 1;

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

	/**
	 * Add a player to the table.
	 * 
	 * @param player
	 */
	public void addPlayer(final Player player) throws Exception {
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
			this.addPlayer(new Player());
		}
	}

	/**
	 * Close player participation opportunity and prepare game start.
	 */
	public void close() {
		if (this.tableClosed != true) {
			this.tableClosed = true;
			this.currentPlayer = 0;
			String playerNames = "";
			for (final Player player : this.players) {
				playerNames += player.toString() + ", ";
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

	private void nextPlayer() {
		this.currentPlayer++;
		Console.println(Table.CNAME,
				"It's your turn " + this.players.get(this.currentPlayer));
	}

	/**
	 * Deliver cards to players
	 */
	private void dealOutCards() throws Exception {
		// player who starts the game
		final Player firstPlayer = this.players.get(this.currentPlayer);
		this.close(); // game starts now
		Console.println(Table.CNAME, "Dealing out cards..");
		for (final Player player : this.players) {
			// pass initial cards to player
			player.setCards(this.getPlayerCardSet());
		}

		Console.println(Table.CNAME, this.players.get(this.currentPlayer)
				+ " begins!");

		// deal out a second set of cards for the first player, if he want so
		if (firstPlayer.keepCardSet() == false) {
			Console.println(Table.CNAME, firstPlayer
					+ " drops his initial card-set.");
			Console.println(Table.CNAME, "Dealing out a second cardset for "
					+ firstPlayer + "..");
			firstPlayer.setCards(this.getPlayerCardSet());
		}
	}
}
