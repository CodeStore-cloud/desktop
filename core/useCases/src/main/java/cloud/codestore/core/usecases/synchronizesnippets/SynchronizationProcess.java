package cloud.codestore.core.usecases.synchronizesnippets;

import cloud.codestore.core.Injectable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.IOException;

/**
 * Represents the process of synchronizing all code snippets.
 */
@Injectable
public class SynchronizationProcess {
    private static final Logger LOGGER = LoggerFactory.getLogger(SynchronizationProcess.class);

    private final ProcessDelegate delegate;

    SynchronizationProcess(
            SynchronizationConfiguration configuration,
            SynchronizationAlgorithmFactory algorithmFactory
    ) {
        if (configuration.isCloudStorageConfigured()) {
            delegate = new ActiveSynchronizationProcess(algorithmFactory);
        } else {
            LOGGER.info("Synchronization skipped");
            delegate = new SkippedSynchronizationProcess();
        }
    }

    public boolean isSkipped() {
        return delegate.isSkipped();
    }

    public void execute() {
        delegate.execute();
    }

    @Nonnull
    public SynchronizationState getState() {
        return delegate.getState();
    }

    @Nonnull
    public SynchronizationProgress getProgress() {
        return delegate.getProgress();
    }

    private interface ProcessDelegate {
        boolean isSkipped();
        void execute();
        SynchronizationState getState();
        SynchronizationProgress getProgress();
    }

    private static class ActiveSynchronizationProcess implements ProcessDelegate {
        private final SynchronizationProgress progress = new SynchronizationProgress();
        private final SynchronizationState state = new SynchronizationState();
        private final SynchronizationAlgorithmFactory algorithmFactory;

        private ActiveSynchronizationProcess(SynchronizationAlgorithmFactory algorithmFactory) {
            this.algorithmFactory = algorithmFactory;
        }

        @Override
        public boolean isSkipped() {
            return false;
        }

        @Override
        public void execute() {
            state.start(); // throws Exception if already startet or finished

            try {
                LOGGER.info("===== Snippet synchronization startet =====");
                algorithmFactory.createSnippetSynchronizationAlgorithm(progress).synchronize();
                saveStatus();
                state.complete();
                LOGGER.info("===== Snippet synchronization finished in {}ms =====", state.getDuration());
            } catch (Throwable error) {
                LOGGER.error("===== Snippet synchronization failed =====", error);
                state.fail(error);
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

        private void saveStatus() {
            try {
                algorithmFactory.getStatus().save();
            } catch (IOException exception) {
                LOGGER.error("Failed to save status.", exception);
            }
        }
    }

    private static class SkippedSynchronizationProcess implements ProcessDelegate {
        private static final String MESSAGE = "The synchronization was skipped.";

        @Override
        public boolean isSkipped() {
            return true;
        }

        @Override
        public void execute() {
            throw new IllegalStateException(MESSAGE);
        }

        @Nonnull
        @Override
        public SynchronizationState getState() {
            throw new IllegalStateException(MESSAGE);
        }

        @Nonnull
        @Override
        public SynchronizationProgress getProgress() {
            throw new IllegalStateException(MESSAGE);
        }
    }
}
