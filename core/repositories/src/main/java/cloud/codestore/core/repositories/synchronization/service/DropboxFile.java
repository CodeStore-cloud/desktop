package cloud.codestore.core.repositories.synchronization.service;

import cloud.codestore.core.repositories.RepositoryException;
import cloud.codestore.core.repositories.synchronization.RemoteFile;
import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.WriteMode;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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

    DropboxFile(DbxClientV2 client, DropboxDirectory parent, FileMetadata metadata) {
        this(client, parent, metadata.getName());
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
    @Nullable
    public OffsetDateTime getModified() {
        if (modified == null && exists()) {
            Instant instant = metadata.getClientModified().toInstant();
            return OffsetDateTime.ofInstant(instant, ZoneOffset.UTC);
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
        if (modified == null) {
            throw new IllegalStateException("Modified timestamp must be set");
        }

        try (InputStream in = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8))) {
            client.files()
                  .uploadBuilder(path)
                  .withClientModified(new Date(modified.toInstant().toEpochMilli()))
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
