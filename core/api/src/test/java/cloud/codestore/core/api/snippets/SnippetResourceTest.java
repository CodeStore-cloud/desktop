package cloud.codestore.core.api.snippets;

import cloud.codestore.core.Snippet;
import cloud.codestore.core.SnippetBuilder;
import cloud.codestore.core.api.DummyWebServerInitializedEvent;
import cloud.codestore.core.api.TestConfig;
import cloud.codestore.core.usecases.listsnippets.ListSnippets;
import cloud.codestore.core.usecases.readsnippet.ReadSnippet;
import cloud.codestore.jsonapi.document.JsonApiDocument;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SnippetController.class)
@Import({TestConfig.class, SnippetController.class})
@ExtendWith(DummyWebServerInitializedEvent.class)
@DisplayName("GET /snippets/{snippetId}")
class SnippetResourceTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private ListSnippets listSnippetsUseCase;
    @MockBean
    private ReadSnippet readSnippetUseCase;

    @Test
    @DisplayName("returns a single programming languages")
    void returnLanguage() throws Exception {
        var snippet = testSnippet();
        when(readSnippetUseCase.read(anyString())).thenReturn(snippet);

        mockMvc.perform(get("/snippets/" + snippet.getId()))
               .andExpect(status().isOk())
               .andExpect(content().contentType(JsonApiDocument.MEDIA_TYPE))
               .andExpect(jsonPath("$.data.type", is("snippet")))
               .andExpect(jsonPath("$.data.id", is(snippet.getId())))
               .andExpect(jsonPath("$.data.attributes.title", is(snippet.getTitle())))
               .andExpect(jsonPath("$.data.attributes.description", is(snippet.getDescription())))
               .andExpect(jsonPath("$.data.attributes.code", is(snippet.getCode())))
               .andExpect(jsonPath("$.data.attributes.created", is(snippet.getCreated().toString())))
               .andExpect(jsonPath("$.data.attributes.modified").doesNotExist())
               .andExpect(jsonPath("$.data.links.self").exists());
    }

    private Snippet testSnippet() {
        return new SnippetBuilder()
                .id("1")
                .title("A simple test snippet")
                .description("A snippet solely for this unit test.")
                .code("System.out.println(\"Hello, World!\");")
                .build();
    }
}
