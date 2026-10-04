package cloud.codestore.core.usecases.synchronizesnippets;

import cloud.codestore.core.Injectable;
import cloud.codestore.core.Snippet;
import cloud.codestore.synchronization.ItemSet;
import cloud.codestore.synchronization.MutableItemSynchronization;
import cloud.codestore.synchronization.Status;
import cloud.codestore.synchronization.Synchronization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents the process of synchronizing all code snippets.
 */
@Injectable
public class SynchronizationProcess {
    private static final Logger LOGGER = LoggerFactory.getLogger(SynchronizationProcess.class);

    private final SynchronizationProgress progress = new SynchronizationProgress();
    private final SynchronizationState state = new SynchronizationState();
    private final Status status;
    private final SnippetSetFactory snippetSetFactory;
    private final SnippetSynchronizations snippetSyncStates;

    SynchronizationProcess(
            Status status,
            SnippetSetFactory snippetSetFactory,
            SnippetSynchronizations snippetSyncStates
    ) {
        this.status = status;
        this.snippetSetFactory = snippetSetFactory;
        this.snippetSyncStates = snippetSyncStates;
    }

    public boolean wasExecuted() {
        return state.getStatus() != SynchronizationStatus.PENDING;
    }

    public void execute(SynchronizationConfiguration configuration) {
        if (wasExecuted()) {
            throw new IllegalStateException("The synchronization has already been executed.");
        }

        if (configuration.isCloudServiceConfigured()) {
            try {
                LOGGER.info("===== Snippet synchronization startet =====");
                state.start();
                execute(configuration.cloudService());
                state.complete();
                LOGGER.info("===== Snippet synchronization finished in {}ms =====", state.getDuration());
            } catch (Throwable throwable) {
                LOGGER.error("===== Snippet synchronization failed =====", throwable);
                state.fail(throwable);
            }
        } else {
            LOGGER.info("Synchronization skipped");
        }
    }

    @Nonnull
    public SynchronizationState getState() {
        return state;
    }

    @Nonnull
    public SynchronizationProgress getProgress() {
        return progress;
    }

    private void execute(@Nonnull CloudService cloudService) {
        Set<String> localSnippets = new HashSet<>();
        try {
            var localSnippetSet = snippetSetFactory.createLocalSnippetSet();
            var remoteSnippetSet = snippetSetFactory.createRemoteSnippetSet(cloudService);

            localSnippets.addAll(localSnippetSet.getItemIds());
            markSyncStatesAsPending(localSnippets);

            var synchronization = createSynchronization(localSnippetSet, remoteSnippetSet);
            synchronization.synchronize();

            saveStatus();
        } catch (Throwable throwable) {
            markAllSyncStatesAsFailed(localSnippets, throwable);
            throw throwable;
        }
    }

    private Synchronization<Snippet> createSynchronization(
            ItemSet<Snippet> localSnippetSet,
            ItemSet<Snippet> remoteSnippetSet
    ) {
        var synchronization = new MutableItemSynchronization<>(localSnippetSet, remoteSnippetSet, status);
        synchronization.setProgressListener(new SynchronizationProgressListener(snippetSyncStates, progress));
        synchronization.setConflictResolver(new SnippetConflictResolver());
        return synchronization;
    }

    private void markSyncStatesAsPending(Set<String> localSnippets) {
        localSnippets.forEach(snippetSyncStates::create);
    }

    private void markAllSyncStatesAsFailed(Set<String> localSnippets, Throwable error) {
        for (String snippetId : localSnippets) {
            snippetSyncStates.getOrCreate(snippetId).fail(error);
        }
    }

    private void saveStatus() {
        try {
            status.save();
        } catch (IOException exception) {
            LOGGER.error("Failed to save status.", exception);
        }
    }
}
