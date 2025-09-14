package cloud.codestore.core.usecases.synchronizesnippets;

import cloud.codestore.core.Sync;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

/**
 * Contains a list of all executed synchronizations and corresponding reports.
 */
@Sync
public class ExecutedSynchronizations {
    private final Map<Integer, Synchronization> executedSynchronizations = new HashMap<>();

    public void add(@Nonnull Synchronization synchronization) {
        int synchronizationId = executedSynchronizations.size();
        executedSynchronizations.put(synchronizationId, synchronization);
    }

    public Synchronization get(int synchronizationId) throws SynchronizationNotExistsException {
        if (executedSynchronizations.containsKey(synchronizationId)) {
            return executedSynchronizations.get(synchronizationId);
        } else {
            throw new SynchronizationNotExistsException();
        }
    }
}
