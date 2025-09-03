package cloud.codestore.client.ui.snippet.footer;

import javafx.event.Event;
import javafx.event.EventType;

import javax.annotation.Nonnull;

/**
 * An event for saving and canceling the current action.
 */
public class ControlEvent extends Event {

    public static final EventType<ControlEvent> SAVE = new EventType<>(ANY, "SAVE");
    public static final EventType<ControlEvent> CANCEL = new EventType<>(ANY, "CANCEL");

    public ControlEvent(@Nonnull EventType<? extends Event> eventType) {
        super(eventType);
    }
}
