package cloud.codestore.client.ui;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("The version reader")
class VersionReaderTest {
    private VersionReader reader = new VersionReader();

    @Test
    @DisplayName("reads the version from the version.properties file")
    void readFromProperties() {
        String version = reader.readVersion();
        assertThat(version).isEqualTo("2.0.0");
    }
}