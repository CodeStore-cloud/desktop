package cloud.codestore.client.application;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("The async file reader")
class AsyncFileReaderTest {
    private static final String FILE_CONTENT = "Hello, World!";

    private Path tempDirectory;
    private Path testFile;
    private AsyncFileReader fileReader;

    @BeforeEach
    void setUp() throws IOException {
        tempDirectory = Files.createTempDirectory("AsyncFileReaderTest");
        testFile = tempDirectory.resolve("test-file.txt");
        fileReader = new AsyncFileReader();
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(testFile);
        Files.deleteIfExists(tempDirectory);
    }

    @Test
    @DisplayName("reads an existing file synchronously")
    void readsExistingFile() throws ExecutionException, InterruptedException, IOException {
        Files.writeString(testFile, FILE_CONTENT);

        CompletableFuture<String> future = fileReader.readFile(testFile);

        assertThat(future.isDone()).isTrue();
        assertThat(future.get()).isEqualTo(FILE_CONTENT);
    }

    @Test
    @DisplayName("waits for a non-existing file to be created")
    void waitsForNonExistingFile() throws IOException, ExecutionException, InterruptedException {
        CompletableFuture<String> future = fileReader.readFile(testFile);
        assertThat(future.isDone()).isFalse();

        Files.writeString(testFile, FILE_CONTENT);

        assertThat(future.get()).isEqualTo(FILE_CONTENT);
    }
}
