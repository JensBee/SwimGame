package swimgame.testing.behavior;

import swimgame.out.Debug;
import swimgame.player.DefaultPlayer;
import swimgame.player.PlayerConfiguration;
import swimgame.table.logic.TableLogic;

public class MutablePlayer extends DefaultPlayer {
    /**
     * Constructor.
     * 
     * @param tableLogic
     *            The logic for this player
     * @param name
     *            The name of this player
     */
    public MutablePlayer(final TableLogic tableLogic, final String name) {
	super(tableLogic, name);
    }

    /**
     * Constructor without given player name
     * 
     * @param tableLogic
     *            The logic for this player
     */
    public MutablePlayer(final TableLogic tableLogic) {
	super(tableLogic);
    }

    /** Helper function to ease the setting of the player configuration values */
    private void setGeneValue(int key, double[] geneValues) {
	this.behavior.set(key, geneValues[key]);
    }

    /**
     * Set a complete gene for this player
     * 
     * @param gene
     *            The gene to set
     */
    public void setGene(PlayerGene gene) {
	Debug.println(Debug.INFO, this.getClass(), "Recieved genes");
	double[] geneValues = gene.getGene();
	this.setGeneValue(PlayerConfiguration.FORCE_DROP, geneValues);
	this.setGeneValue(PlayerConfiguration.WAIT_FOR_CARD, geneValues);
	this.setGeneValue(PlayerConfiguration.STACKDROP_INITIAL, geneValues);
	this.setGeneValue(PlayerConfiguration.STACKDROP, geneValues);
    }
}
