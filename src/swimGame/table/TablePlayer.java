package swimGame.table;

import java.util.ArrayList;

import swimGame.player.IPlayer;

public class TablePlayer {
	/** List of players joined this table */
	private final ArrayList<IPlayer> players = new ArrayList<IPlayer>();
	private int pointer = -1;

	public void add(IPlayer player) {
		this.players.add(player);
	}

	public void reset() {
		this.pointer = -1;
	}

	public IPlayer get(int idx) {
		return ((idx > -1) && (idx <= this.players.size())) ? this.players
				.get(idx) : null;
	}

	public ArrayList<IPlayer> getList() {
		return (ArrayList<IPlayer>) this.players.clone();
	}

	protected int size() {
		return this.players.size();
	}
}
