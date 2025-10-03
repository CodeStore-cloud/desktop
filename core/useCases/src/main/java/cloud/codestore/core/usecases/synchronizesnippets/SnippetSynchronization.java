package cloud.codestore.core.usecases.synchronizesnippets;

import javax.annotation.Nonnull;

/**
 * Represents the synchronization of a single code snippet.
 */
public class SnippetSynchronization extends SynchronizationResult {
    private final String snippetId;

    SnippetSynchronization(@Nonnull String snippetId) {
        this.snippetId = snippetId;
    }

    @Nonnull
    public String getSnippetId() {
        return snippetId;
    }
}
