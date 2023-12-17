package cloud.codestore.core.usecases.listsnippets;

import cloud.codestore.core.Snippet;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.Stream;

import static cloud.codestore.core.usecases.listsnippets.SortProperties.SnippetProperty.RELEVANCE;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("The list-snippets use case")
class ListSnippetsTest {
    private static final int SNIPPET_COUNT = 123;
    private static final int TOTAL_PAGES = 3;

    @Mock
    private ReadSnippetsQuery readSnippetsQuery;
    private ListSnippets useCase;

    @BeforeEach
    void setUp() {
        useCase = new ListSnippets(readSnippetsQuery);

        var searchResult = new SearchResult(SNIPPET_COUNT, snippets());
        when(readSnippetsQuery.readSnippets(any(), any(), any())).thenReturn(searchResult);
    }

    @Test
    @DisplayName("returns code snippets from the repository")
    void returnAllSnippets() throws PageNotExistsException {
        var search = "dummy search query";
        var filter = new FilterProperties();
        var sort = new SortProperties();

        var page = useCase.list(search, filter, sort, 1);

        Snippet[] expectedSnippets = snippets().limit(50).toArray(Snippet[]::new);
        assertThat(page.snippets()).containsExactly(expectedSnippets);
        assertThat(page.totalPages()).isEqualTo(3);
        verify(readSnippetsQuery).readSnippets(search, filter, sort);
    }

    @Test
    @DisplayName("sorts the code snippets by creation time by default")
    void defaultSorting() throws PageNotExistsException {
        var sortQuery = "";
        var filterProperties = new FilterProperties();
        useCase.list(sortQuery, filterProperties, null, 1);
        verify(readSnippetsQuery).readSnippets(sortQuery, filterProperties, new SortProperties());
    }

    @Test
    @DisplayName("sorts the code snippets by relevance if a search query is provided but no sorting is defined")
    void sortByRelevance() throws PageNotExistsException {
        var sortQuery = "sort query";
        var filterProperties = new FilterProperties();
        useCase.list(sortQuery, filterProperties, null, 1);
        verify(readSnippetsQuery).readSnippets(sortQuery, filterProperties, new SortProperties(RELEVANCE, true));
    }

    @Test
    @DisplayName("returns the snippets of the corresponding page")
    void respectPage() throws PageNotExistsException {
        SnippetListPage page = useCase.list("", new FilterProperties(), null, 3);

        Snippet[] expectedSnippets = snippets().skip(100).limit(50).toArray(Snippet[]::new);
        assertThat(page.snippets()).containsExactly(expectedSnippets);
    }

    @Nested
    @DisplayName("throws a PageNotExistsException")
    class PageNotExists {
        @Test
        @DisplayName("if the page number is less than 1")
        void pageLessThan1() {
            assertThatNoException().isThrownBy(listSnippets(1));
            assertThatThrownBy(listSnippets(0))
                    .isInstanceOf(PageNotExistsException.class);
        }

        @Test
        @DisplayName("if the page number is greater than the total pages available")
        void pageGreaterThanTotalPages() {
            assertThatNoException().isThrownBy(listSnippets(TOTAL_PAGES));
            assertThatThrownBy(listSnippets(TOTAL_PAGES + 1))
                    .isInstanceOf(PageNotExistsException.class);
        }

        private ThrowableAssert.ThrowingCallable listSnippets(int page) {
            return () -> useCase.list("", new FilterProperties(), null, page);
        }
    }

    private Stream<Snippet> snippets() {
        String[] snippetIds = new String[SNIPPET_COUNT];
        for (int i = 0; i < SNIPPET_COUNT; i++)
            snippetIds[i] = String.valueOf(i + 1);

        return Stream.of(snippetIds)
                     .map(id -> Snippet.builder().id(String.valueOf(id)).build());
    }
}