package cloud.codestore.core.repositories.snippets;

import cloud.codestore.core.Snippet;
import cloud.codestore.core.SnippetNotExistsException;
import cloud.codestore.core.usecases.listsnippets.FilterProperties;
import org.apache.lucene.search.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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

    @Test
    @DisplayName("filters the snippets to read from the file system")
    @SuppressWarnings("unchecked")
    void filterSnippets() {
        FilterProperties filterProperties = mock(FilterProperties.class);
        when(index.query(any(Query.class))).thenReturn(Stream.of("1", "2", "3"));
        when(localRepo.readSnippets(any(Stream.class))).thenReturn(
                List.of(snippetWithId("1"), snippetWithId("2"), snippetWithId("3")));

        List<Snippet> snippets = repository.readSnippets(filterProperties);

        assertThat(snippets).containsExactly(snippetWithId("1"), snippetWithId("2"), snippetWithId("3"));
        verify(localRepo).readSnippets(any(Stream.class));
    }

    private Snippet testSnippet() {
        return Snippet.builder().id(SNIPPET_ID).build();
    }

    private Snippet snippetWithId(String id) {
        return Snippet.builder().id(id).build();
    }
}