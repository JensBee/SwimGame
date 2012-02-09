package cardGame.logic;

/** Table interface for round based games. */
public interface RoundBasedGame {
    /**
     * Set the maximum amount of rounds to play. This will prevent an never
     * ending game. The table should stop the game if {@code maxRoundsToPlay} is
     * reached without anybody winning.
     * 
     * @param maxRoundsToPlay
     *            Maximum amount of a rounds without anyone winning
     */
    void setMaxRoundsToPlay(final int maxRoundsToPlay);

    /**
     * Get the maximum number of rounds that will be played.
     * 
     * @return Maximum amount of a rounds without anyone winning
     */
    int getMaxRoundsToPlay();
}
