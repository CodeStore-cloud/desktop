package cloud.codestore.core.usecases.synchronizesnippets;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;

@DisplayName("The list of executed synchronizations")
class ExecutedSynchronizationsTest {

    private ExecutedSynchronizations executedSynchronizations = new ExecutedSynchronizations();

    @Test
    @DisplayName("is empty after creation")
    void initiallyEmpty() {
        assertNotExists(1);
    }

    @Test
    @DisplayName("provides access to the objects by ID starting at 1")
    void getSynchronizationById() throws SynchronizationNotExistsException {
        assertNotExists(0);
        assertNotExists(1);
        executedSynchronizations.add(mock(Synchronization.class));
        assertExists(1);

        assertNotExists(2);
        executedSynchronizations.add(mock(Synchronization.class));
        assertExists(2);

        assertNotExists(3);
        executedSynchronizations.add(mock(Synchronization.class));
        assertExists(3);
    }

    private void assertNotExists(int syncId) {
        assertThatThrownBy(() -> executedSynchronizations.get(syncId))
                .isInstanceOf(SynchronizationNotExistsException.class);
    }

    private void assertExists(int syncId) throws SynchronizationNotExistsException {
        assertThat(executedSynchronizations.get(syncId)).isNotNull();
    }
}
