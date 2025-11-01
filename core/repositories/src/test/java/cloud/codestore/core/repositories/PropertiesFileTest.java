package cloud.codestore.core.repositories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(DefaultLocale.class)
@DisplayName("A properties file")
class PropertiesFileTest {
    private static final String FILE_NAME = "test.properties";
    private static final String KEY = "test.key";
    private static final String VALUE = "test.value";

    @TempDir
    private Path testDir;
    private Path testFile;
    private Properties properties = new Properties();

    @BeforeEach
    void setUp() {
        properties.setProperty(KEY, VALUE);
    }

    private File notExistingFile() {
        return new File(testDir.resolve("notExistingFile.properties"));
    }

    @Nested
    @DisplayName("that exists")
    class ExistingFileTest {
        @BeforeEach
        void setUp() throws IOException {
            testFile = testDir.resolve(FILE_NAME);
            properties.store(Files.newOutputStream(testFile), null);
            assertThat(testFile).exists();
        }

        @Test
        @DisplayName("can be read")
        void readSuccessfully() throws RepositoryException {
            Properties readProperties = new File(testFile).readProperties();
            assertThat(readProperties).containsEntry(KEY, VALUE);
        }

        @Test
        @DisplayName("can be overridden")
        void overrideOnWrite() throws RepositoryException, IOException {
            String text = "old.key=old.value";
            Files.writeString(testFile, text);
            assertThat(Files.readString(testFile)).isEqualTo(text);

            new File(testFile).write(properties);

            assertThat(Files.readString(testFile)).contains(KEY + "=" + VALUE);
        }
    }

    @Nested
    @DisplayName("that does not exist")
    class NotExistingFileTest {
        @Test
        @DisplayName("cannot be read")
        void fileNotFound() {
            File file = notExistingFile();
            assertThatThrownBy(file::readProperties)
                    .isInstanceOf(RepositoryException.class)
                    .hasCauseInstanceOf(NoSuchFileException.class)
                    .hasMessageMatching("The file .+ does not exist\\.");
        }

        @Test
        @DisplayName("can be created")
        void createOnWrite() throws RepositoryException, IOException {
            testFile = testDir.resolve(FILE_NAME);
            assertThat(testFile).doesNotExist();

            new File(testFile).write(properties);

            assertThat(testFile).exists();
            assertThat(Files.readString(testFile)).contains(KEY + "=" + VALUE);
        }
    }
}