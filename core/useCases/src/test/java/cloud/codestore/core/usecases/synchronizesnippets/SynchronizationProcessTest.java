package cloud.codestore.core.usecases.synchronizesnippets;

import cloud.codestore.core.Snippet;
import cloud.codestore.synchronization.ItemSet;
import cloud.codestore.synchronization.MutableItemSynchronization;
import cloud.codestore.synchronization.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("The synchronization process")
class SynchronizationProcessTest {
    private static final Set<String> SNIPPET_IDS = Set.of("1", "2", "3");

    @Mock
    private Status status;
    @Mock
    private SnippetSetFactory snippetSetFactory;
    @Mock
    private ItemSet<Snippet> localSnippetSet;
    private SnippetSynchronizations snippetSyncStates;
    private SynchronizationProcess process;
    private SynchronizationConfiguration configuration;

    @BeforeEach
    void setUp() {
        snippetSyncStates = new SnippetSynchronizations();
        process = new SynchronizationProcess(status, snippetSetFactory, snippetSyncStates);

        configuration = new SynchronizationConfiguration(CloudService.GOOGLE_DRIVE);
        when(snippetSetFactory.createLocalSnippetSet()).thenReturn(localSnippetSet);
        when(localSnippetSet.getItemIds()).thenReturn(SNIPPET_IDS);
    }

    @Test
    @DisplayName("can only be executed once")
    void executedOnce() {
        process.getState().start();
        process.getState().complete();
        assertThatThrownBy(() -> process.execute(configuration)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("is skipped if no cloud service is configured")
    void synchronizationSkipped() throws IOException {
        configuration = SynchronizationConfiguration.empty();
        try (var constructorMock = mockConstruction(MutableItemSynchronization.class)) {
            var synchronization = constructorMock.constructed().getFirst();
            process.execute(configuration);
            verify(synchronization, never()).synchronize();
        }

        assertThat(process.wasExecuted()).isFalse();
        verify(status, never()).save();

        for (String snippetId : SNIPPET_IDS) {
            assertThat(snippetSyncStates.getOptional(snippetId)).isEmpty();
        }
    }

    @Test
    @DisplayName("executes the synchronization successfully")
    void synchronizationSuccessful() throws SynchronizationNotExistsException, IOException {
        try (var constructorMock = mockConstruction(MutableItemSynchronization.class)) {
            var synchronization = constructorMock.constructed().getFirst();
            process.execute(configuration);
            verify(synchronization).synchronize();
        }

        assertThat(process.wasExecuted()).isTrue();
        verify(status).save();

        SynchronizationState globalSyncState = process.getState();
        assertThat(globalSyncState).isNotNull();
        assertThat(globalSyncState.getStatus()).isEqualTo(SynchronizationStatus.COMPLETED);
        assertThat(globalSyncState.getError()).isNull();
        assertThat(globalSyncState.getStartTime()).isNotNull();
        assertThat(globalSyncState.getEndTime()).isNotNull().isAfter(globalSyncState.getStartTime());

        for (String snippetId : SNIPPET_IDS) {
            assertThat(snippetSyncStates.getOptional(snippetId)).isPresent();
            SnippetSynchronizationState snippetSyncState = snippetSyncStates.get(snippetId);
            assertThat(snippetSyncState.getStatus()).isEqualTo(SynchronizationStatus.COMPLETED);
            assertThat(snippetSyncState.getError()).isNull();
            assertThat(snippetSyncState.getStartTime()).isNotNull();
            assertThat(snippetSyncState.getEndTime()).isNotNull().isAfter(snippetSyncState.getStartTime());
        }
    }

    @Test
    @DisplayName("marks all code snippets as failed if the overall synchronization fails")
    void markSnippetsFailed() throws SynchronizationNotExistsException {
        Throwable error = new RuntimeException();
        try (var constructorMock = mockConstruction(MutableItemSynchronization.class)) {
            var synchronization = constructorMock.constructed().getFirst();
            doThrow(error).when(synchronization).synchronize();
            process.execute(configuration);
        }

        assertThat(process.wasExecuted()).isTrue();

        SynchronizationState globalSyncState = process.getState();
        assertThat(globalSyncState).isNotNull();
        assertThat(globalSyncState.getStatus()).isEqualTo(SynchronizationStatus.FAILED);
        assertThat(globalSyncState.getError()).isSameAs(error);
        assertThat(globalSyncState.getStartTime()).isNull();
        assertThat(globalSyncState.getEndTime()).isNull();

        for (String snippetId : SNIPPET_IDS) {
            assertThat(snippetSyncStates.getOptional(snippetId)).isPresent();
            SnippetSynchronizationState snippetSyncState = snippetSyncStates.get(snippetId);
            assertThat(snippetSyncState.getStatus()).isEqualTo(SynchronizationStatus.FAILED);
            assertThat(snippetSyncState.getError()).isSameAs(error);
            assertThat(snippetSyncState.getStartTime()).isNull();
            assertThat(snippetSyncState.getEndTime()).isNull();
        }
    }
}