package cloud.codestore.client.ui;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Reads the version of the application from the corresponding properties file.
 */
class VersionReader {
    private static final Logger LOGGER = LogManager.getLogger(VersionReader.class);

    @Nonnull
    String readVersion() {
        Properties properties = new Properties();
        try(InputStream in = getClass().getResourceAsStream("/version.properties")) {
            properties.load(in);
            return properties.getProperty("version", "");
        } catch (IOException exception) {
            LOGGER.warn("Unable to read version from properties file.", exception);
            return "";
        }
    }
}
