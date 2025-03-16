package cloud.codestore.client.ui.selection.filter;

import cloud.codestore.client.Language;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * An event to add filter values via quickfilter buttons.
 */
public record QuickFilterEvent(@Nullable Language language, @Nullable String tag) {
    public QuickFilterEvent(@Nonnull Language language) {
        this(language, null);
    }

    public QuickFilterEvent(@Nonnull String tag) {
        this(null, tag);
    }
}
