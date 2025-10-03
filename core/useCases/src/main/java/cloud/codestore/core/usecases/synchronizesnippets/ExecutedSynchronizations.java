package cloud.codestore.core.usecases.synchronizesnippets;

import cloud.codestore.core.Injectable;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Contains a list of all executed synchronizations.
 */
@Injectable
public class ExecutedSynchronizations {
    private InitialSynchronization initialSynchronization;
    private final Map<String, SnippetSynchronization> snippetSynchronizations = Collections.synchronizedMap(new HashMap<>());

    void setInitialSynchronization(@Nonnull InitialSynchronization initialSynchronization) {
        this.initialSynchronization = initialSynchronization;
    }

    @Nonnull
    public InitialSynchronization getInitialSynchronization() throws SynchronizationNotExistsException {
        if (initialSynchronization == null) {
            throw new SynchronizationNotExistsException();
        }

        return initialSynchronization;
    }

    @Nonnull
    public Optional<InitialSynchronization> getOptionalInitialSynchronization() {
        return Optional.ofNullable(initialSynchronization);
    }

    void add(@Nonnull SnippetSynchronization synchronization) {
        snippetSynchronizations.put(synchronization.getSnippetId(), synchronization);
    }

    public SnippetSynchronization get(@Nonnull String snippetId) throws SynchronizationNotExistsException {
        if (!snippetSynchronizations.containsKey(snippetId)) {
            throw new SynchronizationNotExistsException();
        }

        return snippetSynchronizations.get(snippetId);
    }
}
