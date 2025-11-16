package cloud.codestore.core.repositories.synchronization.service;

import cloud.codestore.core.repositories.RepositoryException;
import cloud.codestore.core.repositories.synchronization.RemoteDirectory;
import cloud.codestore.core.repositories.synchronization.RemoteFile;
import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.*;

import java.util.LinkedList;
import java.util.List;

class DropboxDirectory implements RemoteDirectory {
    private final DbxClientV2 client;
    private final String name;
    private final String path;
    private FolderMetadata metadata;

    DropboxDirectory(DbxClientV2 client, String name) {
        this.client = client;
        this.name = name;
        this.path = "/" + name;
        this.metadata = getFolder();
    }

    private DropboxDirectory(DbxClientV2 client, DropboxDirectory parent, String name) {
        this.client = client;
        this.name = name;
        this.path = parent.path + "/" + name;
        this.metadata = getFolder();
    }

    @Override
    public boolean exists() {
        return metadata != null;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public void create() {
        if (exists()) {
            throw new IllegalStateException("directory already exists");
        }

        try {
            CreateFolderResult result = client.files().createFolderV2(path);
            metadata = result.getMetadata();
        } catch (DbxException exception) {
            throw new RepositoryException(exception, "cloud.directory.couldNotCreate", path);
        }
    }

    @Override
    public RemoteDirectory getSubDirectory(String name) {
        verifyDirectoryExists();
        return new DropboxDirectory(client, this, name);
    }

    @Override
    public List<RemoteFile> getFiles() {
        verifyDirectoryExists();
        List<RemoteFile> snippetFiles = new LinkedList<>();

        try {
            ListFolderResult result = client.files().listFolder(path);
            for (Metadata metadata : result.getEntries()) {
                if (metadata instanceof FileMetadata fileMeta) {
                    snippetFiles.add(new DropboxFile(client, this, fileMeta));
                }
            }

            while (result.getHasMore()) {
                result = client.files().listFolderContinue(result.getCursor());
                for (Metadata metadata : result.getEntries()) {
                    if (metadata instanceof FileMetadata fileMeta) {
                        snippetFiles.add(new DropboxFile(client, this, fileMeta));
                    }
                }
            }
        } catch (DbxException exception) {
            throw new RepositoryException(exception, "cloud.directory.couldNotGetFiles", path);
        }

        return snippetFiles;
    }

    @Override
    public RemoteFile newFile(String name) {
        verifyDirectoryExists();
        return new DropboxFile(client, this, name);
    }

    private FolderMetadata getFolder() {
        try {
            Metadata metadata = client.files().getMetadata(path);
            if (metadata instanceof FolderMetadata folderMetadata) {
                return folderMetadata;
            }
        } catch (GetMetadataErrorException exception) {
            if (exception.errorValue.isPath() && exception.errorValue.getPathValue().isNotFound()) {
                return null;
            } else {
                throw new RepositoryException(exception, "cloud.directory.couldNotAccess", path);
            }
        } catch (DbxException exception) {
            throw new RepositoryException(exception, "cloud.directory.couldNotAccess", path);
        }

        return null;
    }

    private void verifyDirectoryExists() {
        if (!exists()) {
            throw new IllegalStateException("directory does not exist");
        }
    }
}
