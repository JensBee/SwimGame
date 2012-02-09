package cardGame.event;

/** Simple <code>EventBus</code> <code>Event</code> receiver. */
public interface EventReceiver {
    /**
     * Handle an event.
     * 
     * @param event
     *            {@link CardGameEvent} emitted
     * @param data
     *            Data associated with the event
     */
    void handleEvent(final CardGameEvent event, final Object data);
}
