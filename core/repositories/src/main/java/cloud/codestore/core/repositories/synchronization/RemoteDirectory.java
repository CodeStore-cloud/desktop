package cloud.codestore.core.repositories.synchronization;

import java.util.List;

/**
 * Represents a directory on the remote cloud system.
 */
public interface RemoteDirectory {
    /**
     * @return whether the directory exists.
     */
    boolean exists();

    /**
     * @return the path of this directory on the remote system.
     */
    String getPath();

    /**
     * Creates this directory on the remote system.
     */
    void create();

    /**
     * Creates a {@link RemoteDirectory} that represents a subdirectory of this one.
     * The returned directory may not exist.
     * @param name the name of the subdirectory.
     * @return a {@link RemoteDirectory} representing a subdirectory of this one.
     */
    RemoteDirectory getSubDirectory(String name);

    /**
     * @return a list of all files within this directory.
     */
    List<RemoteFile> getFiles();

    /**
     * Creates a new file on the remote system under this directory.
     * The returned object represents the new file which does not yet exist after calling this method.
     * Only when {@link RemoteFile#write(String) writing} into the file, the file gets created.
     * @param name the name of the file.
     * @return a {@link RemoteFile} that represents the new file.
     */
    RemoteFile newFile(String name);
}
