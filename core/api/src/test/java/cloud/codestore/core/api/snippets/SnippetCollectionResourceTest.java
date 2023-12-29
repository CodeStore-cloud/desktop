package cloud.codestore.core.api.snippets;

import cloud.codestore.core.Snippet;
import cloud.codestore.core.usecases.listsnippets.*;
import cloud.codestore.jsonapi.document.JsonApiDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static cloud.codestore.core.usecases.listsnippets.SortProperties.SnippetProperty;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.core.Every.everyItem;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReadSnippetCollectionController.class)
@Import(ReadSnippetCollectionController.class)
@DisplayName("GET /snippets")
class SnippetCollectionResourceTest extends SnippetControllerTest {
    private static final int PAGE_NUMBER = 12;
    private static final int TOTAL_PAGES = 15;

    @MockBean
    private ListSnippets listSnippetsUseCase;

    @BeforeEach
    void setUp() throws PageNotExistsException {
        var page = new SnippetListPage(PAGE_NUMBER, TOTAL_PAGES, snippetList());
        lenient().when(listSnippetsUseCase.list(any(), any(), any(), anyInt())).thenReturn(page);
    }

    @Test
    @DisplayName("returns all available snippets")
    void returnSnippetCollection() throws Exception {
        GET("/snippets")
                .andExpect(status().isOk())
                .andExpect(content().contentType(JsonApiDocument.MEDIA_TYPE))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()", is(5)))
                .andExpect(jsonPath("$.data[*].type", everyItem(is("snippet"))))
                .andExpect(jsonPath("$.data[*].attributes").exists())
                .andExpect(jsonPath("$.data[*].links.self").exists());

        verify(listSnippetsUseCase).list(eq(""), eq(new FilterProperties()), isNull(), eq(1));
    }

    @Test
    @DisplayName("filters the snippets by programming language")
    void filterByLanguage() throws Exception {
        GET("/snippets?filter[language]=Java").andExpect(status().isOk());

        var filterProperties = new FilterProperties("Java", null);
        verify(listSnippetsUseCase).list(any(), eq(filterProperties), any(), anyInt());
    }

    @Test
    @DisplayName("filters the snippets by tags")
    void filterByTags() throws Exception {
        var argument = ArgumentCaptor.forClass(FilterProperties.class);

        GET("/snippets?filter[tags]=TagA,TagB,TagC").andExpect(status().isOk());

        verify(listSnippetsUseCase).list(any(), argument.capture(), any(), anyInt());
        assertThat(argument.getValue().tags()).containsExactlyInAnyOrder("TagA", "TagB", "TagC");
    }

    @Nested
    @DisplayName("with sort parameter")
    class Sort {
        @Test
        @DisplayName("sorts snippets by title")
        void sortByTitle() throws Exception {
            GET("/snippets?sort=title").andExpect(status().isOk());
            var expectedSortProperties = new SortProperties(SnippetProperty.TITLE, true);
            verify(listSnippetsUseCase).list(any(), any(), eq(expectedSortProperties), anyInt());

            GET("/snippets?sort=-title").andExpect(status().isOk());
            expectedSortProperties = new SortProperties(SnippetProperty.TITLE, false);
            verify(listSnippetsUseCase).list(any(), any(), eq(expectedSortProperties), anyInt());
        }

        @Test
        @DisplayName("sorts snippets by creation time")
        void sortByCreationTime() throws Exception {
            GET("/snippets?sort=created").andExpect(status().isOk());
            SortProperties expectedSortProperties = new SortProperties(SnippetProperty.CREATED, true);
            verify(listSnippetsUseCase).list(any(), any(), eq(expectedSortProperties), anyInt());

            GET("/snippets?sort=-created").andExpect(status().isOk());
            expectedSortProperties = new SortProperties(SnippetProperty.CREATED, false);
            verify(listSnippetsUseCase).list(any(), any(), eq(expectedSortProperties), anyInt());
        }

