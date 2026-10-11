package cloud.codestore.core.usecases.synchronizesnippets;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DisplayName("The list of executed synchronizations")
class ExecutedSynchronizationsTest {
    private static final String SNIPPET_ID = "123";

    private SnippetSynchronizations executedSynchronizations = new SnippetSynchronizations();

    @Test
    @DisplayName("provides access to the state of snippet synchronizations")
    void setAndGet() throws SynchronizationNotExistsException {
        assertThat(executedSynchronizations.getOptional(SNIPPET_ID)).isEmpty();
        executedSynchronizations.create(SNIPPET_ID);
        assertThat(executedSynchronizations.get(SNIPPET_ID)).isNotNull();
    }

    @Test
    @DisplayName("throws a SynchronizationNotExistsException of the synchronization object is not present")
    void throwNotFoundException() {
        assertThatThrownBy(() -> executedSynchronizations.get(SNIPPET_ID))
                .isInstanceOf(SynchronizationNotExistsException.class);
    }
}
