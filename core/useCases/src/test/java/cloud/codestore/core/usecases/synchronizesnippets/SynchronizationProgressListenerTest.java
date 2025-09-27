package cloud.codestore.core.usecases.synchronizesnippets;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("The progress listener")
class SynchronizationProgressListenerTest {
    private SynchronizationReport syncReport = new SynchronizationReport();
    private SynchronizationProgress progress = new SynchronizationProgress();
    private SynchronizationProgressListener progressListener = new SynchronizationProgressListener(
            syncReport, progress
    );

    @Test
    @DisplayName("updates the progress when the synchronization of a snippet was finished")
    void setTotalSnippetsInReport() {
        progressListener.numberOfItems(5);
        assertThat(progress.getProgressInPercent()).isEqualTo(0);
        progressListener.synchronizationFinished("1");
        assertThat(progress.getProgressInPercent()).isEqualTo(20);
        progressListener.synchronizationFinished("2");
        assertThat(progress.getProgressInPercent()).isEqualTo(40);
        progressListener.synchronizationFinished("3");
        assertThat(progress.getProgressInPercent()).isEqualTo(60);
        progressListener.synchronizationFailed("4", new IOException());
        assertThat(progress.getProgressInPercent()).isEqualTo(80);
        progressListener.synchronizationFailed("5", new IOException());
        assertThat(progress.getProgressInPercent()).isEqualTo(100);
    }

    @Test
    @DisplayName("sets the number of errors in the report")
    void passErrorToReport() {
        assertThat(syncReport.getErrorCount()).isEqualTo(0);
        progressListener.synchronizationFailed("1", new IOException());
        progressListener.synchronizationFailed("2", new IOException());
        progressListener.synchronizationFailed("3", new IOException());
        assertThat(syncReport.getErrorCount()).isEqualTo(3);
    }
}