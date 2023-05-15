package cloud.codestore.core.usecases.deletesnippet;

import cloud.codestore.core.SnippetNotExistsException;
import cloud.codestore.core.SnippetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("The delete-snippet use case")
class DeleteSnippetTest {
    private static final String SNIPPET_ID = UUID.randomUUID().toString();

    @Mock
    private SnippetRepository repository;
    private DeleteSnippet useCase;

    @BeforeEach
    void setUp() {
        useCase = new DeleteSnippet(repository);
    }

    @DisplayName("deletes the snippet from the repository")
    @Test
    void removeFromRepository() throws SnippetNotExistsException {
        when(repository.contains(SNIPPET_ID)).thenReturn(true);
        useCase.delete(SNIPPET_ID);
        verify(repository).delete(SNIPPET_ID);
    }

    @Test
    @DisplayName("throws a SnippetNotExistsException if the code snippet does not exist")
    void snippetNotExist() {
        when(repository.contains(SNIPPET_ID)).thenReturn(false);
        assertThatThrownBy(() -> useCase.delete(SNIPPET_ID)).isInstanceOf(SnippetNotExistsException.class);
    }
}