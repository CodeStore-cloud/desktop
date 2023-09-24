package cloud.codestore.core.usecases.deletesnippet;

import cloud.codestore.core.SnippetNotExistsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("The delete-snippet use case")
class DeleteSnippetTest {
    private static final String SNIPPET_ID = UUID.randomUUID().toString();

    @Mock
    private DeleteSnippetQuery query;
    private DeleteSnippet useCase;

    @BeforeEach
    void setUp() {
        useCase = new DeleteSnippet(query);
    }

    @DisplayName("deletes the snippet from the repository")
    @Test
    void removeFromRepository() throws SnippetNotExistsException {
        useCase.delete(SNIPPET_ID);
        verify(query).delete(SNIPPET_ID);
    }
}