package cloud.codestore.core.usecases.synchronizesnippets;

import cloud.codestore.core.UseCase;

/**
 * Use Case: synchronize all local code snippets with cloud storage.
 */
@UseCase
public class SynchronizeSnippets {
    private final SynchronizationAlgorithmFactory algorithmFactory;
    private final ExecutedSynchronizations executedSynchronizations;

    SynchronizeSnippets(
            SynchronizationAlgorithmFactory algorithmFactory,
            ExecutedSynchronizations executedSynchronizations
    ) {
        this.algorithmFactory = algorithmFactory;
        this.executedSynchronizations = executedSynchronizations;
    }

    public void synchronizeSnippets() {
        Synchronization synchronization = new Synchronization(algorithmFactory);
        executedSynchronizations.add(synchronization);
        synchronization.execute();
    }
}
