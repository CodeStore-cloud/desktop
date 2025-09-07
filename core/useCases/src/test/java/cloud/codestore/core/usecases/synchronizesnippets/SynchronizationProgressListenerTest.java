package cloud.codestore.core.usecases.synchronizesnippets;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("The progress listener")
class SynchronizationProgressListenerTest {
    private SynchronizationReport syncReport = new SynchronizationReport();
    private SynchronizationProgressListener progressListener = new  SynchronizationProgressListener(syncReport);

    @Test
    @DisplayName("sets the number of total snippets in the synchronization report")
    void setTotalSnippetsInReport() {
        assertThat(syncReport.getTotalSnippetCount()).isEqualTo(0);
        progressListener.numberOfItems(387);
        assertThat(syncReport.getTotalSnippetCount()).isEqualTo(387);
    }

    @Test
    @DisplayName("")
    void setErrorInReport() {
        Exception exception = new IOException("test error");
        assertThat(syncReport.getErrorCount()).isEqualTo(0);

        progressListener.synchronizationFailed("123", exception);

        assertThat(syncReport.getErrorCount()).isEqualTo(1);
        assertThat(syncReport.getErrors().get("123")).isSameAs(exception);
    }
}