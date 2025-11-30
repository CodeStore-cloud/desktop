package cloud.codestore.core.api.logging;

import cloud.codestore.core.api.DefaultLocale;
import cloud.codestore.core.api.TestConfig;
import cloud.codestore.core.api.root.RootController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@Import(TestConfig.class)
@ExtendWith({DefaultLocale.class, MockitoExtension.class})
@DisplayName("The request-logging filter")
class RequestLoggingFilterTest {
    @Mock
    private BufferedRequestAppender appender;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(RootController.class)
                                 .addFilters(new RequestLoggingFilter())
                                 .build();
    }

    @Test
    @DisplayName("logs successful requests")
    void logSuccessfulRequest() throws Exception {
        try (MockedStatic<BufferedRequestAppender> mock = mockStatic(BufferedRequestAppender.class)) {
            mock.when(BufferedRequestAppender::getInstance).thenReturn(appender);
            mockMvc.perform(get("/")).andExpect(status().isOk());
            verify(appender).flushSuccessfulRequest(anyString());
        }
    }

    @Test
    @DisplayName("logs failed requests")
    void logFailedRequest() throws Exception {
        try (MockedStatic<BufferedRequestAppender> mock = mockStatic(BufferedRequestAppender.class)) {
            mock.when(BufferedRequestAppender::getInstance).thenReturn(appender);
            mockMvc.perform(get("/asdf")).andExpect(status().isNotFound());
            verify(appender).flushFailedRequest(anyString());
        }
    }
}