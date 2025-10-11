package cloud.codestore.core.api.root;

import cloud.codestore.core.api.AbstractControllerTest;
import cloud.codestore.core.usecases.synchronizesnippets.SynchronizationProcess;
import cloud.codestore.jsonapi.document.JsonApiDocument;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RootController.class)
@Import(RootController.class)
@DisplayName("GET /")
class RootResourceTest extends AbstractControllerTest {
    @MockitoBean
    private SynchronizationProcess synchronizationProcess;

    @Test
    @DisplayName("returns the core resource")
    void getRootResource() throws Exception {
        when(synchronizationProcess.isSkipped()).thenReturn(true);
        mockMvc.perform(get("/"))
               .andExpect(status().isOk())
               .andExpect(content().contentType(JsonApiDocument.MEDIA_TYPE))
               .andExpect(content().json("""
                       {
                         "jsonapi": {
                           "version": "1.1",
                           "meta": {
                             "documentation": "https://jsonapi.org/format/1.1/"
                           }
                         },
                         "data": {
                           "type": "core",
                           "id": "1",
                           "attributes": {
                             "name" : "{CodeStore} Core",
                             "apiVersion" : "1.0",
                             "apiVersionHeader" : "X-API-Version",
                             "documentation" : "https://codestore.cloud/api-documentation"
                           },
                           "relationships": {
                             "languages": {
                               "links": {
                                 "related": "http://localhost:8080/languages"
                               }
                             },
                             "snippets": {
                               "links": {
                                 "related": "http://localhost:8080/snippets"
                               }
                             },
                             "tags": {
                               "links": {
                                 "related": "http://localhost:8080/tags"
                               }
                             }
                           },
                           "links": {
                             "self": "http://localhost:8080"
                           }
                         }
                       }"""));
    }

    @Test
    @DisplayName("contains a link to the initial synchronization if present")
    void linkInitialSynchronizationResource() throws Exception {
        when(synchronizationProcess.isSkipped()).thenReturn(false);

        mockMvc.perform(get("/"))
               .andExpect(status().isOk())
               .andExpect(content().contentType(JsonApiDocument.MEDIA_TYPE))
               .andExpect(jsonPath(
                       "$.data.relationships.synchronization.links.related",
                       is("http://localhost:8080/synchronizations/1")
               ));
    }
}