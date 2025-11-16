package cloud.codestore.core.repositories.synchronization.service;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.*;
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
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("a Dropbox file")
class DropboxFileTest {
    private static final String FILE_NAME = "test.json";

    @Mock
    private DbxUserFilesRequests filesRequests;
    @Mock
    private DbxClientV2 client;
    @Mock
    private FileMetadata metadata;
    @Mock
    private DropboxDirectory parentDirectory;
    private DropboxFile file;

    @BeforeEach
    void setUp() {
        lenient().when(client.files()).thenReturn(filesRequests);
        when(parentDirectory.getPath()).thenReturn("/parent/path");
        file = new DropboxFile(client, parentDirectory, FILE_NAME);
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
            when(metadata.getName()).thenReturn(FILE_NAME);
            file = new DropboxFile(client, parentDirectory, metadata);
            assertThat(file.exists()).isTrue();
        }

        @Test
        @DisplayName("can be read")
        void read() throws IOException, DbxException {
            String content = "Hello, World!";

            var downloadBuilder = mock(DownloadBuilder.class);
            when(filesRequests.downloadBuilder(anyString())).thenReturn(downloadBuilder);
            when(downloadBuilder.download(any(OutputStream.class))).thenAnswer(invocation -> {
                try (OutputStream outputStream = invocation.getArgument(0, OutputStream.class)) {
                    outputStream.write(content.getBytes(StandardCharsets.UTF_8));
                }
                return null;
            });

            assertThat(file.read()).isEqualTo(content);
        }

        @Test
        @DisplayName("can be updated")
        void update() throws IOException, DbxException {
            assertContentWrittenWithMode(WriteMode.OVERWRITE);
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
        void delete() throws DbxException {
            file.delete();
            verify(filesRequests).deleteV2(file.getPath());
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
        void create() throws IOException, DbxException {
            assertContentWrittenWithMode(WriteMode.ADD);
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

    private void assertContentWrittenWithMode(WriteMode expectedWriteMode) throws IOException, DbxException {
        var modifiedTimestampArgument = ArgumentCaptor.forClass(Date.class);
        var contentArgument = ArgumentCaptor.forClass(InputStream.class);

        var uploadBuilder = mock(UploadBuilder.class);
        when(filesRequests.uploadBuilder(anyString())).thenReturn(uploadBuilder);
        when(uploadBuilder.withClientModified(any())).thenReturn(uploadBuilder);
        when(uploadBuilder.withMode(any())).thenReturn(uploadBuilder);

        OffsetDateTime modified = OffsetDateTime.now().truncatedTo(ChronoUnit.MILLIS);
        file.setModified(modified);
        assertThat(file.getModified()).isEqualTo(modified);

        String content = "Hello, World!";
        file.write(content);

        verify(uploadBuilder).withMode(expectedWriteMode);
        verify(uploadBuilder).withClientModified(modifiedTimestampArgument.capture());
        verify(uploadBuilder).uploadAndFinish(contentArgument.capture());
        assertThat(toOffsetDateTime(modifiedTimestampArgument.getValue())).isEqualTo(modified);

        try (InputStream inputStream = contentArgument.getValue()) {
            String readContent = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            assertThat(readContent).isEqualTo(content);
        }
    }

    private OffsetDateTime toOffsetDateTime(Date date) {
        if (date == null) {
            return null;
        }

        Instant instant = date.toInstant();
        return OffsetDateTime.ofInstant(instant, ZoneOffset.UTC);
    }
}