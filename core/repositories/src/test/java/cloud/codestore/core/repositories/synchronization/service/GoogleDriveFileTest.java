package cloud.codestore.core.repositories.synchronization.service;

import com.google.api.client.http.AbstractInputStreamContent;
import com.google.api.client.util.DateTime;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("a Google Drive file")
class GoogleDriveFileTest {
    private static final String FILE_NAME = "test.json";

    @Mock
    private Drive service;
    @Mock
    private Drive.Files filesRequest;
    @Mock
    private Drive.Files.List listRequest;
    @Mock
    private Drive.Files.Get getRequest;
    @Mock
    private Drive.Files.Create createRequest;
    @Mock
    private Drive.Files.Update updateRequest;
    @Mock
    private Drive.Files.Delete deleteRequest;
    @Mock
    private GoogleDriveDirectory parentDirectory;
    @Mock
    private File parentDir;

    private GoogleDriveFile file;
    private File driveFile;

    @BeforeEach
    void setUp() {
        lenient().when(service.files()).thenReturn(filesRequest);
        when(parentDirectory.getPath()).thenReturn("parent/path");
        when(parentDirectory.getDriveFile()).thenReturn(parentDir);

        driveFile = new File().setId("123").setName(FILE_NAME);
        file = new GoogleDriveFile(service, parentDirectory, FILE_NAME);
    }

    @Test
    @DisplayName("has a name")
    void getName() {
        assertThat(file.getName()).isEqualTo(FILE_NAME);
    }

    @Test
    @DisplayName("has a path including its parent directories")
    void getPath() {
        assertThat(file.getPath()).isEqualTo(parentDirectory.getPath() + "/" + FILE_NAME);
    }

    @Nested
    @DisplayName("that does exist")
    class Existing {
        @BeforeEach
        void setUp() {
            file = new GoogleDriveFile(service, parentDirectory, driveFile);
            assertThat(file.exists()).isTrue();
        }

        @Test
        @DisplayName("can be read")
        void read() throws IOException {
            String content = "Hello, World!";
            when(filesRequest.get(anyString())).thenReturn(getRequest);
            doAnswer(invocation -> {
                try (OutputStream outputStream = invocation.getArgument(0, OutputStream.class)) {
                    outputStream.write(content.getBytes(StandardCharsets.UTF_8));
                }
                return null;
            }).when(getRequest).executeMediaAndDownloadTo(any(OutputStream.class));

            assertThat(file.read()).isEqualTo(content);
        }

        @Test
        @DisplayName("can be updated")
        void update() throws IOException {
            var metadataArgument = ArgumentCaptor.forClass(File.class);
            var contentArgument = ArgumentCaptor.forClass(AbstractInputStreamContent.class);

            when(filesRequest.update(anyString(), any(File.class), any(AbstractInputStreamContent.class)))
                    .thenReturn(updateRequest);

            OffsetDateTime modified = OffsetDateTime.now().truncatedTo(ChronoUnit.MILLIS);
            file.setModified(modified);
            assertThat(file.getModified()).isEqualTo(modified);

            String content = "Hello, World!";
            file.write(content);

            verify(updateRequest).execute();
            verify(filesRequest).update(anyString(), metadataArgument.capture(), contentArgument.capture());
            File metadata = metadataArgument.getValue();
            assertThat(toOffsetDateTime(metadata.getModifiedTime())).isAtSameInstantAs(modified);

            AbstractInputStreamContent contentStream = contentArgument.getValue();
            assertThat(contentStream.getType()).isEqualTo(GoogleDriveFile.CONTENT_TYPE);
            try (InputStream inputStream = contentStream.getInputStream()) {
                String readContent = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                assertThat(readContent).isEqualTo(content);
            }
        }

        @Test
        @DisplayName("must have a modified timestamp set when being created")
        void modifiedTimestampNotSet() {
            assertThatThrownBy(() -> file.write("Hello, World!"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Modified timestamp must be set");
        }

        @Test
        @DisplayName("can be deleted")
        void delete() throws IOException {
            when(filesRequest.delete(anyString())).thenReturn(deleteRequest);
            file.delete();
            verify(deleteRequest).execute();
        }
    }

    @Nested
    @DisplayName("that does not exist")
    class NotExisting {
        @BeforeEach
        void setUp() {
            assertThat(file.exists()).isFalse();
        }

        @Test
        @DisplayName("cannot be read")
        void read() {
            assertThatThrownBy(file::read).isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("can be created")
        void create() throws IOException {
            var metadataArgument = ArgumentCaptor.forClass(File.class);
            var contentArgument = ArgumentCaptor.forClass(AbstractInputStreamContent.class);

            String parentId = "parent_id";
            when(filesRequest.create(any(File.class), any(AbstractInputStreamContent.class))).thenReturn(createRequest);
            when(parentDir.getId()).thenReturn(parentId);

            OffsetDateTime created = OffsetDateTime.now().truncatedTo(ChronoUnit.MILLIS);
            OffsetDateTime modified = created.plusHours(2);
            file.setModified(modified);
            file.setCreated(created);

            String content = "Hello, World!";
            file.write(content);

            verify(createRequest).execute();
            verify(filesRequest).create(metadataArgument.capture(), contentArgument.capture());
            File metadata = metadataArgument.getValue();
            assertThat(metadata.getMimeType()).isEqualTo(GoogleDriveFile.CONTENT_TYPE);
            assertThat(toOffsetDateTime(metadata.getModifiedTime())).isAtSameInstantAs(modified);
            assertThat(toOffsetDateTime(metadata.getCreatedTime())).isAtSameInstantAs(created);

            AbstractInputStreamContent contentStream = contentArgument.getValue();
            assertThat(contentStream.getType()).isEqualTo(GoogleDriveFile.CONTENT_TYPE);
            try (InputStream inputStream = contentStream.getInputStream()) {
                String readContent = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                assertThat(readContent).isEqualTo(content);
            }
        }

        @Test
        @DisplayName("must have a modified timestamp set when being created")
        void modifiedTimestampNotSet() {
            assertThatThrownBy(() -> file.write("Hello, World!"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Modified timestamp must be set");
        }

        @Test
        @DisplayName("cannot be deleted")
        void delete() {
            assertThatThrownBy(file::delete).isInstanceOf(IllegalStateException.class);
        }
    }

    private OffsetDateTime toOffsetDateTime(DateTime dateTime) {
        if (dateTime == null) {
            return null;
        }

        Instant instant = Instant.ofEpochMilli(dateTime.getValue());
        int tzShiftMinutes = dateTime.getTimeZoneShift();
        ZoneOffset offset = ZoneOffset.ofTotalSeconds(tzShiftMinutes * 60);
        return OffsetDateTime.ofInstant(instant, offset);
    }
}