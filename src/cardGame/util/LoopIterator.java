package cardGame.util;

import java.util.Iterator;
import java.util.List;

/**
 * A simple looping {@link Iterator} implementation.
 * 
 * @param <T>
 */
public class LoopIterator<T> implements Iterator<T> {
    /** Current Iterator position. */
    private int index = 0;
    /** List to iterate. */
    private final List<T> list;

    /**
     * Constructor.
     * 
     * @param newList
     *            List to iterate
     */
    public LoopIterator(final List<T> newList) {
	this.list = newList;
    }

    @Override
    public final boolean hasNext() {
	// always true, because we're looping
	if (this.list.size() > 0) {
	    return true;
	}
	return false;
    }

    @Override
    public final T next() {
	this.index++;
	if (this.index >= this.list.size()) {
	    this.index = 0;
	}
	return this.list.get(this.index);
    }

    @Override
    public final void remove() {
	throw new UnsupportedOperationException();
    }

    /**
     * Set the iterator index to the given Object.
     * 
     * @param object
     *            Object to witch the Iterator should be set
     */
    public final void setPosition(final T object) {
	final int newIndex = this.list.indexOf(object);
	if (newIndex < 0) {
	    throw new IllegalArgumentException("Object not in the list.");
	}
	this.setPosition(newIndex);
    }

    /**
     * Set the iterator to the given index.
     * 
     * @param newIndex
     *            New Iterator position
     */
    public final void setPosition(final int newIndex) {
	if ((newIndex > this.list.size()) || (newIndex < 0)) {
	    throw new ArrayIndexOutOfBoundsException(String.format(
		    "Index out of bounds (%d). Size %d", newIndex,
		    this.list.size()));
	}
	this.index = newIndex;
    }
}
