package cloud.codestore.core.usecases.synchronizesnippets;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("The progress listener")
class SynchronizationProgressListenerTest {
    private SnippetSynchronizations executedSynchronizations = new SnippetSynchronizations();
    private SynchronizationProgress progress = new SynchronizationProgress();
    private SynchronizationProgressListener progressListener = new SynchronizationProgressListener(
            executedSynchronizations, progress
    );

    @Test
    @DisplayName("updates the progress when the synchronization of a snippet was finished")
    void setTotalSnippetsInReport() {
        progressListener.numberOfItems(5);
        assertThat(progress.getProgressInPercent()).isEqualTo(0);
        progressListener.synchronizationStarted("1");
        progressListener.synchronizationFinished("1");
        assertThat(progress.getProgressInPercent()).isEqualTo(20);
        progressListener.synchronizationStarted("2");
        progressListener.synchronizationFinished("2");
        assertThat(progress.getProgressInPercent()).isEqualTo(40);
        progressListener.synchronizationStarted("3");
        progressListener.synchronizationFinished("3");
        assertThat(progress.getProgressInPercent()).isEqualTo(60);
        progressListener.synchronizationStarted("4");
        progressListener.synchronizationFailed("4", new IOException());
        assertThat(progress.getProgressInPercent()).isEqualTo(80);
        progressListener.synchronizationStarted("5");
        progressListener.synchronizationFailed("5", new IOException());
        assertThat(progress.getProgressInPercent()).isEqualTo(100);
    }

    @Test
    @DisplayName("adds the synchronization objects to the list of executed synchronizations")
    void addToExecutedSynchronizations() throws SynchronizationNotExistsException {
        assertThatThrownBy(() -> executedSynchronizations.get("1"))
                .isInstanceOf(SynchronizationNotExistsException.class);
        progressListener.synchronizationStarted("1");
        assertThat(executedSynchronizations.get("1")).isNotNull();
    }
}