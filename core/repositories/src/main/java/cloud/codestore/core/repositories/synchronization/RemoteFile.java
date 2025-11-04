package cloud.codestore.core.repositories.synchronization;

import javax.annotation.Nonnull;
import java.time.OffsetDateTime;

/**
 * Represents a file on a remote cloud system,
 */
public interface RemoteFile {
    /**
     * @return whether this file exists.
     */
    boolean exists();

    /**
     * @return the name of this file.
     */
    @Nonnull
    String getName();

    /**
     * @return the path including the name of this file on the remote system.
     */
    @Nonnull
    String getPath();

    /**
     * @return the time when this file was last updated.
     */
    @Nonnull
    OffsetDateTime getModified();

    /**
     * @param modified the time when this file was last updated.
     */
    void setModified(@Nonnull OffsetDateTime modified);

    /**
     * @param created the time when this file was created.
     */
    void setCreated(@Nonnull OffsetDateTime created);

    /**
     * @return the content of this file.
     */
    @Nonnull
    String read();

    /**
     * Creates or updates this file on the remote system.
     * @param content the new content of this file.
     */
    void write(@Nonnull String content);

    /**
     * Deletes this file on the remote system.
     */
    void delete();
}
