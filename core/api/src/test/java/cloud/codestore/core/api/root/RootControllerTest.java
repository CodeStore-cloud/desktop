package cloud.codestore.core.api.root;

import cloud.codestore.core.api.TestConfig;
import cloud.codestore.core.api.UriFactoryInitializer;
import cloud.codestore.jsonapi.document.JsonApiDocument;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(RootController.class)
@Import({TestConfig.class, RootController.class})
@ExtendWith(UriFactoryInitializer.class)
@DisplayName("GET /")
class RootControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("returns the core resource")
    void getRootResource() throws Exception {
        String responseBody = mockMvc.perform(get("/"))
                                     .andExpect(status().isOk())
                                     .andExpect(content().contentType(JsonApiDocument.MEDIA_TYPE))
                                     .andReturn().getResponse().getContentAsString();

        assertThat(responseBody).isEqualToIgnoringWhitespace("""
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
                      "apiVersion": "1.0",
                      "name": "{CodeStore} Core"
                    },
                    "links": {
                      "self": "http://localhost:8080"
                    }
                  }
                }""");
    }
}