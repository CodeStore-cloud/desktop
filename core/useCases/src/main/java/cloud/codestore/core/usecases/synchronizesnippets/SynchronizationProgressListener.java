package cloud.codestore.core.usecases.synchronizesnippets;

import cloud.codestore.synchronization.ProgressListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class SynchronizationProgressListener implements ProgressListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(SynchronizationProgressListener.class);

    private final SnippetSynchronizations executedSynchronizations;
    private final SynchronizationProgress progress;
    private int processedSnippets;

    SynchronizationProgressListener(
            SnippetSynchronizations executedSynchronizations,
            SynchronizationProgress progress
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
        SnippetSynchronizationState state = new SnippetSynchronizationState(snippetId);
        state.start();
        executedSynchronizations.add(state);
    }

    @Override
    public void synchronizationFinished(String snippetId) {
        try {
            executedSynchronizations.get(snippetId).complete();
        } catch (SynchronizationNotExistsException exception) {
            synchronizationFailed(snippetId, exception);
        }

        progress.setProcessedSnippets(++processedSnippets);
    }

    @Override
    public void synchronizationFailed(String snippetId, Throwable exception) {
        LOGGER.error("Synchronization failed for snippet {}", snippetId, exception);
        progress.setProcessedSnippets(++processedSnippets);

        SnippetSynchronizationState state;
        try {
            state = executedSynchronizations.get(snippetId);
        } catch (SynchronizationNotExistsException notExistsException) {
            exception = notExistsException;
            state = new SnippetSynchronizationState(snippetId);
            executedSynchronizations.add(state);
        }
        state.fail(exception);
    }
}
