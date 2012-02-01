package swimgame.testing.behavior;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import swimgame.player.DefaultPlayer;
import swimgame.player.rating.Card;
import swimgame.util.Util;

/**
 * Gene for a player.
 * 
 * @author <a href="mailto:code@jens-bertram.net">Jens Bertram</a>
 * 
 */
public class PlayerGene {
    /** Stores all possible values a card-set rating may archive */
    private static final ArrayList<Double> PLAYER_BIAS_VALUES = new ArrayList<Double>(
	    Arrays.asList(//
		    new Double(24), // 7,8,9
		    new Double(27), // 7,10,10
		    new Double(28), // 8,10,10 7,10,11
		    new Double(29), // 8,10,11
		    new Double(30), // 9,10,11
		    new Double(30.5), // three of a type
		    new Double(31))); // 10,10,11
    /** Storage for generated player bias values. */
    private final Map<DefaultPlayer.Bias, Double> playerBias = new HashMap<DefaultPlayer.Bias, Double>(
	    DefaultPlayer.Bias.values().length);
    /** Storage for generated player card bias values. */
    private final Map<Card.Bias, Double> playerCardBias = new HashMap<Card.Bias, Double>(
	    Card.Bias.values().length);

    /** Empty Constructor. This will create a new random gene. */
    PlayerGene() {
	this.createRandomGene();
    }

    /**
     * Constructor that allows to skip the initialization with random gene
     * values.
     * 
     * @param noInitialize
     *            If true the gene will not be initialized with random values.
     *            This means it will be loaded with the default set of genes.
     */
    PlayerGene(boolean noInitialize) {
	super();
	if (!noInitialize) {
	    this.createRandomGene();
	}
    }

    /** Create a random gene */
    private void createRandomGene() {
	// card set rating dependent values
	final int maxValue = PLAYER_BIAS_VALUES.size() - 1;
	for (DefaultPlayer.Bias bias : DefaultPlayer.Bias.values()) {
	    this.playerBias.put(bias,
		    PLAYER_BIAS_VALUES.get(Util.getRandomInt(maxValue)));
	}

	// card rating biases
	for (Card.Bias bias : Card.Bias.values()) {
	    this.playerCardBias.put(bias, Math.random());
	}
    }

    @Override
    public String toString() {
	StringBuffer out = new StringBuffer();
	out.append("\t---Player:\n");
	for (DefaultPlayer.Bias bias : this.playerBias.keySet()) {
	    out.append(String.format("\t%s: %.1f\n", bias,
		    this.playerBias.get(bias)));
	}
	out.append("\t---Cards:\n");
	for (Card.Bias bias : this.playerCardBias.keySet()) {
	    out.append(String.format("\t%s: %.1f\n", bias,
		    this.playerCardBias.get(bias)));
	}
	return out.toString();
    }

    public Map<DefaultPlayer.Bias, Double> getPlayerBias() {
	return this.playerBias;
    }

    public Map<Card.Bias, Double> getPlayerCardBias() {
	return this.playerCardBias;
    }
}
