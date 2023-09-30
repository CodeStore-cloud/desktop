package cloud.codestore.core.api.snippets;

import cloud.codestore.core.Language;
import cloud.codestore.core.Snippet;
import cloud.codestore.core.SnippetNotExistsException;
import cloud.codestore.core.usecases.deletesnippet.DeleteSnippet;
import cloud.codestore.core.usecases.readsnippet.ReadSnippet;
import cloud.codestore.jsonapi.document.JsonApiDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DeleteSnippetController.class)
@Import({DeleteSnippetController.class})
@DisplayName("DELETE /snippets/{snippetId}")
class DeleteSnippetTest extends SnippetControllerTest {
    @MockBean
    private ReadSnippet readSnippetUseCase;
    @MockBean
    private DeleteSnippet deleteSnippetUseCase;

    @BeforeEach
    void setUp() throws SnippetNotExistsException {
        when(readSnippetUseCase.read(SNIPPET_ID)).thenReturn(testSnippet());
    }

    @Test
    @DisplayName("deletes the corresponding snippet")
    void deleteSnippet() throws Exception {
        DELETE(SNIPPET_URL).andExpect(status().isOk());

        verify(deleteSnippetUseCase).delete(SNIPPET_ID);
    }

    @Test
    @DisplayName("returns the deleted snippet")
    void returnDeletedSnippet() throws Exception {
        DELETE(SNIPPET_URL).andExpect(status().isOk())
                           .andExpect(content().contentType(JsonApiDocument.MEDIA_TYPE))
                           .andExpect(jsonPath("$.data.type", is("snippet")))
                           .andExpect(jsonPath("$.data.id", is(SNIPPET_ID)));
    }

    @Test
    @DisplayName("returns 404 if the snippet does not exist")
    void snippetNotExist() throws Exception {
        doThrow(SnippetNotExistsException.class).when(deleteSnippetUseCase).delete(SNIPPET_ID);
        DELETE(SNIPPET_URL).andExpect(status().isNotFound())
                           .andExpect(content().contentType(JsonApiDocument.MEDIA_TYPE))
                           .andExpect(jsonPath("$.errors").isArray())
                           .andExpect(jsonPath("$.errors.length()", is(1)))
                           .andExpect(jsonPath("$.errors[0].code", is("NOT_FOUND")))
                           .andExpect(jsonPath("$.errors[0].title", is("Not Found")))
                           .andExpect(jsonPath("$.errors[0].detail", is("The code snippet does not exist.")));
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
