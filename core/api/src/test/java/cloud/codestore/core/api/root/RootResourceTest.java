package cloud.codestore.core.api.root;

import cloud.codestore.core.api.DummyWebServerInitializedEvent;
import cloud.codestore.core.api.TestConfig;
import cloud.codestore.jsonapi.document.JsonApiDocument;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(RootController.class)
@Import({TestConfig.class, RootController.class})
@ExtendWith(DummyWebServerInitializedEvent.class)
@DisplayName("GET /")
class RootResourceTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("returns the core resource")
    void getRootResource() throws Exception {
        mockMvc.perform(get("/"))
               .andExpect(status().isOk())
               .andExpect(content().contentType(JsonApiDocument.MEDIA_TYPE))
               .andExpect(content().json("""
                       {
                         "jsonapi": {
                           "version": "1.0",
                           "meta": {
                             "documentation": "https://jsonapi.org/format/1.0/"
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
                             }
                           },
                           "links": {
                             "self": "http://localhost:8080"
                           }
                         }
                       }"""));
    }
}