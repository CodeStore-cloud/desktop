package cloud.codestore.core.api.security;

import cloud.codestore.core.api.DefaultLocale;
import cloud.codestore.core.api.TestConfig;
import cloud.codestore.core.api.root.RootController;
import cloud.codestore.core.usecases.synchronizesnippets.ExecutedSynchronizations;
import cloud.codestore.jsonapi.document.JsonApiDocument;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.bytebuddy.utility.RandomString;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@Import(TestConfig.class)
@ExtendWith({DefaultLocale.class, MockitoExtension.class})
@DisplayName("The access-token filter")
class AccessTokenFilterTest {
    private static final String ACCESS_TOKEN = RandomString.make();

    @Autowired
    private ObjectMapper objectMapper;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ExecutedSynchronizations executedSynchronizations = mock(ExecutedSynchronizations.class);
        lenient().when(executedSynchronizations.getOptionalInitialSynchronization()).thenReturn(Optional.empty());
        mockMvc = MockMvcBuilders.standaloneSetup(new RootController(executedSynchronizations))
                                 .addFilters(new AccessTokenFilter(ACCESS_TOKEN, objectMapper))
                                 .build();
    }

    @Nested
    @DisplayName("accepts requests")
    class AcceptRequest {
        @Test
        @DisplayName("that provide a valid token in the Authorization-Header")
        void validToken() throws Exception {
            mockMvc.perform(get("/").header(HttpHeaders.AUTHORIZATION, "Bearer " + ACCESS_TOKEN))
                   .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("rejects requests")
    class RejectRequest {
        @Test
        @DisplayName("that don't contain an Authorization-Header")
        void noHeader() throws Exception {
            mockMvc.perform(get("/"))
                   .andExpect(status().isUnauthorized())
                   .andExpect(content().contentType(JsonApiDocument.MEDIA_TYPE))
                   .andExpect(content().json("""
                           {
                             "errors" : [{
                               "code" : "ACCESS_DENIED",
                               "title" : "Access Denied",
                               "detail" : "No bearer token was transferred in the Authorization header."
                             }]
                           }"""));
        }

        @Test
        @DisplayName("that don't contain a \"Bearer\" token")
        void noBearerToken() throws Exception {
            mockMvc.perform(get("/").header(HttpHeaders.AUTHORIZATION, "Basic " + ACCESS_TOKEN))
                   .andExpect(status().isUnauthorized())
                   .andExpect(content().contentType(JsonApiDocument.MEDIA_TYPE))
                   .andExpect(content().json("""
                           {
                             "errors" : [{
                               "code" : "ACCESS_DENIED",
                               "title" : "Access Denied",
                               "detail" : "No bearer token was transferred in the Authorization header."
                             }]
                           }"""));
        }

        @Test
        @DisplayName("that don't contain a valid bearer token")
        void invalidToken() throws Exception {
            mockMvc.perform(get("/").header(HttpHeaders.AUTHORIZATION, "Bearer invalid_token"))
                   .andExpect(status().isUnauthorized())
                   .andExpect(content().contentType(JsonApiDocument.MEDIA_TYPE))
                   .andExpect(content().json("""
                           {
                             "errors" : [{
                               "code" : "ACCESS_DENIED",
                               "title" : "Access Denied",
                               "detail" : "The access token is invalid."
                             }]
                           }"""));
        }
    }
}