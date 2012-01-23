package swimgame.testing.behavior;

import java.util.HashMap;
import java.util.Map;

import swimgame.Util;
import swimgame.out.Debug;

/**
 * Manages the genes used to create new players. This also handles the rating of
 * each gene.
 * 
 * @author <a href="mailto:code@jens-bertram.net">Jens Bertram</a>
 * 
 */
public class GenePool {
    private static final String CLASSNAME = "GenePool";
    /** Stores the genes and their ratings */
    private final Map<PlayerGene, Integer> genePool;

    /**
     * Constructor
     * 
     * @param size
     *            The size of this gene pool
     */
    GenePool(final int size) {
	Debug.printf(Debug.INFO, this.getClass(),
		"Initializing pool with %d genes.. ", size);
	this.genePool = new HashMap<PlayerGene, Integer>(size);
	this.initializePool(size);
	Debug.printf(Debug.INFO, this.getClass(),
		"Done initializing pool with %d genes.\n", size);
    }

    private void initializePool(int poolSize) {
	PlayerGene gene;
	// first add the current player default gene
	this.genePool.put(new PlayerGene(true), 0);
	// then add the random ones
	for (int i = 0; i < (poolSize); i++) {
	    gene = new PlayerGene();
	    Debug.printf(Debug.INFO, this.getClass(), "Gene:\n %s", gene);
	    this.genePool.put(gene, 0);
	}
    }

    public PlayerGene getRandomGene() {
	int geneIndex = Util.getRandomInt(this.genePool.size() - 1);
	int i = 0;
	PlayerGene gene = null;
	for (PlayerGene g : this.genePool.keySet()) {
	    if (i == geneIndex) {
		gene = g;
		break;
	    }
	    i++;
	}
	return gene;
    }
}
