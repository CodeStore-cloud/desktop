package cloud.codestore.client.application;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("The root-url reader")
class RootUrlReaderTest {
    private static final String URL = "http://localhost:8080";

    private Path binDirectory;
    private Path rootUrlFile;

    @BeforeEach
    void setUp() throws IOException {
        binDirectory = Files.createTempDirectory("RootUrlReaderTest");
        rootUrlFile = binDirectory.resolve("core-api-url");
        Files.writeString(rootUrlFile, URL);
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(rootUrlFile);
        Files.deleteIfExists(binDirectory);
    }

    @Test
    @DisplayName("reads the URL from the given bin directory")
    void readFile() {
        String url = new RootUrlReader().readApiUrl(binDirectory);
        assertThat(url).isEqualTo(URL);
    }
}