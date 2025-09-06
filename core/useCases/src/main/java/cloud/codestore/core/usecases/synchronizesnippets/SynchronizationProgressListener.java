package cloud.codestore.core.usecases.synchronizesnippets;

import cloud.codestore.synchronization.ProgressListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

class SynchronizationProgressListener implements ProgressListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(SynchronizationProgressListener.class);

    private final AtomicInteger errorCount =  new AtomicInteger(0);

    @Override
    public void numberOfItems(int snippetCount) {
        LOGGER.info("Total snippet count: {}", snippetCount);
    }

    @Override
    public void synchronizationStarted(String snippetId) {}

    @Override
    public void synchronizationFinished(String snippetId) {}

    @Override
    public void synchronizationFailed(String snippetId, Throwable exception) {
        LOGGER.error("Synchronization failed for snippet {}", snippetId, exception);
        errorCount.incrementAndGet();
    }

    int getErrorCount() {
        return errorCount.get();
    }
}
