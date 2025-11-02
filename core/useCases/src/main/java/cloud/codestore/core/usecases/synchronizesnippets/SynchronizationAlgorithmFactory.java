package cloud.codestore.core.usecases.synchronizesnippets;

import cloud.codestore.core.Injectable;
import cloud.codestore.core.Snippet;
import cloud.codestore.synchronization.MutableItemSynchronization;
import cloud.codestore.synchronization.Status;
import cloud.codestore.synchronization.Synchronization;

/**
 * A factory to create a fully initialized {@link Synchronization} object.
 */
@Injectable
class SynchronizationAlgorithmFactory {
    private final Status status;
    private final SnippetSetFactory snippetSetFactory;
    private final SnippetSynchronizations executedSynchronizations;

    SynchronizationAlgorithmFactory(
            Status status,
            SnippetSetFactory snippetSetFactory,
            SnippetSynchronizations executedSynchronizations
    ) {
        this.status = status;
        this.snippetSetFactory = snippetSetFactory;
        this.executedSynchronizations = executedSynchronizations;
    }

    Status getStatus() {
        return status;
    }

    Synchronization<Snippet> createSnippetSynchronizationAlgorithm(
            SynchronizationProgress progress,
            CloudService cloudService
    ) {
        var progressListener = new SynchronizationProgressListener(executedSynchronizations, progress);
        var localSnippetSet = snippetSetFactory.createLocalSnippetSet();
        var remoteSnippetSet = snippetSetFactory.createRemoteSnippetSet(cloudService);

        var synchronization = new MutableItemSynchronization<>(localSnippetSet, remoteSnippetSet, status);
        synchronization.setProgressListener(progressListener);
        synchronization.setConflictResolver(new SnippetConflictResolver());
        return synchronization;
    }
}
