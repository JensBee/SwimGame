package cardGame.games.swimming;

import java.io.IOException;
import java.util.Set;

import cardGame.event.CardGameEvent;
import cardGame.event.EventBus;
import cardGame.event.EventReceiver;
import cardGame.logic.CardGame;
import cardGame.logic.RoundBasedGame;
import cardGame.out.Debug;
import cardGame.player.CardPlayer;
import cardGame.table.GameTable;
import cardGame.table.GeneralGameTable;
import cardGame.util.LoopIterator;

/**
 * Logic for playing the swimming game.
 * 
 * @author <a href="mailto:code@jens-bertram.net">Jens Bertram</a>
 * 
 */
public class GameLogic implements CardGame, RoundBasedGame, EventReceiver {
    /** Number of games to play in turn. */
    private int numberOfGamesToPlay = 1;
    /** The table this game is happening at. */
    private Table table = null;
    /** Maximum number of rounds to play without anybody winning. */
    private int maxRoundsToPlay;

    private boolean roundIsClosed = false;
    private CardPlayer roundClosingPlayer = null;

    /** Events emitted by this class. */
    // TODO: describe parameters
    enum Event implements CardGameEvent {
	/** Next player whose turn it is. */
	NEXTPLAYER;
    }

    /** Empty constructor. This handles the registration for all needed events. */
    GameLogic() {
	// register for table events
	EventBus.INSTANCE.registerEventReceiver(this,
		GeneralGameTable.Event.values());
	EventBus.INSTANCE.registerEventReceiver(this, Table.Event.values());
    }

    @Override
    public final void setTable(final GameTable newTable) {
	this.table = (Table) newTable;
    }

    @Override
    public final void setNumberOfGamesToPlay(final int newNumberOfGamesToPlay) {
	this.numberOfGamesToPlay = newNumberOfGamesToPlay;
    }

    /** Debug: Wait for a keypress. */
    private void debug_keyPress() {
	Debug.println(Debug.Level.SYS, "Press return to continue...");
	try {
	    System.in.read();
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    @Override
    public final int getNumberOfGamesToPlay() {
	return this.numberOfGamesToPlay;
    }

    /** Start the game. */
    public final void start() {
	int currentGame = 1;
	LoopIterator<CardPlayer> startingPlayerIterator =
		this.table.playerLoopIterator();
	LoopIterator<CardPlayer> currentPlayerIterator =
		this.table.playerLoopIterator();
	CardPlayer gameStartingPlayer;
	CardPlayer currentPlayer;
	int gameRound;
	int gameInteraction;

	this.table.close();

	while (currentGame <= this.numberOfGamesToPlay) {
	    // Setup starting player:
	    gameStartingPlayer = startingPlayerIterator.next();
	    currentPlayerIterator.setPosition(gameStartingPlayer);
	    currentPlayer = gameStartingPlayer;

	    Debug.printfn(Debug.Level.INFO, "Game: %d", currentGame);
	    Debug.printfn(Debug.Level.INFO, "StartingPlayer: %s",
		    gameStartingPlayer.getName());

	    Debug.printfn(Debug.Level.INFO, "Game %d - Dealing out cards..",
		    currentGame);
	    // Starting player is first. This will also set the table cards.
	    this.table.dealInitialCards(gameStartingPlayer);
	    for (CardPlayer player : this.table.player()) {
		if (!player.equals(gameStartingPlayer)) {
		    this.table.dealCards(player);
		}
	    }
	    EventBus.INSTANCE.fireEvent(Event.NEXTPLAYER, currentPlayer);

	    Debug.printfn(Debug.Level.INFO, "Game %d - Starting..", currentGame);

	    // Start gaming rounds:
	    gameRound = 1;
	    gameInteraction = 0;
	    this.roundIsClosed = false;
	    this.roundClosingPlayer = null;
	    while (gameRound < this.maxRoundsToPlay) {
		// debug:start
		StringBuffer cardString = new StringBuffer();
		for (cardGame.card.CardDeck.Card card : this.table
			.currentCards()) {
		    cardString.append(card);
		}
		Debug.printfn(Debug.Level.INFO, "Table Cards: %s", cardString);
		// debug:end
		currentPlayer = currentPlayerIterator.next();

		// if round is closed, was current player the closing one?
		if (this.roundIsClosed
			&& (currentPlayer.equals(this.roundClosingPlayer))) {
		    // if so then stop here
		    break;
		}

		EventBus.INSTANCE.fireEvent(Event.NEXTPLAYER, currentPlayer);
		gameInteraction++;
		if (gameInteraction == this.table.numberOfPlayers()) {
		    gameInteraction = 0;
		    gameRound++;
		}
	    }
	    // debug:start
	    Debug.printfn(Debug.Level.INFO,
		    "Game %d of %d finished after %d of %d rounds.",
		    currentGame, this.numberOfGamesToPlay, gameRound,
		    this.maxRoundsToPlay);
	    this.debug_keyPress();
	    // debug:end

	    this.table.startNewGame();
	    currentGame++;
	}
    }

    @Override
    public final void setMaxRoundsToPlay(final int newMaxRoundsToPlay) {
	this.maxRoundsToPlay = newMaxRoundsToPlay;
    }

    @Override
    public final int getMaxRoundsToPlay() {
	return this.maxRoundsToPlay;
    }

    @SuppressWarnings("unchecked")
    @Override
    public final void handleEvent(final CardGameEvent event, final Object data) {
	Class<? extends CardGameEvent> eventClass = event.getClass();
	if (eventClass.equals(GeneralGameTable.Event.class)) {
	    switch ((GeneralGameTable.Event) event) {
	    case CLOSED:
		Debug.println(Debug.Level.INFO, "Table is now closed.");
		break;
	    case PLAYER_JOINED:
		Debug.printfn(Debug.Level.INFO,
			"Player '%s' joined the table.", data);
		break;
	    case PLAYER_LEFT:
		Debug.printfn(Debug.Level.INFO, "Player '%s' left the table.",
			data);
		break;
	    default:
		break;
	    }
	} else if (eventClass.equals(Table.Event.class)) {
	    switch ((Table.Event) event) {
	    case CARDS:
		StringBuffer cardString = new StringBuffer();
		for (cardGame.card.CardDeck.Card card : (Set<cardGame.card.CardDeck.Card>) data) {
		    cardString.append(card);
		}
		Debug.printfn(Debug.Level.INFO, "Table cards: %s", cardString);
		break;
	    case CLOSE_CALL:
		this.roundIsClosed = true;
		this.roundClosingPlayer = (CardPlayer) data;
		break;
	    default:
		break;
	    }
	}
    }
}
