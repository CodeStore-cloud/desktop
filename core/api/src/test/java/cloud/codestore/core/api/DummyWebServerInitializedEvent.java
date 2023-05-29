package cloud.codestore.core.api;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.boot.web.server.WebServer;
import org.springframework.boot.web.servlet.context.ServletWebServerInitializedEvent;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DummyWebServerInitializedEvent implements BeforeAllCallback {
    private static final int TEST_PORT = 8080;

    @Override
    public void beforeAll(ExtensionContext extensionContext) {
        var eventMock = mock(ServletWebServerInitializedEvent.class);
        var webServerMock = mock(WebServer.class);
        when(eventMock.getWebServer()).thenReturn(webServerMock);
        when(webServerMock.getPort()).thenReturn(TEST_PORT);

        new UriFactory().onApplicationEvent(eventMock);
    }
}
