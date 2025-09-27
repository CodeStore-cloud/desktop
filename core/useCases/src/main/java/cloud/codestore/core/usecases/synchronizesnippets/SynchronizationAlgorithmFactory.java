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

    SynchronizationAlgorithmFactory(Status status, SnippetSetFactory snippetSetFactory) {
        this.status = status;
        this.snippetSetFactory = snippetSetFactory;
    }

    Status getStatus() {
        return status;
    }

    Synchronization<Snippet> createSnippetSynchronizationAlgorithm(
            SynchronizationReport report,
            SynchronizationProgress progress
    ) {
        var progressListener = new SynchronizationProgressListener(report, progress);
        var localSnippetSet = snippetSetFactory.createLocalSnippetSet(report);
        var remoteSnippetSet = snippetSetFactory.createRemoteSnippetSet(report);

        var synchronization = new MutableItemSynchronization<>(localSnippetSet, remoteSnippetSet, status);
        synchronization.setProgressListener(progressListener);
        synchronization.setConflictResolver(new SnippetConflictResolver());
        return synchronization;
    }
}
