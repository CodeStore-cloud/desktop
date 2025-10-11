package cloud.codestore.core.usecases.synchronizesnippets;

import javax.annotation.Nonnull;

/**
 * Represents the synchronization state of a single code snippet.
 */
public class SnippetSynchronizationState extends SynchronizationState {
    private final String snippetId;

    SnippetSynchronizationState(@Nonnull String snippetId) {
        this.snippetId = snippetId;
    }

    @Nonnull
    public String getSnippetId() {
        return snippetId;
    }
}
