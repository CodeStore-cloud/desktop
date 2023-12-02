package cloud.codestore.core.application;

import cloud.codestore.core.repositories.Directory;
import cloud.codestore.core.repositories.File;
import net.bytebuddy.utility.RandomString;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("The access-token writer")
class AccessTokenWriterTest {
    private static final String ACCESS_TOKEN = RandomString.make();

    @Mock
    private Directory binDirectory;
    @Mock
    private File accessTokenFile;

    @Test
    @DisplayName("writes the API root url to a file in the bin directory")
    void writeUrlToFile() {
        when(binDirectory.getFile(anyString())).thenReturn(accessTokenFile);
        Path filePath = mock(Path.class);
        when(accessTokenFile.path()).thenReturn(filePath);
        java.io.File nativeFile = mock(java.io.File.class);
        when(filePath.toFile()).thenReturn(nativeFile);

        new AccessTokenWriter(binDirectory, ACCESS_TOKEN).writeAccessToken();

        verify(accessTokenFile).write(ACCESS_TOKEN);
        verify(nativeFile).deleteOnExit();
    }
}