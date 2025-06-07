package cloud.codestore.core.application.update;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Represents the Windows executable to install the application.
 */
class InstallerExecutable {
    private static final Logger LOGGER = LoggerFactory.getLogger(InstallerExecutable.class);

    private final Path file;

    InstallerExecutable(@Nonnull byte[] fileContent) throws IOException {
        Path tempDir = Files.createTempDirectory("codestore-update");
        file = tempDir.resolve("CodeStore.exe");
        Files.write(file, fileContent);
    }

    void execute() throws IOException {
        if (file != null && Files.exists(file)) {
            try {
                LOGGER.info("Executing {}", file);
                ProcessBuilder processBuilder = new ProcessBuilder(file.toString());
                processBuilder.start();
            } catch (IOException exception) {
                LOGGER.error("Failed to execute {}", file, exception);
                throw exception;
            }
        }
    }
}
