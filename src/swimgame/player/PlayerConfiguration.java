package swimgame.player;

/**
 * This class holds the default configuration for a player.
 * 
 * @author <a href="mailto:code@jens-bertram.net">Jens Bertram</a>
 * 
 */
public class PlayerConfiguration {
    /** Below witch value should we drop an initial card stack immediately? */
    public static final byte STACKDROP_INITIAL = 0;
    /** When to consider dropping the stack. */
    public static final byte STACKDROP = 1;
    /**
     * drop a card, even if one of three is missing, if it's rating is over..
     */
    public static final byte FORCE_DROP = 2;
    /** Wait for a third card if current rating is below.. */
    public static final byte WAIT_FOR_CARD = 3;

    /** the gene */
    protected final double[] gene = new double[4];

    /** Default Constructor */
    public PlayerConfiguration() {
	// initialize with default values
	this.gene[STACKDROP_INITIAL] = 20;
	this.gene[STACKDROP] = 20;
	this.gene[FORCE_DROP] = 20;
	this.gene[WAIT_FOR_CARD] = 21;
    }

    /** the gene-names to ease the debugging */
    protected static final String[] GENE_NAMES = new String[] {
	    "STACKDROP_INITIAL", "STACKDROP", "FORCE_DROP", "WAIT_FOR_CARD" };

    /**
     * Get a configuration value.
     * 
     * @param value
     *            The index of the value to get
     * @return The value for the given index
     */
    public double get(int value) {
	return this.gene[value];
    }

    public void set(int key, double value) {
	this.gene[key] = value;
    }

    public double[] asArray() {
	return this.gene;
    }
}
