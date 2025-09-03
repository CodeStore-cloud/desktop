package cloud.codestore.client.ui;

import javafx.event.Event;
import javafx.event.EventType;

import javax.annotation.Nonnull;

/**
 * An event that requests changes to the available code snippets.
 */
public class ChangeSnippetsEvent extends Event {
    /**
     * A new code snippet should be created.
     */
    public static final EventType<ChangeSnippetsEvent> CREATE_SNIPPET = new EventType<>(ANY, "CREATE_SNIPPET");

    /**
     * A code snippet should be updated.
     */
    public static final EventType<ChangeSnippetsEvent> UPDATE_SNIPPET = new EventType<>(ANY, "UPDATE_SNIPPET");

    /**
     * A code snippet should be deleted.
     */
    public static final EventType<ChangeSnippetsEvent> DELETE_SNIPPET = new EventType<>(ANY, "DELETE_SNIPPET");

    /**
     * @param eventType an event type.
     */
    public ChangeSnippetsEvent(@Nonnull EventType<? extends Event> eventType) {
        super(eventType);
    }
}
