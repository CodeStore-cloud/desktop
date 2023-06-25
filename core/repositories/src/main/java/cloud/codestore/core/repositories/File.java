package cloud.codestore.core.repositories;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Objects;

/**
 * Encapsulates the access to a file.
 */
public class File {
    private static final Logger LOGGER = LoggerFactory.getLogger(File.class);

    private final Path path;

    /**
     * Creates a new {@link File} which is represented by the given {@link Path}.
     *
     * @param path the location and name of this file.
     * @throws IllegalArgumentException if the path is an existing directory.
     */
    public File(@Nonnull Path path) {
        if (Files.isDirectory(path))
            throw new IllegalArgumentException(path.toAbsolutePath() + " is a directory.");

        this.path = path;
    }

    /**
     * @return the {@link Path} of this file.
     */
    public Path path() {
        return path;
    }

    /**
     * @return the name of this file.
     */
    public String getName() {
        return path.getFileName().toString();
    }

    /**
     * @return whether this file exists on the file system.
     */
    public boolean exists() {
        return Files.exists(path);
    }

    /**
     * Reads this file
     *
     * @return the content of this file.
     * @throws RepositoryException if the file could not be read.
     */
    @Nonnull
    public String read() throws RepositoryException {
        try {
            return Files.readString(path);
        } catch (NoSuchFileException exception) {
            throw new RepositoryException(exception, "file.notExists", path);
        } catch (IOException exception) {
            throw new RepositoryException(exception, "file.couldNotRead", path);
        }
    }

    /**
     * Reads this file.
     * If it cannot be read or the file is empty, this method returns the given fallback-content.
     *
     * @param fallbackContent the content to return if the file could not be read.
     * @return the content of this file or {@code fallbackContent}.
     */
    @Nonnull
    public String readOrElse(String fallbackContent) {
        try {
            String fileContent = Files.readString(path);
            if (fileContent.isEmpty()) {
                fileContent = fallbackContent;
            }
            return fileContent;
        } catch (IOException e) {
            return fallbackContent;
        }
    }

    /**
     * Saves this file.
     * All parent directories will be created if necessary.
     *
     * @param content the content of the file.
     * @throws RepositoryException if the file could not be saved.
     */
    public void write(@Nonnull String content) throws RepositoryException {
        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, content, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException exception) {
            throw new RepositoryException(exception, "file.couldNotSave", path);
        }
    }

    /**
     * Deletes this file.
     * If the file does not exist, nothing happens.
     *
     * @throws RepositoryException if the file exists but could not be deleted.
     */
    public void delete() throws RepositoryException {
        try {
            Files.deleteIfExists(path);
        } catch (IOException exception) {
            throw new RepositoryException(exception, "file.couldNotDelete", path);
        }
    }

    /**
     * Deletes this file.
     * If the file does not exist, nothing happens.
     * If the file does exist but cannot be deleted, nothing happens.
     */
    public void deleteSilently() {
        try {
            Files.deleteIfExists(path);
        } catch (IOException exception) {
            LOGGER.warn("The file {} could not be deleted.", path, exception);
        }
    }

    /**
     * Copies this file to the given directory.
     *
     * @param target the target directory.
     * @throws RepositoryException if the file could not be copied.
     */
    public void copyTo(@Nonnull Directory target) throws RepositoryException {
        copyTo(target.path().resolve(path.getFileName()));
    }

    /**
     * Copies this file to the given target file.
     * The target file will be overridden if it already exists.
     *
     * @param target the target file.
     * @throws RepositoryException if the file could not be copied.
     */
    public void copyTo(@Nonnull File target) throws RepositoryException {
        copyTo(target.path);
    }

    private void copyTo(@Nonnull Path target) throws RepositoryException {
        try {
            Files.copy(path, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ioException) {
            throw new RepositoryException(ioException, "file.couldNotCopy", path, target);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        File file = (File) o;
        return Objects.equals(path, file.path);
    }

}
