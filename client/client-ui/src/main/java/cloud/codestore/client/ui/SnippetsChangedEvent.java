package cloud.codestore.client.ui;

import javafx.event.Event;
import javafx.event.EventType;

import javax.annotation.Nonnull;

/**
 * An event that indicates that a change has been made to the available code snippets.
 * This event is a confirmation of a preceded {@link ChangeSnippetsEvent}.
 */
public class SnippetsChangedEvent extends Event {
    static final EventType<SnippetsChangedEvent> ANY = new EventType<>(Event.ANY, "ANY_SNIPPET_CHANGE");

    /**
     * A new code snippet has been created.
     */
    public static final EventType<SnippetsChangedEvent> SNIPPET_CREATED = new EventType<>(ANY, "SNIPPET_CREATED");

    /**
     * A code snippet has been updated.
     */
    public static final EventType<SnippetsChangedEvent> SNIPPET_UPDATED = new EventType<>(ANY, "SNIPPET_UPDATED");

    /**
     * A code snippet has been deleted.
     */
    public static final EventType<SnippetsChangedEvent> SNIPPET_DELETED = new EventType<>(ANY, "SNIPPET_DELETED");

    /**
     * @param eventType an event type.
     */
    public SnippetsChangedEvent(@Nonnull EventType<? extends Event> eventType) {
        super(eventType);
    }
}
