package cloud.codestore.core.api.tags;

import cloud.codestore.core.api.DummyWebServerInitializedEvent;
import cloud.codestore.core.api.ErrorHandler;
import cloud.codestore.core.api.TestConfig;
import cloud.codestore.core.usecases.readtags.ReadTags;
import cloud.codestore.jsonapi.document.JsonApiDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReadTagController.class)
@Import({TestConfig.class, ReadTagController.class, ErrorHandler.class})
@ExtendWith(DummyWebServerInitializedEvent.class)
@DisplayName("GET /tags/{tagId}")
class ReadTagControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private ReadTags readTagsUseCase;

    @BeforeEach
    void setUp() {
        when(readTagsUseCase.readTags()).thenReturn(List.of("tagA", "tagB", "tagC"));
    }

    @Test
    @DisplayName("returns the tag with the given ID")
    void returnAllTags() throws Exception {
        mockMvc.perform(get("/tags/tagA"))
               .andExpect(status().isOk())
               .andExpect(content().contentType(JsonApiDocument.MEDIA_TYPE))
               .andExpect(content().json("""
                       {
                           "data": {
                               "type": "tag",
                               "id": "tagA",
                               "attributes": {
                                   "name": "tagA"
                               },
                               "relationships": {
                                   "snippets": {
                                       "links": {
                                           "related": "http://localhost:8080/snippets?filter%5Btags%5D=tagA"
                                       }
                                   }
                               },
                               "links": {
                                   "self": "http://localhost:8080/tags/tagA"
                               }
                           }
                       }"""));
    }

    @Test
    @DisplayName("returns 404 if the tag does not exist")
    void tagNotFound() throws Exception {
        mockMvc.perform(get("/tags/tagD"))
               .andExpect(status().isNotFound())
               .andExpect(content().contentType(JsonApiDocument.MEDIA_TYPE));
    }
}