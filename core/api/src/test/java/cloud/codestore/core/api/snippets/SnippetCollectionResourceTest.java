package cloud.codestore.core.api.snippets;

import cloud.codestore.core.Language;
import cloud.codestore.core.Snippet;
import cloud.codestore.core.usecases.listsnippets.FilterProperties;
import cloud.codestore.core.usecases.listsnippets.ListSnippets;
import cloud.codestore.core.usecases.readlanguage.LanguageNotExistsException;
import cloud.codestore.core.usecases.readlanguage.ReadLanguage;
import cloud.codestore.jsonapi.document.JsonApiDocument;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.core.Every.everyItem;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReadSnippetCollectionController.class)
@Import(ReadSnippetCollectionController.class)
@DisplayName("GET /snippets")
class SnippetCollectionResourceTest extends SnippetControllerTest {
    @MockBean
    private ListSnippets listSnippetsUseCase;
    @MockBean
    private ReadLanguage readLanguageUseCase;

    @Test
    @DisplayName("returns all available snippets")
    void returnSnippetCollection() throws Exception {
        when(listSnippetsUseCase.list(any())).thenReturn(snippetList());
        GET("/snippets").andExpect(status().isOk())
                        .andExpect(content().contentType(JsonApiDocument.MEDIA_TYPE))
                        .andExpect(jsonPath("$.data").isArray())
                        .andExpect(jsonPath("$.data.length()", is(5)))
                        .andExpect(jsonPath("$.data[*].type", everyItem(is("snippet"))))
                        .andExpect(jsonPath("$.data[*].attributes").exists())
                        .andExpect(jsonPath("$.data[*].links.self").exists());
    }

    @Nested
    @DisplayName("with language filter")
    class FilterByLanguage {
        @Test
        @DisplayName("returns all snippets of the specified programming language")
        void filterByLanguage() throws Exception {
            when(readLanguageUseCase.read(10)).thenReturn(Language.JAVA);
            when(listSnippetsUseCase.list(any())).thenReturn(snippetList());
            ArgumentCaptor<FilterProperties> argument = ArgumentCaptor.forClass(FilterProperties.class);

            GET("/snippets?filter[language]=10").andExpect(status().isOk());

            verify(listSnippetsUseCase).list(argument.capture());
            Assertions.assertThat(argument.getValue().language()).isEqualTo(Language.JAVA);
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

    private List<Snippet> snippetList() {
        return Stream.of(1, 2, 3, 4, 5)
                     .map(id -> Snippet.builder().id(String.valueOf(id)).build())
                     .toList();
    }
}