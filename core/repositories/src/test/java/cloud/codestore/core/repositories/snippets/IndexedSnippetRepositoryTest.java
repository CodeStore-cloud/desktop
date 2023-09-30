package cloud.codestore.core.repositories.snippets;

import cloud.codestore.core.Snippet;
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
@DisplayName("The indexed snippet repository")
class IndexedSnippetRepositoryTest {
    private static final String SNIPPET_ID = UUID.randomUUID().toString();

    @Mock
    private SnippetIndex index;
    @Mock
    private FileSystemRepository localRepo;
    private IndexedSnippetRepository repository;

    @BeforeEach
    void setUp() {
        repository = new IndexedSnippetRepository(index, localRepo);
    }

    @Test
    @DisplayName("adds a new snippet to the index after saving it on the file system")
    void createSnippet() {
        Snippet snippet = testSnippet();
        repository.create(snippet);
        verify(localRepo).create(snippet);
        verify(index).add(snippet);
    }

    @Test
    @DisplayName("updates an existing snippet on the index after updating the corresponding file")
    void updateSnippet() throws SnippetNotExistsException {
        Snippet snippet = testSnippet();
        repository.update(snippet);
        verify(localRepo).update(snippet);
        verify(index).update(snippet);
    }

    @Test
    @DisplayName("removes a snippet from the index after deleting it from the file system")
    void deleteSnippet() throws SnippetNotExistsException {
        repository.delete(SNIPPET_ID);
        verify(localRepo).delete(SNIPPET_ID);
        verify(index).remove(SNIPPET_ID);
    }

    private Snippet testSnippet() {
        return Snippet.builder().id(SNIPPET_ID).build();
    }
}