        @Test
        @DisplayName("sorts snippets by modification time")
        void sortByModificationTime() throws Exception {
            GET("/snippets?sort=modified").andExpect(status().isOk());
            SortProperties expectedSortProperties = new SortProperties(SnippetProperty.MODIFIED, true);
            verify(listSnippetsUseCase).list(any(), any(), eq(expectedSortProperties), anyInt());

            GET("/snippets?sort=-modified").andExpect(status().isOk());
            expectedSortProperties = new SortProperties(SnippetProperty.MODIFIED, false);
            verify(listSnippetsUseCase).list(any(), any(), eq(expectedSortProperties), anyInt());
        }

        @Test
        @DisplayName("fails if the sort parameter is invalid")
        void failForInvalidSortParameter() throws Exception {
            GET("/snippets?sort=unknownProperty").andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("with search parameter")
    class Search {
        @Test
        @DisplayName("passes the parameter to the list-snippets use-case")
        void searchSnippets() throws Exception {
            GET("/snippets?searchQuery=test").andExpect(status().isOk());
            verify(listSnippetsUseCase).list(eq("test"), any(), isNull(), anyInt());
        }
    }

    @Nested
    @DisplayName("with page[number] parameter")
    class Pagination {
        @Test
        @DisplayName("reads the corresponding page of code snippets")
        void getPage() throws Exception {
            GET("/snippets?page[number]=" + PAGE_NUMBER).andExpect(status().isOk());
            verify(listSnippetsUseCase).list(eq(""), any(), isNull(), eq(PAGE_NUMBER));
        }

        @Test
        @DisplayName("returns all pagination links")
        void paginationLinks() throws Exception {
            GET("/snippets?page[number]=" + PAGE_NUMBER)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.links.first", is(url(1))))
                    .andExpect(jsonPath("$.links.last", is(url(TOTAL_PAGES))))
                    .andExpect(jsonPath("$.links.prev", is(url(PAGE_NUMBER - 1))))
                    .andExpect(jsonPath("$.links.next", is(url(PAGE_NUMBER + 1))));
        }

        @Test
        @DisplayName("omits the \"prev\" pagination link on the first page")
        void firstPage() throws Exception {
            GET("/snippets?page[number]=1")
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.links.first").exists())
                    .andExpect(jsonPath("$.links.last").exists())
                    .andExpect(jsonPath("$.links.prev").doesNotExist())
                    .andExpect(jsonPath("$.links.next").exists());
        }

        @Test
        @DisplayName("omits the \"next\" pagination link on the last page")
        void lastPage() throws Exception {
            GET("/snippets?page[number]=" + TOTAL_PAGES)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.links.first").exists())
                    .andExpect(jsonPath("$.links.last").exists())
                    .andExpect(jsonPath("$.links.prev").exists())
                    .andExpect(jsonPath("$.links.next").doesNotExist());
        }

        @Test
        @DisplayName("omits all pagination links if there is only one page")
        void onlyOnePage() throws Exception {
            var page = new SnippetListPage(1, 1, Collections.emptyList());
            lenient().when(listSnippetsUseCase.list(any(), any(), any(), anyInt())).thenReturn(page);

            GET("/snippets?page[number]=1")
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.links.first").doesNotExist())
                    .andExpect(jsonPath("$.links.last").doesNotExist())
                    .andExpect(jsonPath("$.links.prev").doesNotExist())
                    .andExpect(jsonPath("$.links.next").doesNotExist());
        }

        @Test
        @DisplayName("returns 404 if the client passes an invalid page number")
        void pageOutOfBounds() throws Exception {
            when(listSnippetsUseCase.list(any(), any(), any(), anyInt())).thenThrow(PageNotExistsException.class);
            GET("/snippets?page[number]=0").andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("returns 400 if the page number is not an integer")
        void invalidParameter() throws Exception {
            GET("/snippets?page[number]=notAnInteger").andExpect(status().isBadRequest());
        }

        private String url(int page) {
            return "http://localhost:8080/snippets?page%5Bnumber%5D=" + page;
        }
    }

    private List<Snippet> snippetList() {
        return Stream.of(1, 2, 3, 4, 5)
                     .map(id -> Snippet.builder().id(String.valueOf(id)).build())
                     .toList();
    }
}