package cloud.codestore.core.repositories.synchronization;

import cloud.codestore.core.repositories.RepositoryException;
import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.WriteMode;

import javax.annotation.Nonnull;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Date;

class DropboxFile implements RemoteFile {
    private final DbxClientV2 client;
    private final String name;
    private final String path;
    private OffsetDateTime modified;
    private FileMetadata metadata;

    DropboxFile(DbxClientV2 client, FileMetadata metadata) {
        this.client = client;
        this.name = metadata.getName();
        this.path = "/" + name;
        this.metadata = metadata;
    }

    DropboxFile(DbxClientV2 client, DropboxDirectory parent, String name) {
        this.client = client;
        this.name = name;
        this.path = parent.getPath() + "/" + name;
    }

    @Override
    public boolean exists() {
        return metadata != null;
    }

    @Override
    @Nonnull
    public String getName() {
        return name;
    }

    @Override
    @Nonnull
    public String getPath() {
        return path;
    }

    @Override
    @Nonnull
    public OffsetDateTime getModified() {
        if (modified == null && exists()) {
            Instant instant = metadata.getClientModified().toInstant();
            modified = OffsetDateTime.ofInstant(instant, ZoneOffset.UTC);
        }

        return modified;
    }

    @Override
    public void setModified(@Nonnull OffsetDateTime modified) {
        this.modified = modified;
    }

    @Override
    public void setCreated(@Nonnull OffsetDateTime created) {}

    @Override
    @Nonnull
    public String read() {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            client.files()
                  .downloadBuilder(path)
                  .download(outputStream);

            return outputStream.toString(StandardCharsets.UTF_8);
        } catch (IOException | DbxException exception) {
            throw new RepositoryException(exception, "cloud.file.couldNotRead", path);
        }
    }

    @Override
    public void write(@Nonnull String content) {
        try (InputStream in = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8))) {
            client.files()
                  .uploadBuilder(path)
                  .withClientModified(new Date(getModified().toInstant().toEpochMilli()))
                  .withMode(exists() ? WriteMode.OVERWRITE : WriteMode.ADD)
                  .uploadAndFinish(in);
        } catch (IOException | DbxException exception) {
            throw new RepositoryException(exception, "cloud.file.couldNotSave", path);
        }
    }

    @Override
    public void delete() {
        try {
            client.files().deleteV2(path);
        } catch (DbxException exception) {
            throw new RepositoryException(exception, "cloud.file.couldNotDelete", path);
        }
    }
}
