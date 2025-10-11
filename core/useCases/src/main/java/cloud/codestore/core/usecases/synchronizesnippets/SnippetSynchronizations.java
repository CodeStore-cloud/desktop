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

    void add(@Nonnull SnippetSynchronizationState synchronization) {
        snippetSyncState.put(synchronization.getSnippetId(), synchronization);
    }

    public SnippetSynchronizationState get(@Nonnull String snippetId) throws SynchronizationNotExistsException {
        if (!snippetSyncState.containsKey(snippetId)) {
            throw new SynchronizationNotExistsException();
        }

        return snippetSyncState.get(snippetId);
    }

    public Optional<SnippetSynchronizationState> getOptional(@Nonnull String snippetId) {
        return Optional.ofNullable(snippetSyncState.get(snippetId));
    }
}
