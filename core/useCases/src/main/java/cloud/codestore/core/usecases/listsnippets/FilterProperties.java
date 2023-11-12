package cloud.codestore.core.usecases.listsnippets;

import cloud.codestore.core.Language;

import javax.annotation.Nullable;
import java.util.Collection;

/**
 * Provides information about how to filter the list of code snippets.
 */
public record FilterProperties(@Nullable Language language, @Nullable Collection<String> tags) {
    public FilterProperties() {
        this(null, null);
    }

    public boolean isEmpty() {
        return language == null && tags == null;
    }
}
