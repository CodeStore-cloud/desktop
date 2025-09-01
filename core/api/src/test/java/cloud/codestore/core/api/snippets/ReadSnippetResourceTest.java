package cloud.codestore.core.api.snippets;

import cloud.codestore.core.Language;
import cloud.codestore.core.Snippet;
import cloud.codestore.core.usecases.readsnippet.ReadSnippet;
import cloud.codestore.jsonapi.document.JsonApiDocument;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import java.util.Map;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.StringEndsWith.endsWith;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReadSnippetController.class)
@Import(ReadSnippetController.class)
@DisplayName("GET /snippets/{snippetId}")
class ReadSnippetResourceTest extends SnippetControllerTest {
    private static final String SNIPPET_URL = "http://localhost:8080" + SnippetControllerTest.SNIPPET_URL;

    @MockBean
    private ReadSnippet readSnippetUseCase;

    @Test
    @DisplayName("returns a single code snippet")
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
                        .andExpect(jsonPath("$.data.links.self", is(SNIPPET_URL)))
                        .andExpect(jsonPath("$.data.meta.operations").isArray())
                        .andExpect(jsonPath("$.data.meta.operations", hasSize(2)))
                        .andExpect(jsonPath("$.data.meta.operations", containsInAnyOrder(
                                Map.of("operation", "updateSnippet",
                                        "method", "PATCH",
                                        "href", SNIPPET_URL),
                                Map.of("operation", "deleteSnippet",
                                        "method", "DELETE",
                                        "href", SNIPPET_URL)
                        )));
    }

    private Snippet testSnippet() {
        return Snippet.builder()
                      .id(SNIPPET_ID)
                      .title("A simple test snippet")
                      .description("A snippet solely for this unit test.")
                      .code("System.out.println(\"Hello, World!\");")
                      .language(Language.JAVA)
                      .build();
    }
}
