package cloud.codestore.core.repositories.synchronization.service;

import cloud.codestore.core.repositories.RepositoryException;
import cloud.codestore.core.repositories.synchronization.RemoteFile;
import com.google.api.client.http.AbstractInputStreamContent;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.util.DateTime;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.Date;
import java.util.TimeZone;

class GoogleDriveFile implements RemoteFile {
    static final String CONTENT_TYPE = "application/json";

    private final Drive service;
    private final String name;
    private final String path;
    private File parent;
    private File driveFile;
    private OffsetDateTime created;
    private OffsetDateTime modified;

    GoogleDriveFile(Drive service, GoogleDriveDirectory parent, File file) {
        this(service, parent, file.getName());
        this.driveFile = file;
    }

    GoogleDriveFile(Drive service, GoogleDriveDirectory parent, String name) {
        this.service = service;
        this.name = name;
        this.path = parent.getPath() + "/" + name;
        this.parent = parent.getDriveFile();
    }

    @Override
    public boolean exists() {
        return driveFile != null;
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
            DateTime modifiedTime = driveFile.getModifiedTime();
            Instant instant = Instant.ofEpochMilli(modifiedTime.getValue());
            return OffsetDateTime.ofInstant(instant, ZoneOffset.UTC);
        }
        return modified;
    }

    @Override
    public void setModified(@Nonnull OffsetDateTime modified) {
        this.modified = modified;
    }

    @Override
    public void setCreated(@Nonnull OffsetDateTime created) {
        this.created = created;
    }

    @Override
    @Nonnull
    public String read() {
        verifyFileExists();

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            service.files()
                   .get(driveFile.getId())
                   .executeMediaAndDownloadTo(stream);
        } catch (IOException exception) {
            throw new RepositoryException(exception, "cloud.file.couldNotRead", path);
        }

        return stream.toString(StandardCharsets.UTF_8);
    }

    @Override
    public void write(@Nonnull String content) {
        if (modified == null) {
            throw new IllegalStateException("Modified timestamp must be set");
        }

        try {
            if (exists()) {
                updateFile(content);
            } else {
                createFile(content);
            }
        } catch (IOException exception) {
            throw new RepositoryException(exception, "cloud.file.couldNotSave", path);
        }
    }

    @Override
    public void delete() {
        verifyFileExists();

        try {
            service.files()
                   .delete(driveFile.getId())
                   .execute();
        } catch (IOException exception) {
            throw new RepositoryException(exception, "cloud.file.couldNotDelete", path);
        }
    }

    private void updateFile(String content) throws IOException {
        File fileMetadata = new File().setModifiedTime(toDateTime(modified));
        AbstractInputStreamContent mediaContent = new ByteArrayContent(
                CONTENT_TYPE,
                content.getBytes(StandardCharsets.UTF_8)
        );

        service.files()
               .update(driveFile.getId(), fileMetadata, mediaContent)
               .execute();
    }

    private void createFile(String content) throws IOException {
        File fileMetadata = new File().setName(name)
                                      .setParents(Collections.singletonList(parent.getId()))
                                      .setMimeType(CONTENT_TYPE)
                                      .setCreatedTime(toDateTime(created))
                                      .setModifiedTime(toDateTime(modified));

        AbstractInputStreamContent mediaContent = new ByteArrayContent(
                CONTENT_TYPE,
                content.getBytes(StandardCharsets.UTF_8)
        );

        driveFile = service.files()
                           .create(fileMetadata, mediaContent)
                           .execute();
    }

    @Nullable
    private DateTime toDateTime(@Nullable OffsetDateTime offsetDateTime) {
        if (offsetDateTime == null) {
            return null;
        }

        TimeZone timeZone = TimeZone.getTimeZone(offsetDateTime.toZonedDateTime().getZone());
        return new DateTime(new Date(offsetDateTime.toInstant().toEpochMilli()), timeZone);
    }

    private void verifyFileExists() {
        if (!exists()) {
            throw new IllegalStateException("file does not exist");
        }
    }
}
