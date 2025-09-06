package cloud.codestore.core.usecases.synchronizesnippets;

import cloud.codestore.core.Snippet;
import cloud.codestore.core.UseCase;
import cloud.codestore.synchronization.Status;
import cloud.codestore.synchronization.Synchronization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Use Case: synchronize local code snippet with cloud storage.
 */
@UseCase
public class SynchronizeSnippets {
    private static final Logger LOGGER = LoggerFactory.getLogger(SynchronizeSnippets.class);

    private final Status status;
    private final Synchronization<Snippet> synchronization;
    private final SynchronizationReport syncReport;

    SynchronizeSnippets(
            Synchronization<Snippet> synchronization,
            Status status,
            SynchronizationProgressListener progressListener,
            SynchronizationReport syncReport
    ) {
        this.synchronization = synchronization;
        this.status = status;
        this.syncReport = syncReport;
        this.synchronization.setProgressListener(progressListener);
    }

    public SynchronizationReport synchronizeSnippets() {
        long start = System.currentTimeMillis();
        LOGGER.info("===== Snippet synchronization startet =====");

        synchronization.synchronize();
        saveStatus();

        LOGGER.info(
                "===== Snippet synchronization finished {} in {}ms =====",
                syncReport.getErrorCount() == 0 ? "successfully" : "with errors",
                System.currentTimeMillis() - start
        );

        return syncReport;
    }

    private void saveStatus() {
        try {
            status.save();
        } catch (IOException exception) {
            LOGGER.error("Failed to save status.", exception);
        }
    }
}
