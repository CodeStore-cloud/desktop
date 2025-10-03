package cloud.codestore.core.usecases.synchronizesnippets;

import cloud.codestore.core.Injectable;

/**
 * Use Case: synchronize all local code snippets with cloud storage.
 */
@Injectable
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
        InitialSynchronization initialSynchronization = new InitialSynchronization(algorithmFactory);
        executedSynchronizations.setInitialSynchronization(initialSynchronization);
        initialSynchronization.execute();
    }
}
