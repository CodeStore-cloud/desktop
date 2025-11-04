package cloud.codestore.core.repositories.synchronization;

import cloud.codestore.core.repositories.RepositoryException;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

class GoogleDriveDirectory implements RemoteDirectory {
    private static final String MIME_TYPE = "application/vnd.google-apps.folder";
    private static final String DEFAULT_SPACE = "drive";

    private final Drive service;
    private final String name;
    private final String path;
    private final File parentFolder;
    private File driveFile;

    GoogleDriveDirectory(Drive service, String name) {
        this.service = service;
        this.name = name;
        this.path = "/" + name;
        this.parentFolder = new File().setId("root");
        this.driveFile = getFolder();
    }

    private GoogleDriveDirectory(Drive service, GoogleDriveDirectory parent, String name) {
        this.service = service;
        this.name = name;
        this.path = parent.path + "/" + name;
        this.parentFolder = parent.driveFile;
        this.driveFile = getFolder();
    }

    File getDriveFile() {
        return driveFile;
    }

    @Override
    public boolean exists() {
        return driveFile != null;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public void create() {
        File folderMetadata = new File();
        folderMetadata.setName(name);
        folderMetadata.setMimeType(MIME_TYPE);
        folderMetadata.setParents(Collections.singletonList(parentFolder.getId()));

        try {
            driveFile = service.files()
                               .create(folderMetadata)
                               .setFields("id")
                               .execute();
        } catch (IOException exception) {
            throw new RepositoryException(exception, "cloud.directory.couldNotCreate", path);
        }
    }

    @Override
    public RemoteDirectory getSubDirectory(String name) {
        return new GoogleDriveDirectory(service, this, name);
    }

    @Override
    public List<RemoteFile> getFiles() {
        String query = "mimeType != '" + MIME_TYPE + "' " +
                       "and '" + parentFolder.getId() + "' in parents " +
                       "and trashed = false";

        List<RemoteFile> snippetFiles = new LinkedList<>();
        try {
            String pageToken = null;
            do {
                FileList result = service.files()
                                         .list()
                                         .setQ(query)
                                         .setSpaces("drive")
                                         .setFields("nextPageToken, files(id, name, modifiedTime)")
                                         .setPageToken(pageToken)
                                         .execute();

                List<File> files = result.getFiles();
                if (files != null && !files.isEmpty()) {
                    for (File file : files) {
                        snippetFiles.add(new GoogleDriveFile(service, this, file));
                    }
                }

                pageToken = result.getNextPageToken();
            } while (pageToken != null);
        } catch (IOException exception) {
            throw new RepositoryException(exception, "cloud.directory.couldNotGetFiles", path);
        }

        return snippetFiles;
    }

    @Override
    public RemoteFile newFile(String name) {
        return new GoogleDriveFile(service, this, name);
    }

    @Nullable
    private File getFolder() {
        String query = "mimeType = '" + MIME_TYPE + "' " +
                       "and name = '" + name + "' " +
                       "and '" + parentFolder.getId() + "' in parents " +
                       "and trashed = false";

        FileList result;
        try {
            result = service.files()
                            .list()
                            .setQ(query)
                            .setSpaces(DEFAULT_SPACE)
                            .setFields("files(id)")
                            .execute();
        } catch (IOException exception) {
            throw new RepositoryException(exception, "cloud.directory.couldNotAccess", path);
        }

        List<File> folders = result.getFiles();
        return folders == null || folders.isEmpty() ? null : folders.getFirst();
    }
}
