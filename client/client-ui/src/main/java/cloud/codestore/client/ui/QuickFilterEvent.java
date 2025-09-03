package cloud.codestore.client.ui;

import cloud.codestore.client.Language;
import javafx.event.Event;
import javafx.event.EventType;

import javax.annotation.Nonnull;
import java.util.Optional;

/**
 * An event that is fired when the user clicks on a quick filter element.
 */
public class QuickFilterEvent extends Event {
    public static final EventType<QuickFilterEvent> ANY = new EventType<>(Event.ANY, "ANY_QUICK_FILTER");

    private Language language;
    private String tag;

    public QuickFilterEvent(@Nonnull Language language) {
        super(ANY);
        this.language = language;
    }

    public QuickFilterEvent(@Nonnull String tag) {
        super(ANY);
        this.tag = tag;
    }

    @Nonnull
    public Optional<Language> getLanguage() {
        return Optional.ofNullable(language);
    }

    @Nonnull
    public Optional<String> getTag() {
        return Optional.ofNullable(tag);
    }
}
