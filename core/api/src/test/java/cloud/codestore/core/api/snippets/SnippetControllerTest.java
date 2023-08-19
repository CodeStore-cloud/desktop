package cloud.codestore.core.api.snippets;

import cloud.codestore.core.api.DefaultLocale;
import cloud.codestore.core.api.DummyWebServerInitializedEvent;
import cloud.codestore.core.api.ErrorHandler;
import cloud.codestore.core.api.TestConfig;
import cloud.codestore.core.usecases.readlanguage.ReadLanguage;
import cloud.codestore.jsonapi.document.JsonApiDocument;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@ExtendWith(DefaultLocale.class)
@WebMvcTest(AbstractSnippetController.class)
@Import({TestConfig.class, AbstractSnippetController.class, ErrorHandler.class})
@ExtendWith(DummyWebServerInitializedEvent.class)
class SnippetControllerTest {
    static final String SNIPPET_ID = UUID.randomUUID().toString();
    static final String SNIPPET_URL = "/snippets/" + SNIPPET_ID;

    @Autowired
    protected ObjectMapper objectMapper;
    @Autowired
    protected MockMvc mockMvc;

    @MockBean
    protected ReadLanguage readLanguageUseCase;

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
