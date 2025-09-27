package cloud.codestore.core.usecases.synchronizesnippets;

import cloud.codestore.core.Snippet;
import cloud.codestore.synchronization.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("The synchronize-snippets use case")
class SynchronizeSnippetsTest {

    @Mock
    private SynchronizationAlgorithmFactory algorithmFactory;
    @Mock
    private ExecutedSynchronizations executedSynchronizations;
    @Mock
    private cloud.codestore.synchronization.Synchronization<Snippet> synchronizationAlgorithm;
    @Mock
    private Status status;
    private SynchronizeSnippets useCase;

    @BeforeEach
    void setUp() {
        when(algorithmFactory.createSnippetSynchronizationAlgorithm(any(), any())).thenReturn(synchronizationAlgorithm);
        when(algorithmFactory.getStatus()).thenReturn(status);
        useCase = new SynchronizeSnippets(algorithmFactory, executedSynchronizations);
    }

    @Test
    @DisplayName("executes the synchronization and updates the status afterwards")
    void executeSynchronization() {
        useCase.synchronizeSnippets();

        ArgumentCaptor<Synchronization> argument = ArgumentCaptor.forClass(Synchronization.class);
        verify(executedSynchronizations).add(argument.capture());
        var synchronization = argument.getValue();
        assertThat(synchronization.getProgress().getStatus()).isEqualTo(SynchronizationStatus.COMPLETED);
    }
}