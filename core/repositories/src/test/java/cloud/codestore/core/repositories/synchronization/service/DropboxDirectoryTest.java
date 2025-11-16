package cloud.codestore.core.repositories.synchronization.service;

import cloud.codestore.core.repositories.synchronization.RemoteFile;
import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("a Dropbox directory")
class DropboxDirectoryTest {
    private static final String DIR_NAME = "test";

    @Mock
    private DbxUserFilesRequests filesRequests;
    @Mock
    private DbxClientV2 client;
    @Mock
    private FolderMetadata metadata;
    private DropboxDirectory directory;

    @BeforeEach
    void setUp() throws DbxException {
        when(client.files()).thenReturn(filesRequests);
        when(filesRequests.getMetadata(anyString())).thenReturn(metadata);
        directory = new DropboxDirectory(client, DIR_NAME);
    }

    @Test
    @DisplayName("checks for its existence after creation")
    void checkForExistence() throws DbxException {
        verify(filesRequests).getMetadata(directory.getPath());
        assertThat(directory.exists()).isTrue();

        var metaError = GetMetadataError.path(LookupError.NOT_FOUND);
        var metaErrorException = new GetMetadataErrorException("<routeName>", "<requestId>", null, metaError);
        doThrow(metaErrorException).when(filesRequests).getMetadata(anyString());
        directory = new DropboxDirectory(client, DIR_NAME);
        assertThat(directory.exists()).isFalse();
    }

    @Test
    @DisplayName("has the root directory as parent by default")
    void rootParent() {
        assertThat(directory.getPath()).isEqualTo("/" + DIR_NAME);
    }

    @Nested
    @DisplayName("that does exist")
    class Existing {
        @BeforeEach
        void setUp() {
            assertThat(directory.exists()).isTrue();
        }

        @Test
        @DisplayName("cannot be created")
        void failCreation() {
            assertThatThrownBy(directory::create).isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("may have subdirectories")
        void getDubDirectory() {
            DropboxDirectory subDirectory = (DropboxDirectory) directory.getSubDirectory("subdir");
            assertThat(subDirectory.getPath()).isEqualTo("/" + DIR_NAME+ "/subdir");
        }

        @Test
        @DisplayName("may have files")
        void getFiles() throws DbxException {
            String[] fileNames = {"file1", "file2", "file3", "file4", "file5", "file6", "file7", "file8"};
            FileMetadata[] meta = Arrays.stream(fileNames)
                                 .map(name -> {
                                     FileMetadata file = mock(FileMetadata.class);
                                     when(file.getName()).thenReturn(name);
                                     return file;
                                 })
                                 .toArray(FileMetadata[]::new);

            ListFolderResult page1 = new ListFolderResult(List.of(meta[0], meta[1], meta[2]), "page1", true);
            ListFolderResult page2 = new ListFolderResult(List.of(meta[3], meta[4], meta[5]), "page2", true);
            ListFolderResult page3 = new ListFolderResult(List.of(meta[6], meta[7]), "page3", false);
            when(filesRequests.listFolder(anyString())).thenReturn(page1);
            when(filesRequests.listFolderContinue(anyString())).thenReturn(page2, page3);

            List<RemoteFile> files = directory.getFiles();
            assertThat(files).hasSize(meta.length);
            List<String> remoteFileNames = files.stream().map(RemoteFile::getName).toList();
            assertThat(remoteFileNames).containsExactlyInAnyOrder(fileNames);
        }

        @Test
        @DisplayName("accepts new files to be added")
        void newFile() {
            RemoteFile remoteFile = new DropboxFile(client, directory, "test.json");

            assertThat(remoteFile).isNotNull();
            assertThat(remoteFile.exists()).isFalse();
            assertThat(remoteFile.getPath()).isEqualTo("/" + DIR_NAME + "/test.json");
        }

        private FileMetadata fileMetadata(String fileName) {
            FileMetadata file = mock(FileMetadata.class);
            when(file.getName()).thenReturn(fileName);
            return file;
        }
    }

    @Nested
    @DisplayName("that does not exist")
    class NotExisting {
        @BeforeEach
        void setUp() throws DbxException {
            when(filesRequests.getMetadata(anyString())).thenReturn(null);
            directory = new DropboxDirectory(client, DIR_NAME);
            assertThat(directory.exists()).isFalse();
        }

        @Test
        @DisplayName("can be created")
        void createDirectory() throws DbxException {
            when(filesRequests.createFolderV2(directory.getPath())).thenReturn(new CreateFolderResult(metadata));
            directory.create();
            assertThat(directory.exists()).isTrue();
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