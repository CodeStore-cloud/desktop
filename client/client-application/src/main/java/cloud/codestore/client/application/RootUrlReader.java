package cloud.codestore.client.application;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Reads the root URL to the {CodeStore} Core API.
 */
class RootUrlReader {
    String readApiUrl(@Nonnull Path binDirectory) {
        Path rootUrlFile = binDirectory.resolve("core-api-url").toAbsolutePath();
        try {
            return Files.readString(rootUrlFile);
        } catch (IOException ioException) {
            throw new IllegalStateException("Unable to read API URL from file " + rootUrlFile, ioException);
        }
    }
}
