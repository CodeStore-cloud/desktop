package cloud.codestore.core.usecases.synchronizesnippets;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DisplayName("The list of executed synchronizations")
class ExecutedSynchronizationsTest {

    private SnippetSynchronizations executedSynchronizations = new SnippetSynchronizations();

    @Test
    @DisplayName("provides access to the state of snippet synchronizations")
    void setAndGet() throws SynchronizationNotExistsException {
        var snippetSync = new SnippetSynchronizationState("123");
        executedSynchronizations.add(snippetSync);
        assertThat(executedSynchronizations.get("123")).isSameAs(snippetSync);
    }

    @Test
    @DisplayName("throws a SynchronizationNotExistsException of the synchronization object is not present")
    void throwNotFoundException() {
        assertThatThrownBy(() -> executedSynchronizations.get("123"))
                .isInstanceOf(SynchronizationNotExistsException.class);
    }
}
