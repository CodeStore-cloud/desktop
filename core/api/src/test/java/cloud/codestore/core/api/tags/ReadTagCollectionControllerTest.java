package cloud.codestore.core.api.tags;

import cloud.codestore.core.Snippet;
import cloud.codestore.core.api.DummyWebServerInitializedEvent;
import cloud.codestore.core.api.TestConfig;
import cloud.codestore.core.usecases.readsnippet.ReadSnippet;
import cloud.codestore.core.usecases.readtags.ReadTags;
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
import java.util.UUID;

import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReadTagCollectionController.class)
@Import({TestConfig.class, ReadTagCollectionController.class})
@ExtendWith(DummyWebServerInitializedEvent.class)
@DisplayName("GET /tags")
class ReadTagCollectionControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private ReadSnippet readSnippetUseCase;
    @MockBean
    private ReadTags readTagsUseCase;

    @Test
    @DisplayName("returns all available tags")
    void returnAllTags() throws Exception {
        when(readTagsUseCase.readTags()).thenReturn(List.of("tagA", "tagB", "tagC"));

        mockMvc.perform(get("/tags"))
               .andExpect(status().isOk())
               .andExpect(content().contentType(JsonApiDocument.MEDIA_TYPE))
               .andExpect(content().json("""
                       {
                           "data": [ {
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
                               }, {
                                   "type": "tag",
                                   "id": "tagB",
                                   "attributes": {
                                       "name": "tagB"
                                   },
                                   "relationships": {
                                       "snippets": {
                                           "links": {
                                               "related": "http://localhost:8080/snippets?filter%5Btags%5D=tagB"
                                           }
                                       }
                                   },
                                   "links": {
                                       "self": "http://localhost:8080/tags/tagB"
                                   }
                               }, {
                                   "type": "tag",
                                   "id": "tagC",
                                   "attributes": {
                                       "name": "tagC"
                                   },
                                   "relationships": {
                                       "snippets": {
                                           "links": {
                                               "related": "http://localhost:8080/snippets?filter%5Btags%5D=tagC"
                                           }
                                       }
                                   },
                                   "links": {
                                       "self": "http://localhost:8080/tags/tagC"
                                   }
                               }
                           ],
                           "links": {
                               "self": "http://localhost:8080/tags"
                           }
                       }"""));
    }

    @Test
    @DisplayName("returns the tags of a specific snippet")
    void returnSnippetTags() throws Exception {
        String snippetId = UUID.randomUUID().toString();
        Snippet snippet = Snippet.builder().id(snippetId).tags(List.of("tagA", "tagB", "tagC")).build();
        when(readSnippetUseCase.read(snippetId)).thenReturn(snippet);

        mockMvc.perform(get("/tags?filter[snippet]=" + snippetId))
               .andExpect(status().isOk())
               .andExpect(content().contentType(JsonApiDocument.MEDIA_TYPE))
               .andExpect(jsonPath("$.data").isArray())
               .andExpect(jsonPath("$.data.length()", is(3)));

        verify(readSnippetUseCase).read(snippetId);
    }
}