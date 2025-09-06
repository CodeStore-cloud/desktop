package cloud.codestore.core.usecases.synchronizesnippets;

import cloud.codestore.core.Sync;
import cloud.codestore.synchronization.ProgressListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Sync
class SynchronizationProgressListener implements ProgressListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(SynchronizationProgressListener.class);

    private final SynchronizationReport syncReport;

    SynchronizationProgressListener(SynchronizationReport syncReport) {
        this.syncReport = syncReport;
    }

    @Override
    public void numberOfItems(int snippetCount) {
        LOGGER.info("Total snippet count: {}", snippetCount);
        syncReport.setTotalSnippetCount(snippetCount);
    }

    @Override
    public void synchronizationStarted(String snippetId) {}

    @Override
    public void synchronizationFinished(String snippetId) {}

    @Override
    public void synchronizationFailed(String snippetId, Throwable exception) {
        LOGGER.error("Synchronization failed for snippet {}", snippetId, exception);
        syncReport.synchronizationFailed(snippetId, exception);
    }
}
