package cloud.codestore.core.application;

import cloud.codestore.core.api.UriFactory;
import cloud.codestore.core.repositories.Directory;
import cloud.codestore.core.repositories.File;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.server.WebServer;
import org.springframework.boot.web.servlet.context.ServletWebServerInitializedEvent;

import java.nio.file.Path;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("The root-url writer")
class RootUrlWriterTest {

    @Mock
    private Directory binDirectory;
    @Mock
    private File rootUrlFile;

    @BeforeAll
    static void beforeAll() {
        var eventMock = mock(ServletWebServerInitializedEvent.class);
        var webServerMock = mock(WebServer.class);
        when(eventMock.getWebServer()).thenReturn(webServerMock);
        when(webServerMock.getPort()).thenReturn(8080);

        new UriFactory().onApplicationEvent(eventMock);
    }

    @Test
    @DisplayName("writes the API root url to a file in the bin directory")
    void writeUrlToFile() {
        when(binDirectory.getFile(anyString())).thenReturn(rootUrlFile);
        Path filePath = mock(Path.class);
        when(rootUrlFile.path()).thenReturn(filePath);
        java.io.File nativeFile = mock(java.io.File.class);
        when(filePath.toFile()).thenReturn(nativeFile);


        new RootUrlWriter(binDirectory).writeRootUrl();

        verify(rootUrlFile).write("http://localhost:8080");
        verify(nativeFile).deleteOnExit();
    }
}