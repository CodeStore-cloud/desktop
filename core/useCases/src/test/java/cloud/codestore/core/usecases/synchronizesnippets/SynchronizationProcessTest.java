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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("The synchronization process")
class SynchronizationProcessTest {

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
        when(configuration.isCloudStorageConfigured()).thenReturn(true);
        synchronization = new SynchronizationProcess(configuration, algorithmFactory);
        lenient().when(algorithmFactory.createSnippetSynchronizationAlgorithm(any())).thenReturn(synchronizationAlgorithm);
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
            when(algorithmFactory.createSnippetSynchronizationAlgorithm(any())).thenAnswer(invocation -> {
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
}