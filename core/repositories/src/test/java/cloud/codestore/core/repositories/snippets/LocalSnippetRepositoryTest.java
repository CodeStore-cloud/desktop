package cloud.codestore.core.repositories.snippets;

import cloud.codestore.core.Snippet;
import cloud.codestore.core.SnippetNotExistsException;
import cloud.codestore.core.repositories.Directory;
import cloud.codestore.core.repositories.File;
import cloud.codestore.core.usecases.listsnippets.FilterProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("The local snippet repository")
class LocalSnippetRepositoryTest {
    private static final String SNIPPET_ID = UUID.randomUUID().toString();
    private static final String SNIPPET_FILE_NAME = SNIPPET_ID + LocalSnippetRepository.JSON_FILE_EXTENSION;

    @Mock
    private File snippetFile;
    @Mock
    private Directory snippetDirectory;
    @Mock
    private SnippetReader snippetReader;
    @Mock
    private SnippetWriter snippetWriter;
    private LocalSnippetRepository repository;

    @BeforeEach
    void setUp() {
        lenient().when(snippetDirectory.getFile(SNIPPET_FILE_NAME)).thenReturn(snippetFile);
        repository = new LocalSnippetRepository(snippetDirectory, snippetReader, snippetWriter);
    }

    @Test
    @DisplayName("reads a snippet from a JSON file")
    void readSnippet() throws SnippetNotExistsException {
        when(snippetFile.exists()).thenReturn(true);
        when(snippetReader.read(snippetFile)).thenReturn(mock(Snippet.class));

        Snippet snippet = repository.read(SNIPPET_ID);

        assertThat(snippet).isNotNull();
        verify(snippetReader).read(snippetFile);
    }

    @Test
    @DisplayName("read all snippets from the file system")
    void readSnippets() {
        var files = List.of(mock(File.class), mock(File.class), mock(File.class));
        when(snippetDirectory.getFiles()).thenReturn(files);

        var snippets = repository.readSnippets(new FilterProperties(null));

        assertThat(snippets).isNotNull();
        assertThat(snippets).hasSize(files.size());
        verify(snippetReader, times(3)).read(any(File.class));
    }

    @Test
    @DisplayName("saves a new snippet in a file")
    void saveSnippet() {
        Snippet testSnippet = mock(Snippet.class);
        when(testSnippet.getId()).thenReturn(SNIPPET_ID);

        repository.create(testSnippet);
        verify(snippetWriter).write(testSnippet, snippetFile);
    }

    @Test
    @DisplayName("overrides the corresponding file when updating a code snippet")
    void updateSnippet() throws SnippetNotExistsException {
        Snippet testSnippet = mock(Snippet.class);
        when(testSnippet.getId()).thenReturn(SNIPPET_ID);

        repository.update(testSnippet);
        verify(snippetWriter).write(testSnippet, snippetFile);
    }

    @Test
    @DisplayName("deletes the corresponding file of a snippet")
    void deleteSnippet() throws SnippetNotExistsException {
        when(snippetFile.exists()).thenReturn(true);
        repository.delete(SNIPPET_ID);
        verify(snippetFile).delete();
    }

    @Test
    @DisplayName("verifies that snippet exists when reading, updating or deleting a code snippet")
    void checkSnippetExists() {
        Snippet testSnippet = mock(Snippet.class);
        when(testSnippet.getId()).thenReturn(SNIPPET_ID);
        when(snippetFile.exists()).thenReturn(false);

        assertThatThrownBy(() -> repository.read(SNIPPET_ID)).isInstanceOf(SnippetNotExistsException.class);
        assertThatThrownBy(() -> repository.update(testSnippet)).isInstanceOf(SnippetNotExistsException.class);
        assertThatThrownBy(() -> repository.delete(SNIPPET_ID)).isInstanceOf(SnippetNotExistsException.class);
    }
}