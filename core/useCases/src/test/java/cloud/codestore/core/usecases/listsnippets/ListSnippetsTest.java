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

import java.util.List;
import java.util.stream.Stream;

import static cloud.codestore.core.usecases.listsnippets.SortProperties.SnippetProperty.RELEVANCE;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("The list-snippets use case")
class ListSnippetsTest {
    private static final int SNIPPET_COUNT = 123;
    private static final int TOTAL_PAGES = 3;

    @Mock
    private ReadSnippetsQuery readSnippetsQuery;
    @Mock
    private CountSnippetsQuery countSnippetsQuery;
    private ListSnippets useCase;

    @BeforeEach
    void setUp() {
        when(countSnippetsQuery.getSnippetCount()).thenReturn(SNIPPET_COUNT);
        useCase = new ListSnippets(readSnippetsQuery, countSnippetsQuery);
    }

    @Test
    @DisplayName("returns code snippets from the repository")
    void returnAllSnippets() throws PageNotExistsException {
        var search = "dummy search query";
        var filter = new FilterProperties();
        var sort = new SortProperties();
        var expectedResult = snippets();
        when(readSnippetsQuery.readSnippets(search, filter, sort)).thenReturn(expectedResult);

        var snippets = useCase.list(search, filter, sort, 1);

        assertThat(snippets.snippets()).isSameAs(expectedResult);
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

    private List<Snippet> snippets() {
        return Stream.of(1, 2, 3, 4, 5)
                     .map(id -> Snippet.builder().id(String.valueOf(id)).build())
                     .toList();
    }
}