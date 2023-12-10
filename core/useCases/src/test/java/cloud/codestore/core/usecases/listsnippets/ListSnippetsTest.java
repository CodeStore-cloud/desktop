package cloud.codestore.core.usecases.listsnippets;

import cloud.codestore.core.Snippet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.stream.Stream;

import static cloud.codestore.core.usecases.listsnippets.SortProperties.SnippetProperty.RELEVANCE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("The list-snippets use case")
class ListSnippetsTest {
    @Mock
    private ReadSnippetsQuery query;
    private ListSnippets useCase;

    @BeforeEach
    void setUp() {
        useCase = new ListSnippets(query);
    }

    @Test
    @DisplayName("returns code snippets from the repository")
    void returnAllSnippets() {
        var search = "dummy search query";
        var filter = new FilterProperties();
        var sort = new SortProperties();
        var expectedResult = snippets();
        when(query.readSnippets(search, filter, sort)).thenReturn(expectedResult);

        var snippets = useCase.list(search, filter, sort);

        assertThat(snippets).isSameAs(expectedResult);
        verify(query).readSnippets(search, filter, sort);
    }

    @Test
    @DisplayName("sorts the code snippets by creation time by default")
    void defaultSorting() {
        var sortQuery = "";
        var filterProperties = new FilterProperties();
        useCase.list(sortQuery, filterProperties, null);
        verify(query).readSnippets(sortQuery, filterProperties, new SortProperties());
    }

    @Test
    @DisplayName("sorts the code snippets by relevance if a search query is provided but no sorting is defined")
    void sortByRelevance() {
        var sortQuery = "sort query";
        var filterProperties = new FilterProperties();
        useCase.list(sortQuery, filterProperties, null);
        verify(query).readSnippets(sortQuery, filterProperties, new SortProperties(RELEVANCE, true));
    }

    private List<Snippet> snippets() {
        return Stream.of(1, 2, 3, 4, 5)
                     .map(id -> Snippet.builder().id(String.valueOf(id)).build())
                     .toList();
    }
}