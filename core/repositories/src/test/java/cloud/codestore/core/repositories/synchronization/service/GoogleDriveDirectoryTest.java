package cloud.codestore.core.repositories.synchronization.service;

import cloud.codestore.core.repositories.synchronization.RemoteFile;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("a Google Drive directory")
class GoogleDriveDirectoryTest {
    @Mock
    private Drive service;
    @Mock
    private Drive.Files filesRequest;
    @Mock
    private Drive.Files.List listRequest;
    @Mock
    private Drive.Files.Create createRequest;

    private GoogleDriveDirectory directory;
    private File driveDir;

    @BeforeEach
    void setUp() throws IOException {
        when(service.files()).thenReturn(filesRequest);
        when(filesRequest.list()).thenReturn(listRequest);
        when(listRequest.setQ(anyString())).thenReturn(listRequest);
        when(listRequest.setSpaces(anyString())).thenReturn(listRequest);
        when(listRequest.setFields(anyString())).thenReturn(listRequest);

        driveDir = new File().setId("123");
        when(listRequest.execute()).thenReturn(new FileList().setFiles(List.of(driveDir)));

        directory = new GoogleDriveDirectory(service, "test");
    }

    @Test
    @DisplayName("checks for its existence after creation")
    void checkForExistence() {
        verify(listRequest).setQ("mimeType = 'application/vnd.google-apps.folder' " +
                                 "and name = 'test' " +
                                 "and 'root' in parents " +
                                 "and trashed = false");

        assertThat(directory.exists()).isTrue();
        assertThat(directory.getDriveFile()).isSameAs(driveDir);
    }

    @Test
    @DisplayName("has the root directory as parent by default")
    void rootParent() {
        assertThat(directory.getPath()).isEqualTo("/test");
        assertThat(directory.getParent()).isNotNull();
        assertThat(directory.getParent().getId()).isEqualTo("root");
    }

    @Nested
    @DisplayName("that does exist")
    class Existing {
        @Test
        @DisplayName("cannot be created")
        void failCreation() {
            assertThatThrownBy(directory::create).isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("may have subdirectories")
        void getDubDirectory() {
            GoogleDriveDirectory subDirectory = (GoogleDriveDirectory) directory.getSubDirectory("bla");

            assertThat(subDirectory.getPath()).isEqualTo("/test/bla");
            assertThat(subDirectory.getDriveFile()).isEqualTo(driveDir);
        }

        @Test
        @DisplayName("may have files")
        void getFiles() throws IOException {
            clearInvocations(listRequest);
            String[] fileNames = {"file1", "file2", "file3", "file4", "file5", "file6", "file7", "file8"};
            File[] files = Arrays.stream(fileNames)
                                 .map(name -> new File().setName(name))
                                 .toArray(File[]::new);

            FileList page1 = new FileList().setFiles(List.of(files[0], files[1], files[2])).setNextPageToken("p2");
            FileList page2 = new FileList().setFiles(List.of(files[3], files[4], files[5])).setNextPageToken("p3");
            FileList page3 = new FileList().setFiles(List.of(files[6], files[7])).setNextPageToken(null);
            when(listRequest.setPageToken(any())).thenReturn(listRequest);
            when(listRequest.execute()).thenReturn(page1, page2, page3);

            List<RemoteFile> remoteFiles = directory.getFiles();

            verify(listRequest, times(3)).setQ("mimeType != 'application/vnd.google-apps.folder' " +
                                               "and '123' in parents " +
                                               "and trashed = false");
            assertThat(files).hasSize(files.length);
            List<String> remoteFileNames = remoteFiles.stream().map(RemoteFile::getName).toList();
            assertThat(remoteFileNames).containsExactlyInAnyOrder(fileNames);
        }

        @Test
        @DisplayName("accepts new files to be added")
        void newFile() {
            GoogleDriveFile remoteFile = (GoogleDriveFile) directory.newFile("test.json");

            assertThat(remoteFile).isNotNull();
            assertThat(remoteFile.exists()).isFalse();
            assertThat(remoteFile.getPath()).isEqualTo("/test/test.json");
        }
    }

    @Nested
    @DisplayName("that does not exist")
    class NotExisting {
        @BeforeEach
        void setUp() throws IOException {
            when(listRequest.execute()).thenReturn(new FileList().setFiles(List.of()));
            directory = new GoogleDriveDirectory(service, "test");
            assertThat(directory.exists()).isFalse();
        }

        @Test
        @DisplayName("can be created")
        void createDirectory() throws IOException {
            when(filesRequest.create(any(File.class))).thenReturn(createRequest);
            when(createRequest.setFields(anyString())).thenReturn(createRequest);
            when(createRequest.execute()).thenReturn(driveDir);

            directory.create();

            assertThat(directory.exists()).isTrue();
            ArgumentCaptor<File> metadataArgument = ArgumentCaptor.forClass(File.class);
            verify(filesRequest).create(metadataArgument.capture());
            File metadata = metadataArgument.getValue();
            assertThat(metadata.getName()).isEqualTo("test");
            assertThat(metadata.getMimeType()).isEqualTo("application/vnd.google-apps.folder");
            assertThat(metadata.getParents()).containsExactly("root");
        }

        @Test
        @DisplayName("cannot access subdirectories")
        void getDubDirectory() {
            assertThatThrownBy(() -> directory.getSubDirectory("bla")).isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("cannot access its files")
        void getFiles() {
            assertThatThrownBy(directory::getFiles).isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("cannot add new files")
        void createFile() {
            assertThatThrownBy(() -> directory.newFile("bla")).isInstanceOf(IllegalStateException.class);
        }
    }
}