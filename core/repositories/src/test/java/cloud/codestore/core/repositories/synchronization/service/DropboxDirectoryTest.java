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
        new DropboxDirectory(client, DIR_NAME);
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
            FileMetadata file1 = fileMetadata("file1");
            FileMetadata file2 = fileMetadata("file2");
            FileMetadata file3 = fileMetadata("file3");
            ListFolderResult page1 = new ListFolderResult(List.of(file1, file2, file3), "page1", true);
            FileMetadata file4 = fileMetadata("file4");
            FileMetadata file5 = fileMetadata("file5");
            FileMetadata file6 = fileMetadata("file6");
            ListFolderResult page2 = new ListFolderResult(List.of(file4, file5, file6), "page2", true);
            FileMetadata file7 = fileMetadata("file7");
            FileMetadata file8 = fileMetadata("file8");
            ListFolderResult page3 = new ListFolderResult(List.of(file7, file8), "page3", false);

            when(filesRequests.listFolder(anyString())).thenReturn(page1);
            when(filesRequests.listFolderContinue(anyString())).thenReturn(page2);
            when(filesRequests.listFolderContinue(anyString())).thenReturn(page3);

            List<RemoteFile> files = directory.getFiles();
            assertThat(files).hasSize(8);
            List<String> fileNames = files.stream().map(RemoteFile::getName).toList();
            assertThat(fileNames).containsExactlyInAnyOrder(
                    "file1", "file2", "file3", "file4", "file5", "file6", "file7", "file8");
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