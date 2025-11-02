package cloud.codestore.core.repositories.synchronization;

import cloud.codestore.core.Snippet;
import cloud.codestore.core.repositories.serialization.SnippetReader;
import cloud.codestore.core.repositories.serialization.SnippetWriter;
import com.google.api.client.http.AbstractInputStreamContent;
import com.google.api.client.util.DateTime;
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("The Google Drive snippet set")
class GoogleDriveSnippetSetTest {
    private static final String CODESTORE_FOLDER_ID = "codestore-folder";
    private static final String SNIPPETS_FOLDER_ID = "snippets-folder";
    private static final String SNIPPET1_ID = "snippet-1";
    private static final String DUMMY_CONTENT = "DUMMY-CONTENT";

    @Mock
    private GoogleDriveAuthenticator authenticator;
    @Mock
    private SnippetReader snippetReader;
    @Mock
    private SnippetWriter snippetWriter;
    @Mock
    private Drive service;
    @Mock
    private Drive.Files files;
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

    private GoogleDriveSnippetSet snippetSet;
    private File codestoreFolder;
    private File snippetsFolder;
    private File file1;

    @BeforeEach
    void setUp() throws Exception {
        when(authenticator.authenticate()).thenReturn(service);
        when(service.files()).thenReturn(files);

        when(files.list()).thenReturn(listRequest);
        when(listRequest.setQ(anyString())).thenReturn(listRequest);
        when(listRequest.setSpaces(anyString())).thenReturn(listRequest);
        when(listRequest.setFields(anyString())).thenReturn(listRequest);
        when(listRequest.setPageToken(any())).thenReturn(listRequest);

        snippetSet = new GoogleDriveSnippetSet(authenticator, snippetReader, snippetWriter);
        codestoreFolder = new File().setId(CODESTORE_FOLDER_ID);
        snippetsFolder = new File().setId(SNIPPETS_FOLDER_ID);

        file1 = new File().setId("f1").setName("snippet-1.json").setModifiedTime(new DateTime("2025-11-02T12:21:54Z"));
        File file2 = new File().setId("f2").setName("snippet-2.json").setModifiedTime(new DateTime("2025-11-12T09:45:00Z"));
        FileList snippetPage = new FileList().setFiles(List.of(file1, file2)).setNextPageToken(null);
        when(listRequest.execute()).thenReturn(
                new FileList().setFiles(List.of(codestoreFolder)),
                new FileList().setFiles(List.of(snippetsFolder)),
                snippetPage
        );
    }

    @Nested
    @DisplayName("when collecting the available snippets")
    class GetItemIdsTest {
        @Test
        @DisplayName("reads the IDs and modified timestamps of all code snippets from Google Drive")
        void getItemIds() {
            Set<String> ids = snippetSet.getItemIds();
            assertThat(ids).containsExactlyInAnyOrder(SNIPPET1_ID, "snippet-2");
            assertThat(snippetSet.contains(SNIPPET1_ID)).isTrue();
            assertThat(snippetSet.contains("snippet-2")).isTrue();
            assertThat(snippetSet.contains("snippet-3")).isFalse();
        }

        @Test
        @DisplayName("creates the CodeStore folders if it doesn't exist")
        void createCodeStoreFolder() throws IOException {
            FileList snippetPage = new FileList().setFiles(List.of()).setNextPageToken(null);
            when(listRequest.execute()).thenReturn(
                    new FileList(),
                    new FileList().setFiles(List.of(snippetsFolder)),
                    snippetPage
            );
            when(files.create(any(File.class))).thenReturn(createRequest);
            when(createRequest.setFields(anyString())).thenReturn(createRequest);
            when(createRequest.execute()).thenReturn(codestoreFolder);

            snippetSet.getItemIds();

            ArgumentCaptor<File> metadataCaptor = ArgumentCaptor.forClass(File.class);
            verify(files).create(metadataCaptor.capture());
            File metadata = metadataCaptor.getValue();
            assertThat(metadata.getName()).isEqualTo("CodeStore");
            assertThat(metadata.getMimeType()).isEqualTo("application/vnd.google-apps.folder");
            assertThat(metadata.getParents()).containsExactly("root");
        }

        @Test
        @DisplayName("creates the snippets folders if it doesn't exist")
        void createSnippetsFolder() throws IOException {
            FileList snippetPage = new FileList().setFiles(List.of()).setNextPageToken(null);
            when(listRequest.execute()).thenReturn(
                    new FileList().setFiles(List.of(codestoreFolder)),
                    new FileList(),
                    snippetPage
            );
            when(files.create(any(File.class))).thenReturn(createRequest);
            when(createRequest.setFields(anyString())).thenReturn(createRequest);
            when(createRequest.execute()).thenReturn(snippetsFolder);

            snippetSet.getItemIds();

            ArgumentCaptor<File> metadataCaptor = ArgumentCaptor.forClass(File.class);
            verify(files).create(metadataCaptor.capture());
            File metadata = metadataCaptor.getValue();
            assertThat(metadata.getName()).isEqualTo("snippets");
            assertThat(metadata.getMimeType()).isEqualTo("application/vnd.google-apps.folder");
            assertThat(metadata.getParents()).containsExactly(CODESTORE_FOLDER_ID);
        }
    }

