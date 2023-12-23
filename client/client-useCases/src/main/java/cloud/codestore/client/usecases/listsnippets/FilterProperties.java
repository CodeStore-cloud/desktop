package cloud.codestore.client.usecases.listsnippets;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Set;

/**
 * Provides information about how to filter the requested code snippets.
 */
public record FilterProperties(@Nonnull Set<String> tags) {
    public FilterProperties() {
        this(Collections.emptySet());
    }

    public boolean isEmpty() {
        return tags.isEmpty();
    }
}
