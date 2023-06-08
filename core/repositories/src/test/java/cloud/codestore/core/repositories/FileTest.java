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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(DefaultLocale.class)
@DisplayName("A file")
class FileTest {
    private static final String FILE_NAME = "test.txt";
    private static final String FILE_CONTENT = "FILE_CONTENT";

    @TempDir
    private Path testDir;
    private Path testFile;

    @Test
    @DisplayName("cannot be instantiated if the path is an existing directory")
    void failInstantiationIfDirectory() {
        assertThatThrownBy(() -> new File(testDir))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageEndingWith(" is a directory.");
    }

    @Test
    @DisplayName("has a name")
    void getName() {
        File file = new File(testDir.resolve(FILE_NAME));
        assertThat(file.getName()).isEqualTo("test.txt");
    }

    @Nested
    @DisplayName("that exists")
    class ExistingFileTest {
        @BeforeEach
        void setUp() throws IOException {
            testFile = testDir.resolve(FILE_NAME);
            Files.writeString(testFile, FILE_CONTENT);
        }

        @Test
        @DisplayName("returns true when calling exists()")
        void existsReturnsTrue() {
            assertThat(new File(testFile).exists()).isTrue();
        }

        @Test
        @DisplayName("can be read")
        void readSuccessfully() throws RepositoryException {
            String content = new File(testFile).read();
            assertThat(content).isEqualTo(FILE_CONTENT);
        }

        @Test
        @DisplayName("returns a fallback-content in case of an error")
        void readFallbackContent() {
            String fallbackContent = "Fallback-Content";
            String content = notExistingFile().readOrElse(fallbackContent);
            assertThat(content).isEqualTo(fallbackContent);
        }

        @Test
        @DisplayName("can be overridden")
        void overrideOnWrite() throws RepositoryException, IOException {
            assertThat(testFile).exists();
            String text = "old text";
            Files.writeString(testFile, text);
            assertThat(Files.readString(testFile)).isEqualTo(text);

            new File(testFile).write(FILE_CONTENT);

            assertThat(Files.readString(testFile)).isEqualTo(FILE_CONTENT);
        }

        @Test
        @DisplayName("creates not existing parent directories")
        void createParentDirectories() throws RepositoryException, IOException {
            Path subDir = testDir.resolve("subDir").resolve("subSubDir");
            assertThat(subDir).doesNotExist();

            testFile = subDir.resolve("test.txt");
            new File(testFile).write(FILE_CONTENT);

            assertThat(subDir).exists();
        }

        @Test
        @DisplayName("can be copied to another directory")
        void copyToDirectory(@TempDir Path anotherDirectory) throws RepositoryException, IOException {
            assertThat(anotherDirectory).isEmptyDirectory();

            new File(testFile).copyTo(new Directory(anotherDirectory));

            assertThat(anotherDirectory).isNotEmptyDirectory();
            Path newFile = anotherDirectory.resolve(testFile.getFileName());
            assertThat(newFile).exists();
            assertThat(Files.readString(newFile)).isEqualTo(FILE_CONTENT);
        }

        @Test
        @DisplayName("can be renamed")
        void copyToNewFile() throws RepositoryException {
            Path newFile = testFile.resolveSibling("newFile.txt");
            assertThat(newFile).doesNotExist();

            new File(testFile).copyTo(new File(newFile));

            assertThat(newFile).exists();
        }

        @Test
        @DisplayName("overrides an existing file when copied")
        void copyToExistingFile() throws RepositoryException, IOException {
            Path newFile = testDir.resolve("newFile.txt");
            Files.writeString(newFile, "");
            assertThat(newFile).exists();
            assertThat(Files.readString(newFile)).isEmpty();

            new File(testFile).copyTo(new File(newFile));

            assertThat(newFile).exists();
            assertThat(Files.readString(newFile)).isEqualTo(FILE_CONTENT);
        }

        @Test
        @DisplayName("can be deleted")
        void deleteSuccessfully() throws RepositoryException {
            assertThat(testFile).exists();
            new File(testFile).delete();
            assertThat(testFile).doesNotExist();
        }
    }

    @Nested
    @DisplayName("that does not exist")
    class NotExistingFileTest {
        @Test
        @DisplayName("returns false when calling exists()")
        void existsReturnsFalse() {
            assertThat(notExistingFile().exists()).isFalse();
        }

        @Test
        @DisplayName("cannot be read")
        void fileNotFound() {
            File file = notExistingFile();
            assertThatThrownBy(file::read)
                    .isInstanceOf(RepositoryException.class)
                    .hasCauseInstanceOf(NoSuchFileException.class)
                    .hasMessageMatching("The file .+ does not exist\\.");
        }

        @Test
        @DisplayName("can be created")
        void createOnWrite() throws RepositoryException, IOException {
            testFile = testDir.resolve(FILE_NAME);
            assertThat(testFile).doesNotExist();

            new File(testFile).write(FILE_CONTENT);

            assertThat(testFile).exists();
            assertThat(Files.readString(testFile)).isEqualTo(FILE_CONTENT);
        }

        @Test
        @DisplayName("does not throw Exception when deleted")
        void delete() throws RepositoryException {
            notExistingFile().delete();
        }
    }

    private File notExistingFile() {
        return new File(testDir.resolve("notExistingFile.txt"));
    }
}