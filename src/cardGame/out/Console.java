package cardGame.out;

public enum Console {
    INSTANCE;

    /**
     * Central output function.
     * 
     * @param string
     *            Message to write
     */
    private void write(final String string) {
	System.out.print(string);
    }

    /**
     * Format string printing.
     * 
     * @param string
     *            Format string
     * @param content
     *            Arguments
     */
    public final void printf(final String string, final Object... content) {
	this.write(String.format(string, content));
    }

    /**
     * Simple string printing.
     * 
     * @param string
     *            Message to print
     */
    public final void print(final String string) {
	this.write(string);
    }

}