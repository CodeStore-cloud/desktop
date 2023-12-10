package cloud.codestore.core.api.snippets;

import cloud.codestore.core.Language;
import cloud.codestore.core.Snippet;
import cloud.codestore.core.TagNotExistsException;
import cloud.codestore.core.usecases.listsnippets.FilterProperties;
import cloud.codestore.core.usecases.listsnippets.ListSnippets;
import cloud.codestore.core.usecases.listsnippets.SortProperties;
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
    @MockBean
    private ListSnippets listSnippetsUseCase;
    @MockBean
    private ReadLanguage readLanguageUseCase;
    @MockBean
    private ReadTags readTagsUseCase;

    @BeforeEach
    void setUp() {
        lenient().when(listSnippetsUseCase.list(any(), any(), any())).thenReturn(snippetList());
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

        verify(listSnippetsUseCase).list(eq(""), eq(new FilterProperties()), isNull());
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

            verify(listSnippetsUseCase).list(any(), argument.capture(), any());
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

            verify(listSnippetsUseCase).list(any(), argument.capture(), any());
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
            verify(listSnippetsUseCase).list(any(), any(), eq(new SortProperties(SnippetProperty.TITLE, true)));

            GET("/snippets?sort=-title").andExpect(status().isOk());
            verify(listSnippetsUseCase).list(any(), any(), eq(new SortProperties(SnippetProperty.TITLE, false)));
        }

        @Test
        @DisplayName("sorts snippets by creation time")
        void sortByCreationTime() throws Exception {
            GET("/snippets?sort=created").andExpect(status().isOk());
            verify(listSnippetsUseCase).list(any(), any(), eq(new SortProperties(SnippetProperty.CREATED, true)));

            GET("/snippets?sort=-created").andExpect(status().isOk());
            verify(listSnippetsUseCase).list(any(), any(), eq(new SortProperties(SnippetProperty.CREATED, false)));
        }

        @Test
        @DisplayName("sorts snippets by modification time")
        void sortByModificationTime() throws Exception {
            GET("/snippets?sort=modified").andExpect(status().isOk());
            verify(listSnippetsUseCase).list(any(), any(), eq(new SortProperties(SnippetProperty.MODIFIED, true)));

            GET("/snippets?sort=-modified").andExpect(status().isOk());
            verify(listSnippetsUseCase).list(any(), any(), eq(new SortProperties(SnippetProperty.MODIFIED, false)));
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
            verify(listSnippetsUseCase).list(eq("test"), any(), isNull());
        }
    }

    private List<Snippet> snippetList() {
        return Stream.of(1, 2, 3, 4, 5)
                     .map(id -> Snippet.builder().id(String.valueOf(id)).build())
                     .toList();
    }
}