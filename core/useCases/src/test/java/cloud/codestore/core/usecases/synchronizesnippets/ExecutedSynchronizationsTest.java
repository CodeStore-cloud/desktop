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
    @DisplayName("provides access to executed synchronizations")
    void setAndGet() throws SynchronizationNotExistsException {
        var initialSync = mock(InitialSynchronization.class);
        executedSynchronizations.setInitialSynchronization(initialSync);
        var snippetSync = new SnippetSynchronization("123");
        executedSynchronizations.add(snippetSync);

        assertThat(executedSynchronizations.getInitialSynchronization()).isSameAs(initialSync);
        assertThat(executedSynchronizations.get("123")).isSameAs(snippetSync);
    }

    @Test
    @DisplayName("throws a SynchronizationNotExistsException of the synchronization object is not present")
    void throwNotFoundException() {
        assertThatThrownBy(executedSynchronizations::getInitialSynchronization)
                .isInstanceOf(SynchronizationNotExistsException.class);

        assertThatThrownBy(() -> executedSynchronizations.get("123"))
                .isInstanceOf(SynchronizationNotExistsException.class);
    }
}
