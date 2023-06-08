package cloud.codestore.core.repositories;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("A directory")
class DirectoryTest {
    @TempDir
    private Path testPath;

    @Test
    @DisplayName("cannot be instantiated if the path is not a directory")
    void failInstantiationIfDirectory() throws IOException {
        Path filePath = testPath.resolve("test.txt");
        Files.writeString(filePath, "");
        assertThatThrownBy(() -> new Directory(filePath))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageEndingWith(" is not a directory.");
    }

    @Test
    @DisplayName("has a name")
    void getName() throws IOException {
        Path subDir = testPath.resolve("subDir");
        Files.createDirectory(subDir);

        Directory directory = new Directory(subDir);

        assertThat(directory.getName()).isEqualTo("subDir");
    }

    @Nested
    @DisplayName("that exists")
    class ExistingDirectoryTest {
        @Test
        @DisplayName("returns true when calling exists()")
        void existsReturnsTrue() {
            assertThat(new Directory(testPath).exists()).isTrue();
        }

        @Test
        @DisplayName("may be empty")
        void emptyDir() throws IOException, RepositoryException {
            assertThat(new Directory(testPath).isEmpty()).isTrue();
            Files.writeString(testPath.resolve("test.txt"), "Hello World");
            assertThat(new Directory(testPath).isEmpty()).isFalse();
        }

        @Test
        @DisplayName("can resolve nested files")
        void getFile() {
            File file = new Directory(testPath).getFile("test.txt");
            assertThat(file.path()).isEqualTo(testPath.resolve("test.txt"));
        }

        @Test
        @DisplayName("can list nested files")
        void listFiles() throws Exception {
            Path file1 = testPath.resolve("test1.txt");
            Path file2 = testPath.resolve("test2.txt");
            Path file3 = testPath.resolve("test3.txt");

            Files.writeString(file1, "test");
            Files.writeString(file2, "test");
            Files.writeString(file3, "test");

            List<File> files = new Directory(testPath).getFiles();
            Assertions.assertThat(files).containsExactlyInAnyOrder(new File(file1), new File(file2), new File(file3));
        }

        @Test
        @DisplayName("can resolve subdirectories")
        void getDir() {
            Directory subDir = new Directory(testPath).getSubDirectory("subDir");
            assertThat(subDir.path()).isEqualTo(testPath.resolve("subDir"));
        }

        @Test
        @DisplayName("can resolve its parent directory")
        void getParentDir() {
            Directory parentDir = new Directory(testPath).getParentDirectory();
            assertThat(parentDir.path()).isEqualTo(testPath.getParent());
        }

        @Test
        @DisplayName("can be deleted")
        void deleteSuccessfully() throws RepositoryException {
            assertThat(Files.exists(testPath)).isTrue();
            new Directory(testPath).delete();
            assertThat(Files.exists(testPath)).isFalse();
        }
    }

    @Nested
    @DisplayName("that not exists")
    class NotExistingDirectoryTest {
        private Directory notExistingDirectory = new Directory(Path.of("NotExistingDirectory"));

        @Test
        @DisplayName("returns false when calling exists()")
        void existsReturnsFalse() {
            assertThat(notExistingDirectory.exists()).isFalse();
        }

        @Test
        @DisplayName("does not throw Exception when deleted")
        void fileNotFound() throws RepositoryException {
            notExistingDirectory.delete();
        }

        @Test
        @DisplayName("is considered to be empty")
        void emptyNotExistingDirectory() throws RepositoryException {
            assertThat(notExistingDirectory.isEmpty()).isTrue();
        }
    }
}