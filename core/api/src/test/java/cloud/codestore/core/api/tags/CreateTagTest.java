package cloud.codestore.core.api.tags;

import cloud.codestore.core.api.AbstractControllerTest;
import cloud.codestore.core.usecases.createtag.CreateTag;
import cloud.codestore.core.usecases.createtag.InvalidTagException;
import cloud.codestore.jsonapi.document.JsonApiDocument;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CreateTagController.class)
@Import(CreateTagController.class)
@DisplayName("POST /tags")
class CreateTagTest extends AbstractControllerTest {
    @MockBean
    private CreateTag createTagUseCase;

    @Test
    @DisplayName("creates the given tag")
    void createTag() throws Exception {
        mockMvc.perform(postRequest())
               .andExpect(status().isCreated())
               .andExpect(content().contentType(JsonApiDocument.MEDIA_TYPE))
               .andExpect(header().string(HttpHeaders.LOCATION, "http://localhost:8080/tags/test-tag"))
               .andExpect(content().json("""
                       {
                           "data": {
                               "type": "tag",
                               "id": "test-tag",
                               "attributes": {
                                   "name": "test-tag"
                               },
                               "relationships": {
                                   "snippets": {
                                       "links": {
                                           "related": "http://localhost:8080/snippets?filter%5Btags%5D=test-tag"
                                       }
                                   }
                               },
                               "links": {
                                   "self": "http://localhost:8080/tags/test-tag"
                               }
                           }
                       }"""));

        verify(createTagUseCase).create("test-tag");
    }

    @Test
    @DisplayName("returns an error if the tag is invalid")
    void invalidSnippet() throws Exception {
        var exception = mock(InvalidTagException.class);
        when(exception.getMessage()).thenReturn("validation message");
        doThrow(exception).when(createTagUseCase).create(anyString());

        mockMvc.perform(postRequest()).andExpect(status().isBadRequest());
    }

    private MockHttpServletRequestBuilder postRequest() {
        return post("/tags")
                .contentType(JsonApiDocument.MEDIA_TYPE)
                .content("""
                        {
                            "data": {
                                "type": "tag",
                                "attributes": {
                                    "name": "test-tag"
                                }
                            }
                        }""");
    }
}