    @Test
    @DisplayName("calculates the etag from the modified time of the files")
    void getEtag() {
        snippetSet.getItemIds();
        String etag = snippetSet.getEtag(SNIPPET1_ID);
        assertThat(etag).isEqualTo("2025-11-02T12:21:54.000Z");
    }

    @Test
    @DisplayName("reads a snippet by downloading its file content from Drive")
    void getItem() throws Exception {
        when(files.get(anyString())).thenReturn(getRequest);
        doAnswer(invocation -> {
            ByteArrayOutputStream out = invocation.getArgument(0);
            out.write(DUMMY_CONTENT.getBytes(StandardCharsets.UTF_8));
            return null;
        }).when(getRequest).executeMediaAndDownloadTo(any());

        Snippet snippet = Snippet.builder().build();
        when(snippetReader.read(anyString(), anyString())).thenReturn(snippet);

        snippetSet.getItemIds();
        Snippet result = snippetSet.getItem(SNIPPET1_ID);
        assertThat(result).isSameAs(snippet);
        verify(snippetReader).read(SNIPPET1_ID, DUMMY_CONTENT);
    }

    @Nested
    @DisplayName("When creating a new file")
    class NewFile {
        @BeforeEach
        void setUp() throws IOException {
            when(snippetWriter.stringify(any(Snippet.class))).thenReturn(DUMMY_CONTENT);
            when(files.create(any(File.class), any())).thenReturn(createRequest);
        }

        @Test
        @DisplayName("sets created and modified timestamps")
        void addItem() throws Exception {
            Snippet snippet = Snippet.builder()
                                     .created(OffsetDateTime.parse("2025-01-01T00:00:00Z"))
                                     .modified(OffsetDateTime.parse("2025-01-02T03:04:05Z"))
                                     .build();

            snippetSet.getItemIds();
            snippetSet.addItem("snippet-3", snippet);

            var metaCaptor = ArgumentCaptor.forClass(File.class);
            var contentCaptor = ArgumentCaptor.forClass(AbstractInputStreamContent.class);
            verify(files).create(metaCaptor.capture(), contentCaptor.capture());
            File metadata = metaCaptor.getValue();
            assertThat(metadata.getName()).isEqualTo("snippet-3.json");
            assertThat(metadata.getParents()).containsExactly(SNIPPETS_FOLDER_ID);
            assertThat(metadata.getMimeType()).isEqualTo("application/json");
            assertThat(metadata.getCreatedTime()).isEqualTo(new DateTime("2025-01-01T00:00:00Z"));
            assertThat(metadata.getModifiedTime()).isEqualTo(new DateTime("2025-01-02T03:04:05Z"));

            var content = contentCaptor.getValue();
            byte[] bytes = content.getInputStream().readAllBytes();
            assertThat(new String(bytes, StandardCharsets.UTF_8)).isEqualTo(DUMMY_CONTENT);
            verify(createRequest).execute();
        }

        @Test
        @DisplayName("sets the creation time as modified time if that doesn't exist")
        void useCreatedTimeForModifiedTime() throws Exception {
            Snippet snippet = Snippet.builder()
                                     .created(OffsetDateTime.parse("2025-01-01T00:00:00Z"))
                                     .build();

            snippetSet.getItemIds();
            snippetSet.addItem("snippet-3", snippet);

            var metaCaptor = ArgumentCaptor.forClass(File.class);
            verify(files).create(metaCaptor.capture(), any());
            File metadata = metaCaptor.getValue();
            DateTime timestamp = new DateTime("2025-01-01T00:00:00Z");
            assertThat(metadata.getCreatedTime()).isEqualTo(timestamp);
            assertThat(metadata.getModifiedTime()).isEqualTo(timestamp);
        }
    }

    @Test
    @DisplayName("updates a file with the corresponding timestamp")
    void updateItem() throws Exception {
        Snippet snippet = Snippet.builder()
                                 .modified(OffsetDateTime.parse("2023-01-02T03:04:05Z"))
                                 .build();
        when(snippetWriter.stringify(any(Snippet.class))).thenReturn(DUMMY_CONTENT);
        when(files.update(anyString(), any(), any())).thenReturn(updateRequest);

        snippetSet.getItemIds();
        snippetSet.updateItem(SNIPPET1_ID, snippet);

        var metaCaptor = ArgumentCaptor.forClass(File.class);
        var contentCaptor = ArgumentCaptor.forClass(AbstractInputStreamContent.class);
        verify(files).update(eq(file1.getId()), metaCaptor.capture(), contentCaptor.capture());
        assertThat(metaCaptor.getValue().getModifiedTime()).isEqualTo(new DateTime("2023-01-02T03:04:05Z"));

        byte[] bytes = contentCaptor.getValue().getInputStream().readAllBytes();
        assertThat(new String(bytes, StandardCharsets.UTF_8)).isEqualTo(DUMMY_CONTENT);
    }

    @Test
    @DisplayName("deletes the Drive file when deleting a snippet")
    void deleteItem() throws Exception {
        when(files.delete(anyString())).thenReturn(deleteRequest);
        snippetSet.getItemIds();
        snippetSet.delete(SNIPPET1_ID);

        verify(files).delete(file1.getId());
    }
}