package cloud.codestore.core.usecases.synchronizesnippets;

import cloud.codestore.core.Injectable;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * Contains a list of all executed synchronizations.
 */
@Injectable
public class ExecutedSynchronizations {
    private final List<Synchronization> executedSynchronizations = new ArrayList<>();

    public void add(@Nonnull Synchronization synchronization) {
        executedSynchronizations.add(synchronization);
    }

    public Synchronization get(int synchronizationId) throws SynchronizationNotExistsException {
        int index = synchronizationId - 1;
        if (index < 0 || index >= executedSynchronizations.size()) {
            throw new SynchronizationNotExistsException();
        }

        return executedSynchronizations.get(index);
    }
}
