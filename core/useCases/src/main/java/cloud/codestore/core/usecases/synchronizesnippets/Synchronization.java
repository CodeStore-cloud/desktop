package cloud.codestore.core.usecases.synchronizesnippets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.IOException;

/**
 * Represents the process of synchronizing code snippets.
 */
public class Synchronization {
    private static final Logger LOGGER = LoggerFactory.getLogger(Synchronization.class);

    private final SynchronizationProgress progress = new SynchronizationProgress();
    private final SynchronizationReport report = new SynchronizationReport();
    private final SynchronizationAlgorithmFactory algorithmFactory;

    Synchronization(SynchronizationAlgorithmFactory algorithmFactory) {
        this.algorithmFactory = algorithmFactory;
    }

    void execute() {
        if (progress.getStatus() != SynchronizationStatus.PENDING) {
            throw new IllegalStateException("Synchronization has already been executed.");
        }

        LOGGER.info("===== Snippet synchronization startet =====");

        progress.setStatus(SynchronizationStatus.IN_PROGRESS);
        algorithmFactory.createSnippetSynchronizationAlgorithm(report, progress).synchronize();
        saveStatus();
        progress.setStatus(SynchronizationStatus.COMPLETED);

        LOGGER.info(
                "===== Snippet synchronization finished {} in {}ms =====",
                report.getErrorCount() == 0 ? "successfully" : "with errors",
                progress.getDuration()
        );
    }

    @Nonnull
    public SynchronizationProgress getProgress() {
        return progress;
    }

    @Nonnull
    public SynchronizationReport getReport() {
        return report;
    }

    private void saveStatus() {
        try {
            algorithmFactory.getStatus().save();
        } catch (IOException exception) {
            LOGGER.error("Failed to save status.", exception);
        }
    }
}
