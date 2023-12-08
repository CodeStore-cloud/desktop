package cloud.codestore.client.application;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Reads the root URL to the {CodeStore} Core API.
 */
class RootUrlReader {
    String readApiUrl(@Nonnull Path directory) {
        Path file = directory.resolve("core-api-url").toAbsolutePath();
        try {
            return Files.readString(file);
        } catch (IOException ioException) {
            throw new IllegalStateException("Unable to read API URL from file " + file, ioException);
        }
    }
}
