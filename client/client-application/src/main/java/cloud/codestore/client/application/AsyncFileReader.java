package cloud.codestore.client.application;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Reads a file from the file system.
 * If the file does not exist, this class waits until the file was created.
 */
class AsyncFileReader {
    private static final int BUSY_WAITING_INTERVAL = 200;

    private ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();

    CompletableFuture<String> readFile(@Nonnull Path file) {
        if (Files.exists(file)) {
            String content = readContent(file);
            return CompletableFuture.completedFuture(content);
        } else {
            return CompletableFuture.supplyAsync(() -> {
                waitForFile(file);
                return readContent(file);
            }, executorService);
        }
    }

    private String readContent(Path file) {
        try {
            return Files.readString(file);
        } catch (IOException ioException) {
            throw new RuntimeException("Unable to read file " + file, ioException);
        }
    }

    private void waitForFile(Path file) {
        try {
            while (Files.notExists(file)) {
                Thread.sleep(BUSY_WAITING_INTERVAL);
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }
}
