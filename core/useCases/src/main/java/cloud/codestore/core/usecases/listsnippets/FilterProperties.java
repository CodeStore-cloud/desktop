package cloud.codestore.core.usecases.listsnippets;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;

/**
 * Provides information about how to filter the list of code snippets.
 */
public record FilterProperties(@Nonnull String languageName, @Nonnull Collection<String> tags) {
    public FilterProperties() {
        this("", Collections.emptySet());
    }

    public boolean isEmpty() {
        return languageName.isEmpty() && tags.isEmpty();
    }
}
