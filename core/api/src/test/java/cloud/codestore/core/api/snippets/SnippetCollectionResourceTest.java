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

import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.core.Every.everyItem;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SnippetController.class)
@Import({TestConfig.class, SnippetController.class})
@ExtendWith(DummyWebServerInitializedEvent.class)
@DisplayName("GET /snippets")
class SnippetCollectionResourceTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private ListSnippets listSnippetsUseCase;
    @MockBean
    private ReadSnippet readSnippetUseCase;

    @Test
    @DisplayName("returns all available snippets")
    void returnSnippetCollection() throws Exception {
        when(listSnippetsUseCase.list()).thenReturn(snippetList());
        mockMvc.perform(get("/snippets"))
               .andExpect(status().isOk())
               .andExpect(content().contentType(JsonApiDocument.MEDIA_TYPE))
               .andExpect(jsonPath("$.data").isArray())
               .andExpect(jsonPath("$.data.length()", is(5)))
               .andExpect(jsonPath("$.data[*].type", everyItem(is("snippet"))))
               .andExpect(jsonPath("$.data[*].attributes").exists())
               .andExpect(jsonPath("$.data[*].links.self").exists());
    }

    private List<Snippet> snippetList() {
        return Stream.of(1, 2, 3, 4, 5)
                     .map(id -> new SnippetBuilder().id(String.valueOf(id)).build())
                     .toList();
    }
}