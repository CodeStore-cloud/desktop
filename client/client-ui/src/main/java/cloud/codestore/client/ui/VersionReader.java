package cloud.codestore.client.ui;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.Attributes;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

/**
 * Reads the version of the application from the manifest file.
 */
class VersionReader {
    private static final Logger LOGGER = LogManager.getLogger(VersionReader.class);
    private static final String IMPLEMENTATION_VERSION = "Implementation-Version";

    @Nonnull
    String readVersion() {
        String version = "";

        try {
            Path jarPath = Path.of(getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
            try (JarInputStream jar = new JarInputStream(Files.newInputStream(jarPath))) {
                Manifest manifest = jar.getManifest();
                Attributes attributes = manifest.getMainAttributes();

                if (attributes != null && attributes.containsKey(new Attributes.Name(IMPLEMENTATION_VERSION))) {
                    version = attributes.getValue(IMPLEMENTATION_VERSION);
                }
            }
        } catch (Exception exception) {
            LOGGER.warn("Unable to read version from manifest file.", exception);
        }

        return version;
    }
}
