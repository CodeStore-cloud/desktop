package cloud.codestore.core.api.snippets;

import cloud.codestore.core.Language;
import cloud.codestore.core.Snippet;
import cloud.codestore.core.SnippetBuilder;
import cloud.codestore.jsonapi.document.JsonApiDocument;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.StringEndsWith.endsWith;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("GET /snippets/{snippetId}")
class SnippetResourceTest extends SnippetControllerTest {
    @Test
    @DisplayName("returns a single programming languages")
    void returnLanguage() throws Exception {
        var snippet = testSnippet();
        var languagePath = "/languages/" + snippet.getLanguage().getId();
        when(readSnippetUseCase.read(anyString())).thenReturn(snippet);

        GET(SNIPPET_URL).andExpect(status().isOk())
                        .andExpect(content().contentType(JsonApiDocument.MEDIA_TYPE))
                        .andExpect(jsonPath("$.data.type", is("snippet")))
                        .andExpect(jsonPath("$.data.id", is(snippet.getId())))
                        .andExpect(jsonPath("$.data.attributes.title", is(snippet.getTitle())))
                        .andExpect(jsonPath("$.data.attributes.description", is(snippet.getDescription())))
                        .andExpect(jsonPath("$.data.attributes.code", is(snippet.getCode())))
                        .andExpect(jsonPath("$.data.attributes.created", is(snippet.getCreated().toString())))
                        .andExpect(jsonPath("$.data.attributes.modified").doesNotExist())
                        .andExpect(jsonPath("$.data.relationships.language").exists())
                        .andExpect(jsonPath("$.data.relationships.language.links.related", endsWith(languagePath)))
                        .andExpect(jsonPath("$.data.links.self").exists());
    }

    private Snippet testSnippet() {
        return new SnippetBuilder()
                .id(SNIPPET_ID)
                .title("A simple test snippet")
                .description("A snippet solely for this unit test.")
                .code("System.out.println(\"Hello, World!\");")
                .language(Language.JAVA)
                .build();
    }
}
