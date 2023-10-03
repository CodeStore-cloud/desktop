package cloud.codestore.core.api.snippets;

import cloud.codestore.core.api.AbstractControllerTest;
import cloud.codestore.jsonapi.document.JsonApiDocument;
import org.springframework.test.web.servlet.ResultActions;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

class SnippetControllerTest extends AbstractControllerTest {
    static final String SNIPPET_ID = UUID.randomUUID().toString();
    static final String SNIPPET_URL = "/snippets/" + SNIPPET_ID;

    ResultActions GET(String path) throws Exception {
        return mockMvc.perform(get(path));
    }

    ResultActions POST(String path, String requestBody) throws Exception {
        return mockMvc.perform(post(path).content(requestBody).contentType(JsonApiDocument.MEDIA_TYPE));
    }

    ResultActions PATCH(String path, String requestBody) throws Exception {
        return mockMvc.perform(patch(path).content(requestBody).contentType(JsonApiDocument.MEDIA_TYPE));
    }

    ResultActions PATCHviaPOST(String path, String requestBody) throws Exception {
        return mockMvc.perform(post(path)
                .content(requestBody)
                .contentType(JsonApiDocument.MEDIA_TYPE)
                .header("X-HTTP-Method-Override", "PATCH"));
    }

    ResultActions DELETE(String path) throws Exception {
        return mockMvc.perform(delete(path));
    }
}
