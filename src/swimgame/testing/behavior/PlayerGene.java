package swimgame.testing.behavior;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import swimgame.player.DefaultPlayer;
import swimgame.util.Util;

/**
 * Gene for a player.
 * 
 * @author <a href="mailto:code@jens-bertram.net">Jens Bertram</a>
 * 
 */
public class PlayerGene {
    /** Stores all possible values a card-set rating may archive */
    private static final ArrayList<Double> CARD_RATING_VALUES = new ArrayList<Double>(
	    Arrays.asList(//
		    new Double(24), // 7,8,9
		    new Double(27), // 7,10,10
		    new Double(28), // 8,10,10 7,10,11
		    new Double(29), // 8,10,11
		    new Double(30), // 9,10,11
		    new Double(30.5), // three of a type
		    new Double(31))); // 10,10,11
    /** Storage for generated bias values. */
    private final Map<DefaultPlayer.Bias, Double> gene = new HashMap<DefaultPlayer.Bias, Double>(
	    DefaultPlayer.Bias.values().length);

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
	final int maxValue = CARD_RATING_VALUES.size() - 1;

	for (DefaultPlayer.Bias bias : DefaultPlayer.Bias.values()) {
	    this.gene.put(bias,
		    CARD_RATING_VALUES.get(Util.getRandomInt(maxValue)));
	}
    }

    @Override
    public String toString() {
	StringBuffer out = new StringBuffer();
	for (DefaultPlayer.Bias bias : this.gene.keySet()) {
	    out.append(String.format("\t%s: %.1f\n", bias, this.gene.get(bias)));
	}
	return out.toString();
    }

    /**
     * Get the gene array
     * 
     * @return The gene Array
     */
    public Map<DefaultPlayer.Bias, Double> getBias() {
	return this.gene;
    }
}
