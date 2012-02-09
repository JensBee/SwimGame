package cardGame.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A basic event bus that allows receivers to register for defined events.
 * Removing of receivers is not implemented as it's simply currently not needed.
 * 
 * @author <a href="mailto:code@jens-bertram.net">Jens Bertram</a>
 * 
 */
public enum EventBus {
    /** EventBus singleton instance. */
    INSTANCE;

    /** Map storing the all receivers attached to specific events. */
    private final Map<CardGameEvent, List<EventReceiver>> events =
	    new HashMap<CardGameEvent, List<EventReceiver>>();

    /**
     * Register an receiver for the specified events.
     * 
     * @param receiver
     *            Receiver handling the events
     * @param registerEvents
     *            The events the receiver wants to handle
     */
    public void registerEventReceiver(final EventReceiver receiver,
	    final CardGameEvent... registerEvents) {
	for (CardGameEvent event : registerEvents) {
	    if (this.events.containsKey(event)) {
		this.events.get(event).add(receiver);
	    } else {
		List<EventReceiver> eventList = new ArrayList<EventReceiver>();
		eventList.add(receiver);
		this.events.put(event, eventList);
	    }
	}
    }

    /**
     * Fire an event to all registered receivers.
     * 
     * @param event
     *            Event to witch the receivers have registered
     * @param data
     *            Data associated with the event
     */
    public void fireEvent(final CardGameEvent event, final Object data) {
	// only try to call if anyone has registered for the event
	if (this.events.containsKey(event)) {
	    for (EventReceiver receiver : this.events.get(event)) {
		receiver.handleEvent(event, data);
	    }
	}
    }
}
