package cloud.codestore.core.usecases.listsnippets;

import cloud.codestore.core.Language;

import javax.annotation.Nullable;

/**
 * Provides information about how to filter the list of code snippets.
 */
public record FilterProperties(@Nullable Language language) {
    public boolean isEmpty() {
        return language == null;
    }
}
