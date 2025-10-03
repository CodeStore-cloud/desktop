package cloud.codestore.core.usecases.synchronizesnippets;

import cloud.codestore.synchronization.ProgressListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class SynchronizationProgressListener implements ProgressListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(SynchronizationProgressListener.class);

    private final ExecutedSynchronizations executedSynchronizations;
    private final InitialSynchronizationProgress progress;
    private int processedSnippets;

    SynchronizationProgressListener(
            ExecutedSynchronizations executedSynchronizations,
            InitialSynchronizationProgress progress
    ) {
        this.executedSynchronizations = executedSynchronizations;
        this.progress = progress;
    }

    @Override
    public void numberOfItems(int snippetCount) {
        LOGGER.info("Total snippet count: {}", snippetCount);
        progress.setTotalSnippets(snippetCount);
    }

    @Override
    public void synchronizationStarted(String snippetId) {
        SnippetSynchronization snippetSynchronization = new SnippetSynchronization(snippetId);
        snippetSynchronization.start();
        executedSynchronizations.add(snippetSynchronization);
    }

    @Override
    public void synchronizationFinished(String snippetId) {
        try {
            SnippetSynchronization snippetSynchronization = executedSynchronizations.get(snippetId);
            snippetSynchronization.complete();
        } catch (SynchronizationNotExistsException exception) {
            synchronizationFailed(snippetId, exception);
        }

        progress.setProcessedSnippets(++processedSnippets);
    }

    @Override
    public void synchronizationFailed(String snippetId, Throwable exception) {
        LOGGER.error("Synchronization failed for snippet {}", snippetId, exception);
        progress.setProcessedSnippets(++processedSnippets);

        SnippetSynchronization snippetSynchronization;
        try {
            snippetSynchronization = executedSynchronizations.get(snippetId);
        } catch (SynchronizationNotExistsException notExistsException) {
            exception = notExistsException;
            snippetSynchronization = new SnippetSynchronization(snippetId);
            executedSynchronizations.add(snippetSynchronization);
        }
        snippetSynchronization.fail(exception);
    }
}
