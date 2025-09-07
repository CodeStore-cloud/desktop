package cloud.codestore.core.usecases.synchronizesnippets;

import cloud.codestore.core.Snippet;
import cloud.codestore.synchronization.Status;
import cloud.codestore.synchronization.Synchronization;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.mockito.Mockito.inOrder;

@ExtendWith(MockitoExtension.class)
@DisplayName("The synchronize-snippets use case")
class SynchronizeSnippetsTest {

    @Mock
    private Synchronization<Snippet> synchronization;
    @Mock
    private Status status;
    @Mock
    private SynchronizationReport syncReport;
    private SynchronizeSnippets useCase;

    @BeforeEach
    void setUp() {
        useCase = new SynchronizeSnippets(synchronization, status, null, syncReport);
    }

    @Test
    @DisplayName("executes the synchronization and updates the status afterwards")
    void executeSynchronization() throws IOException {
        useCase.synchronizeSnippets();

        InOrder inOrder = inOrder(synchronization, status);
        inOrder.verify(synchronization).synchronize();
        inOrder.verify(status).save();
    }
}