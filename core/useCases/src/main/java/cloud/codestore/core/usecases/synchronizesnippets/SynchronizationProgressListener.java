package cloud.codestore.core.usecases.synchronizesnippets;

import cloud.codestore.synchronization.ProgressListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class SynchronizationProgressListener implements ProgressListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(SynchronizationProgressListener.class);

    private final SynchronizationReport syncReport;
    private final SynchronizationProgress progress;
    private int processedSnippets;

    SynchronizationProgressListener(SynchronizationReport syncReport, SynchronizationProgress progress) {
        this.syncReport = syncReport;
        this.progress = progress;
    }

    @Override
    public void numberOfItems(int snippetCount) {
        LOGGER.info("Total snippet count: {}", snippetCount);
        progress.setTotalSnippets(snippetCount);
    }

    @Override
    public void synchronizationStarted(String snippetId) {}

    @Override
    public void synchronizationFinished(String snippetId) {
        progress.setProcessedSnippets(++processedSnippets);
    }

    @Override
    public void synchronizationFailed(String snippetId, Throwable exception) {
        LOGGER.error("Synchronization failed for snippet {}", snippetId, exception);
        progress.setProcessedSnippets(++processedSnippets);
        syncReport.synchronizationFailed(snippetId, exception);
    }
}
