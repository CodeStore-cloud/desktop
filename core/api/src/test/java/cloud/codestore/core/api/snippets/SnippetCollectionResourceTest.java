package cloud.codestore.core.api.snippets;

import cloud.codestore.core.Language;
import cloud.codestore.core.Snippet;
import cloud.codestore.core.TagNotExistsException;
import cloud.codestore.core.usecases.listsnippets.*;
import cloud.codestore.core.usecases.readlanguage.LanguageNotExistsException;
import cloud.codestore.core.usecases.readlanguage.ReadLanguage;
import cloud.codestore.core.usecases.readtags.ReadTags;
import cloud.codestore.jsonapi.document.JsonApiDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.Set;
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
    @MockBean
    private ReadLanguage readLanguageUseCase;
    @MockBean
    private ReadTags readTagsUseCase;

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

    @Nested
    @DisplayName("with language filter")
    class FilterByLanguage {
        @Test
        @DisplayName("returns all snippets of the specified programming language")
        void filterByLanguage() throws Exception {
            when(readLanguageUseCase.read(10)).thenReturn(Language.JAVA);
            var argument = ArgumentCaptor.forClass(FilterProperties.class);

            GET("/snippets?filter[language]=10").andExpect(status().isOk());

            verify(listSnippetsUseCase).list(any(), argument.capture(), any(), anyInt());
            assertThat(argument.getValue().language()).isEqualTo(Language.JAVA);
        }

        @Test
        @DisplayName("fails if the referred programming language does not exist")
        void filterByLanguageInvalidId() throws Exception {
            when(readLanguageUseCase.read(999)).thenThrow(new LanguageNotExistsException());
            GET("/snippets?filter[language]=999").andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("fails if the provided ID is not an integer")
        void filterByLanguageNoInteger() throws Exception {
            GET("/snippets?filter[language]=java").andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("with tags filter")
    class FilterByTags {
        @Test
        @DisplayName("returns all snippets with the specified tags")
        void filterByTags() throws Exception {
            when(readTagsUseCase.readTags(any(String[].class))).thenReturn(Set.of("TagA", "TagB", "TagC"));
            var argument = ArgumentCaptor.forClass(FilterProperties.class);

            GET("/snippets?filter[tags]=TagA,TagB,TagC").andExpect(status().isOk());

            verify(listSnippetsUseCase).list(any(), argument.capture(), any(), anyInt());
            assertThat(argument.getValue().tags()).containsExactlyInAnyOrder("TagA", "TagB", "TagC");
        }

        @Test
        @DisplayName("fails if one of the referred tags does not exist")
        void filterByInvalidTag() throws Exception {
            when(readTagsUseCase.readTags(any(String[].class))).thenThrow(new TagNotExistsException("InvalidTag"));
            GET("/snippets?filter[tags]=InvalidTag").andExpect(status().isNotFound());
        }
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
                    .andExpect(jsonPath("$.links.prev").doesNotExist())
                    .andExpect(jsonPath("$.links.next").exists());
        }

        @Test
        @DisplayName("omits the \"next\" pagination link on the last page")
        void lastPage() throws Exception {
            GET("/snippets?page[number]=" + TOTAL_PAGES)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.links.prev").exists())
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