package cloud.codestore.core.usecases.synchronizesnippets;

import cloud.codestore.synchronization.ProgressListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class SynchronizationProgressListener implements ProgressListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(SynchronizationProgressListener.class);

    private final SnippetSynchronizations snippetSyncStates;
    private final SynchronizationProgress progress;
    private int processedSnippets;

    SynchronizationProgressListener(SnippetSynchronizations snippetSyncStates, SynchronizationProgress progress) {
        this.snippetSyncStates = snippetSyncStates;
        this.progress = progress;
    }

    @Override
    public void numberOfItems(int snippetCount) {
        LOGGER.info("Total snippet count: {}", snippetCount);
        progress.setTotalSnippets(snippetCount);
    }

    @Override
    public void synchronizationStarted(String snippetId) {
        snippetSyncStates.getOrCreate(snippetId).start();
    }

    @Override
    public void synchronizationFinished(String snippetId) {
        try {
            snippetSyncStates.get(snippetId).complete();
            progress.setProcessedSnippets(++processedSnippets);
        } catch (SynchronizationNotExistsException exception) {
            synchronizationFailed(snippetId, exception);
        }
    }

    @Override
    public void synchronizationFailed(String snippetId, Throwable exception) {
        LOGGER.error("Synchronization failed for snippet {}", snippetId, exception);
        progress.setProcessedSnippets(++processedSnippets);
        snippetSyncStates.getOrCreate(snippetId).fail(exception);
    }
}
