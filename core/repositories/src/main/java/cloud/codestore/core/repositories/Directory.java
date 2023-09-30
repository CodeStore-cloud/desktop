package cloud.codestore.core.repositories;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Encapsulates the access to a directory.
 */
public record Directory(Path path) {
    /**
     * Creates a new directory with the given path.
     *
     * @param path the path of ths directory.
     * @throws IllegalArgumentException if the path exists and is not a directory.
     */
    public Directory(@Nonnull Path path) {
        if (Files.exists(path) && !Files.isDirectory(path))
            throw new IllegalArgumentException(path.toAbsolutePath() + " is not a directory.");

        this.path = path;
    }

    /**
     * @return the {@link Path} of this directory.
     */
    @Override
    public Path path() {
        return path;
    }

    /**
     * @return whether this directory exists on the file system.
     */
    public boolean exists() {
        return Files.exists(path);
    }

    /**
     * @return the name of this directory.
     */
    public String getName() {
        return path.getFileName().toString();
    }

    @Override
    public String toString() {
        return path.toString();
    }

    /**
     * Deletes this directory recursively.
     * If it does not exist, nothing happens.
     *
     * @throws RepositoryException if the directory exists but could not be deleted.
     */
    public void delete() throws RepositoryException {
        if (Files.exists(path)) {
            try {
                Files.walk(path)
                     .sorted(Comparator.reverseOrder())
                     .map(Path::toFile)
                     .forEach(java.io.File::delete);
            } catch (IOException exception) {
                throw new RepositoryException(exception, "directory.couldNotDelete", path);
            }
        }
    }

    /**
     * Checks whether this directory is empty.
     * If it does not exist, this method returns {@code true}.
     *
     * @return whether this directory is empty or does not exist.
     * @throws RepositoryException if the directory could not be accessed.
     */
    public boolean isEmpty() throws RepositoryException {
        try (Stream<Path> entries = Files.list(path)) {
            return entries.findFirst().isEmpty();
        } catch (NoSuchFileException exception) {
            return true;
        } catch (IOException exception) {
            throw new RepositoryException(exception, "directory.couldNotAccess", path);
        }
    }

    /**
     * Returns a file inside this directory.
     *
     * @param fileName the name of the file.
     * @return a {@link File} object representing a file inside this directory.
     */
    public File getFile(@Nonnull String fileName) {
        return new File(path.resolve(fileName));
    }

    /**
     * Returns al files inside this directory.
     *
     * @return a list of {@link File}s representing the files (not subdirectories) inside this directory.
     */
    public List<File> getFiles() throws RepositoryException {
        try {
            return Files.list(path)
                        .filter(Files::isRegularFile)
                        .map(File::new)
                        .collect(Collectors.toList());
        } catch (NoSuchFileException exception) {
            throw new RepositoryException(exception, "directory.notExists", path);
        } catch (IOException exception) {
            throw new RepositoryException(exception, "directory.couldNotAccess", path);
        }
    }

    /**
     * @return the parent directory of this directory.
     */
    public Directory getParentDirectory() {
        return new Directory(path.getParent());
    }

    /**
     * Returns a subdirectory.
     *
     * @param directoryName the name of the subdirectory.
     * @return a {@link Directory} object representing a subdirectory.
     */
    public Directory getSubDirectory(@Nonnull String directoryName) {
        return new Directory(path.resolve(directoryName));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Directory directory = (Directory) o;
        return Objects.equals(path, directory.path);
    }

}
