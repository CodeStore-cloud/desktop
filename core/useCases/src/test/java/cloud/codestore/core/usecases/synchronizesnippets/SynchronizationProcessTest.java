package cloud.codestore.core.usecases.synchronizesnippets;

import cloud.codestore.core.Snippet;
import cloud.codestore.synchronization.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.UncheckedIOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("The synchronization process")
class SynchronizationProcessTest {

    @Mock
    private ReadSynchronizationConfigurationQuery query;
    @Mock
    private SynchronizationConfiguration configuration;
    @Mock
    private SynchronizationAlgorithmFactory algorithmFactory;
    @Mock
    private cloud.codestore.synchronization.Synchronization<Snippet> synchronizationAlgorithm;
    @Mock
    private Status status;
    private SynchronizationProcess synchronization;

    @BeforeEach
    void setUp() {
        when(query.read()).thenReturn(configuration);
        when(configuration.isCloudServiceConfigured()).thenReturn(true);
        synchronization = new SynchronizationProcess(query, algorithmFactory);
        lenient().when(algorithmFactory.createSnippetSynchronizationAlgorithm(any(), any())).thenReturn(synchronizationAlgorithm);
        lenient().when(algorithmFactory.getStatus()).thenReturn(status);
    }

    @Test
    @DisplayName("provides access to the progress")
    void providesAccessToProgressAndReport() {
        assertThat(synchronization.getProgress()).isNotNull();
    }

    @Test
    @DisplayName("can only be started once")
    void avoidRestart() {
        synchronization.execute();
        assertThatThrownBy(synchronization::execute).isInstanceOf(IllegalStateException.class);
    }

    @Nested
    @DisplayName("when executing")
    class ExecutionTest {
        @Test
        @DisplayName("updates the status")
        void setProgress() {
            when(algorithmFactory.createSnippetSynchronizationAlgorithm(any(), any())).thenAnswer(invocation -> {
                assertThat(synchronization.getState().getStatus()).isEqualTo(SynchronizationStatus.IN_PROGRESS);
                return synchronizationAlgorithm;
            });
            synchronization.execute();
            assertThat(synchronization.getState().getStatus()).isEqualTo(SynchronizationStatus.COMPLETED);
        }

        @Test
        @DisplayName("executes the synchronization algorithm and saves the status afterwards")
        void executeSynchronizationAlgorithm() throws IOException {
            synchronization.execute();

            InOrder inOrder = inOrder(synchronizationAlgorithm, status);
            inOrder.verify(synchronizationAlgorithm).synchronize();
            inOrder.verify(status).save();
        }
    }

    @Nested
    @DisplayName("when the configuration could not be read")
    class ConfigurationErrorTest {
        private static final RuntimeException ERROR = new UncheckedIOException(new IOException("Loading config failed"));

        @BeforeEach
        void setUp() {
            when(query.read()).thenThrow(ERROR);
        }

        @Test
        @DisplayName("fails with the original exception")
        void failWithOriginalException() {
            synchronization = new SynchronizationProcess(query, algorithmFactory);
            synchronization.execute();

            SynchronizationState state = synchronization.getState();
            assertThat(state.getStatus()).isEqualTo(SynchronizationStatus.FAILED);
            assertThat(state.getError()).isSameAs(ERROR);
        }
    }
}