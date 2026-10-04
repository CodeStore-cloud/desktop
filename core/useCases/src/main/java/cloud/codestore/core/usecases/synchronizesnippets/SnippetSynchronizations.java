package cloud.codestore.core.usecases.synchronizesnippets;

import cloud.codestore.core.Injectable;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Provides access to the synchronization states of each code snippet.
 */
@Injectable
public class SnippetSynchronizations {
    private final Map<String, SnippetSynchronizationState> snippetSyncState = Collections.synchronizedMap(new HashMap<>());

    void create(@Nonnull String snippetId) {
        snippetSyncState.put(snippetId, new SnippetSynchronizationState(snippetId));
    }

    SnippetSynchronizationState getOrCreate(@Nonnull String snippetId) {
        return snippetSyncState.computeIfAbsent(snippetId, SnippetSynchronizationState::new);
    }

    public SnippetSynchronizationState get(@Nonnull String snippetId) throws SynchronizationNotExistsException {
        return getOptional(snippetId).orElseThrow(SynchronizationNotExistsException::new);
    }

    public Optional<SnippetSynchronizationState> getOptional(@Nonnull String snippetId) {
        return Optional.ofNullable(snippetSyncState.get(snippetId));
    }
}
