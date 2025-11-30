package cloud.codestore.core.api.logging;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.AbstractConfiguration;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.message.SimpleMessage;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("The BufferedRequestAppender")
class BufferedRequestAppenderTest {
    private static final String DELEGATE_NAME = "TestDelegate";
    private static final String REQUEST_ID = "requestId";
    private static final String DEBUG_MESSAGE = "debug message";
    private static final String INFO_MESSAGE = "info message";
    private static final String ERROR_MESSAGE = "error message";

    @Mock
    private Appender delegate;
    private BufferedRequestAppender appender;

    @BeforeEach
    void setUp() {
        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        Configuration config = context.getConfiguration();

        when(delegate.getName()).thenReturn(DELEGATE_NAME);
        config.addAppender(delegate);

        AppenderRef[] refs = new AppenderRef[]{AppenderRef.createAppenderRef(DELEGATE_NAME, null, null)};
        appender = BufferedRequestAppender.createAppender("BufferedRequestAppender", refs);
        appender.start();
    }

    @AfterEach
    void tearDown() {
        MDC.clear();

        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        AbstractConfiguration config = (AbstractConfiguration) context.getConfiguration();
        config.removeAppender(DELEGATE_NAME);
    }

    @Test
    @DisplayName("delegates events without requestId immediately")
    void delegatesWithoutRequestId() {
        MDC.clear();
        appender.append(event(Level.INFO, INFO_MESSAGE));

        ArgumentCaptor<LogEvent> captor = ArgumentCaptor.forClass(LogEvent.class);
        verify(delegate).append(captor.capture());
        List<String> messages = messages(captor.getAllValues());
        assertThat(messages).containsExactly(INFO_MESSAGE);
    }

    @Nested
    @DisplayName("in case of a successful request")
    class SuccessfulRequest {
        @Test
        @DisplayName("logs only INFO and higher log statements")
        void flushSuccessfulFiltersOutDebug() {
            MDC.put("requestId", REQUEST_ID);

            appender.append(event(Level.DEBUG, DEBUG_MESSAGE));
            appender.append(event(Level.INFO, INFO_MESSAGE));
            appender.append(event(Level.ERROR, ERROR_MESSAGE));

            BufferedRequestAppender.getInstance().flushSuccessfulRequest(REQUEST_ID);

            ArgumentCaptor<LogEvent> captor = ArgumentCaptor.forClass(LogEvent.class);
            verify(delegate, times(2)).append(captor.capture());
            List<String> messages = messages(captor.getAllValues());
            assertThat(messages).containsExactly(INFO_MESSAGE, ERROR_MESSAGE);
        }
    }

    @Nested
    @DisplayName("in case of a failed request")
    class FailedRequest {
        @Test
        @DisplayName("logs all statements for failed requests")
        void flushFailedIncludesDebug() {
            MDC.put("requestId", REQUEST_ID);

            appender.append(event(Level.DEBUG, DEBUG_MESSAGE));
            appender.append(event(Level.INFO, INFO_MESSAGE));
            appender.append(event(Level.ERROR, ERROR_MESSAGE));

            BufferedRequestAppender.getInstance().flushFailedRequest(REQUEST_ID);

            ArgumentCaptor<LogEvent> captor = ArgumentCaptor.forClass(LogEvent.class);
            verify(delegate, times(3)).append(captor.capture());
            List<String> messages = messages(captor.getAllValues());
            assertThat(messages).containsExactly(DEBUG_MESSAGE, INFO_MESSAGE, ERROR_MESSAGE);
        }
    }

    private LogEvent event(Level level, String message) {
        return Log4jLogEvent.newBuilder()
                            .setLoggerName("test")
                            .setLevel(level)
                            .setMessage(new SimpleMessage(message))
                            .build();
    }

    private List<String> messages(List<LogEvent> events) {
        List<String> messages = new ArrayList<>();
        for (LogEvent logEvent : events) {
            messages.add(logEvent.getMessage().getFormattedMessage());
        }
        return messages;
    }
}
