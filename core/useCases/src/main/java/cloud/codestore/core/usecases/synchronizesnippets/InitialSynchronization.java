package cloud.codestore.core.usecases.synchronizesnippets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.IOException;

/**
 * Represents the initial synchronization process.
 */
public class InitialSynchronization {
    private static final Logger LOGGER = LoggerFactory.getLogger(InitialSynchronization.class);

    private final InitialSynchronizationProgress progress = new InitialSynchronizationProgress();
    private final SynchronizationAlgorithmFactory algorithmFactory;

    InitialSynchronization(SynchronizationAlgorithmFactory algorithmFactory) {
        this.algorithmFactory = algorithmFactory;
    }

    void execute() {
        if (progress.getStatus() != SynchronizationStatus.PENDING) {
            throw new IllegalStateException("Synchronization has already been executed.");
        }

        try {
            progress.start();
            LOGGER.info("===== Snippet synchronization startet =====");
            algorithmFactory.createSnippetSynchronizationAlgorithm(progress).synchronize();
            saveStatus();
            progress.complete();
            LOGGER.info("===== Snippet synchronization finished in {}ms =====", progress.getDuration());
        } catch (Throwable error) {
            LOGGER.error("===== Snippet synchronization failed =====", error);
            progress.fail(error);
        }
    }

    @Nonnull
    public InitialSynchronizationProgress getProgress() {
        return progress;
    }

    private void saveStatus() {
        try {
            algorithmFactory.getStatus().save();
        } catch (IOException exception) {
            LOGGER.error("Failed to save status.", exception);
        }
    }
}
