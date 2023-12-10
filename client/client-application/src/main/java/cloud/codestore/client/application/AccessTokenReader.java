package cloud.codestore.client.application;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Reads the access token to the {CodeStore} Core API.
 */
class AccessTokenReader {
    String readAccessToken(@Nonnull Path directory) {
        Path file = directory.resolve("core-api-access-token");
        try {
            return Files.readString(file);
        } catch (IOException ioException) {
            throw new IllegalStateException("Unable to read API access token from file " + file, ioException);
        }
    }
}
