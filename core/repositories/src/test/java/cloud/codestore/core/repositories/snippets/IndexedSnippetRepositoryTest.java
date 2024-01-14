package cloud.codestore.core.repositories.snippets;

import cloud.codestore.core.Snippet;
import cloud.codestore.core.SnippetNotExistsException;
import cloud.codestore.core.repositories.tags.TagRepository;
import cloud.codestore.core.usecases.listsnippets.FilterProperties;
import cloud.codestore.core.usecases.listsnippets.SortProperties;
import org.apache.lucene.search.SortField;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static cloud.codestore.core.usecases.listsnippets.SortProperties.SnippetProperty;
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
    @Mock
    private TagRepository tagRepository;
    private IndexedSnippetRepository repository;

    @BeforeEach
    void setUp() {
        repository = new IndexedSnippetRepository(index, localRepo, tagRepository);
    }

    @Test
    @DisplayName("indexes all snippets when created")
    @SuppressWarnings("unchecked")
    void indexAllSnippets() {
        Mockito.reset(index);
        var argument = ArgumentCaptor.forClass(Stream.class);
        var snippets = List.of(mock(Snippet.class), mock(Snippet.class), mock(Snippet.class));
        when(localRepo.readSnippets()).thenReturn(snippets.stream());

        new IndexedSnippetRepository(index, localRepo, tagRepository);

        verify(index).add(argument.capture());
        assertThat(argument.getValue().toList()).isEqualTo(snippets);
        verify(tagRepository, times(3)).add(any());
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

    @ParameterizedTest
    @MethodSource("sortParams")
    @DisplayName("sorts the requested snippets")
    void sortSnippets(SortProperties sortProperties, String expectedSnippetField, boolean expectedOrder) {
        var sortFieldArgument = ArgumentCaptor.forClass(SortField.class);
        when(index.query(any(), any())).thenReturn(Collections.emptyList());

        repository.readSnippets("", new FilterProperties(), sortProperties);

        verify(index).query(any(), sortFieldArgument.capture());
        SortField sortField = sortFieldArgument.getValue();
        assertThat(sortField.getField()).isEqualTo(expectedSnippetField);
        assertThat(sortField.getReverse()).isEqualTo(!expectedOrder);
    }

    private static Stream<Arguments> sortParams() {
        return Stream.of(
                Arguments.of(new SortProperties(SnippetProperty.RELEVANCE, true), null, true),
                Arguments.of(new SortProperties(SnippetProperty.TITLE, true), "title", true),
                Arguments.of(new SortProperties(SnippetProperty.TITLE, false), "title", false),
                Arguments.of(new SortProperties(SnippetProperty.CREATED, true), "created", true),
                Arguments.of(new SortProperties(SnippetProperty.CREATED, false), "created", false),
                Arguments.of(new SortProperties(SnippetProperty.MODIFIED, true), "modified", true),
                Arguments.of(new SortProperties(SnippetProperty.MODIFIED, false), "modified", false)
        );
    }

    private Snippet testSnippet() {
        return Snippet.builder().id(SNIPPET_ID).build();
    }
}