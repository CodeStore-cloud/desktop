package cloud.codestore.client.ui;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileInputStream;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * Reads the version of the application from the manifest file.
 */
class VersionReader {
    private static final Logger LOGGER = LogManager.getLogger(FxApplication.class);
    private static final String MANIFEST_FILE = "META-INF/MANIFEST.MF";
    private static final String IMPLEMENTATION_VERSION = "Implementation-Version";

    @Nonnull
    String readVersion() {
        String version = "";

        try {
            File manifestFile  = new File(MANIFEST_FILE);
            Manifest manifest = new Manifest(new FileInputStream(manifestFile));
            Attributes attributes = manifest.getMainAttributes();

            if (attributes != null && attributes.containsKey(new Attributes.Name(IMPLEMENTATION_VERSION))) {
                version = attributes.getValue(IMPLEMENTATION_VERSION);
            }
        } catch (Exception exception) {
            LOGGER.warn("Unable to read version from {}", MANIFEST_FILE, exception);
        }

        return version;
    }
}
