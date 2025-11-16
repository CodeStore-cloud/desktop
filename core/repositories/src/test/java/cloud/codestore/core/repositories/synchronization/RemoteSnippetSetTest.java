package cloud.codestore.core.repositories.synchronization;


import cloud.codestore.core.Snippet;
import cloud.codestore.core.SnippetBuilder;
import cloud.codestore.core.repositories.serialization.SnippetFileHelper;
import cloud.codestore.core.repositories.serialization.SnippetReader;
import cloud.codestore.core.repositories.serialization.SnippetWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("The remote snippet set")
class RemoteSnippetSetTest {
    private static final String SNIPPET_ID = UUID.randomUUID().toString();

    @Mock
    private SnippetReader snippetReader;
    @Mock
    private SnippetWriter snippetWriter;
    @Mock
    private RemoteDirectory codestoreDirectory;
    @Mock
    private RemoteDirectory snippetsDirectory;
    @Mock
    private RemoteFile remoteFile;
    private RemoteSnippetSet snippetSet;

    @BeforeEach
    void setUp() {
        when(codestoreDirectory.exists()).thenReturn(true);
        when(snippetsDirectory.exists()).thenReturn(true);
        when(codestoreDirectory.getSubDirectory("snippets")).thenReturn(snippetsDirectory);
    }

    @Test
    @DisplayName("loads the snippet IDs and Metadata from the remote system")
    void getItemIds() {
        String[] snippetIds = {"1", "2", "3", "4", "5"};
        List<RemoteFile> files = Stream.of(snippetIds)
                                       .map(id -> {
                                           RemoteFile file = mock(RemoteFile.class);
                                           when(file.getName()).thenReturn(SnippetFileHelper.getFileName(id));
                                           return file;
                                       })
                                       .toList();
        when(snippetsDirectory.getFiles()).thenReturn(files);

        snippetSet = new RemoteSnippetSet(snippetReader, snippetWriter, codestoreDirectory);
        Set<String> itemIds = snippetSet.getItemIds();

        assertThat(itemIds).containsExactlyInAnyOrder(snippetIds);
        for (String id : itemIds) {
            assertThat(snippetSet.contains(id)).isTrue();
        }
    }

    @Test
    @DisplayName("calculates the etag from the modified timestamp")
    void getEtag() {
        initSnippetSet();
        when(remoteFile.getModified()).thenReturn(OffsetDateTime.parse("2025-11-08T13:34:35.2415+01:00"));
        String etag = snippetSet.getEtag(SNIPPET_ID);
        assertThat(etag).isEqualTo("2025-11-08T12:34:35Z");
    }

    @Test
    @DisplayName("reads the content of the code snippet")
    void getItem() {
        initSnippetSet();
        Snippet snippet = Snippet.builder()
                                 .id(SNIPPET_ID)
                                 .title("Hello, World!")
                                 .build();
        when(snippetReader.read(remoteFile)).thenReturn(snippet);

        Snippet readSnippet = snippetSet.getItem(SNIPPET_ID);
        assertThat(readSnippet).isSameAs(snippet);
    }

    @Test
    @DisplayName("deletes a code snippet on the remote system")
    void deleteItem() {
        initSnippetSet();
        snippetSet.delete(SNIPPET_ID);
        verify(remoteFile).delete();
    }

    @Nested
    @DisplayName("when created")
    class AfterCreation {
        @Test
        @DisplayName("creates the CodeStore directory if it doesn't exist")
        void createMainDirectory() {
            when(codestoreDirectory.exists()).thenReturn(false);
            new RemoteSnippetSet(snippetReader, snippetWriter, codestoreDirectory);
            verify(codestoreDirectory).create();
        }

        @Test
        @DisplayName("creates the snippets directory if it doesn't exist")
        void createSnippetsDirectory() {
            when(snippetsDirectory.exists()).thenReturn(false);
            new RemoteSnippetSet(snippetReader, snippetWriter, codestoreDirectory);
            verify(snippetsDirectory).create();
        }
    }

    @Nested
    @DisplayName("when adding a code snippet")
    class CreateSnippet {
        private final OffsetDateTime created = OffsetDateTime.now();
        private final OffsetDateTime modified = OffsetDateTime.now().plusDays(2);
        private final SnippetBuilder builder = Snippet.builder().id(SNIPPET_ID).created(created);

        @BeforeEach
        void setUp() {
            initEmptySnippetSet();
            when(snippetsDirectory.newFile(anyString())).thenReturn(remoteFile);
        }

        @Test
        @DisplayName("sets the modified timestamp if it exists")
        void setModifiedTimestamp() {
            Snippet snippet = builder.modified(modified).build();

            snippetSet.addItem(SNIPPET_ID, snippet);

            verify(remoteFile).setCreated(truncatedToSecondsUTC(created));
            verify(remoteFile).setModified(truncatedToSecondsUTC(modified));
            verify(snippetWriter).write(snippet, remoteFile);
        }

        @Test
        @DisplayName("sets the creation timestamp as modified timestamp if it doesn't exist")
        void setCreatedTimestampAsModifiedTimestamp() {
            Snippet snippet = builder.build();

            snippetSet.addItem(SNIPPET_ID, snippet);

            OffsetDateTime timestamp = truncatedToSecondsUTC(created);
            verify(remoteFile).setCreated(timestamp);
            verify(remoteFile).setModified(timestamp);
        }
    }

    @Nested
    @DisplayName("when updating a code snippet")
    class UpdateSnippet {
        private final OffsetDateTime created = OffsetDateTime.now();
        private final OffsetDateTime modified = OffsetDateTime.now().plusDays(2);
        private final SnippetBuilder builder = Snippet.builder().id(SNIPPET_ID).created(created);

        @BeforeEach
        void setUp() {
            initSnippetSet();
        }

        @Test
        @DisplayName("sets the modified timestamp if it exists")
        void setModifiedTimestamp() {
            Snippet snippet = builder.modified(modified).build();

            snippetSet.updateItem(SNIPPET_ID, snippet);

            verify(remoteFile, never()).setCreated(any());
            verify(remoteFile).setModified(truncatedToSecondsUTC(modified));
            verify(snippetWriter).write(snippet, remoteFile);
        }

        @Test
        @DisplayName("sets the creation timestamp as modified timestamp if it doesn't exist")
        void setCreatedTimestampAsModifiedTimestamp() {
            Snippet snippet = builder.build();

            snippetSet.updateItem(SNIPPET_ID, snippet);

            verify(remoteFile, never()).setCreated(any());
            verify(remoteFile).setModified(truncatedToSecondsUTC(created));
            verify(snippetWriter).write(snippet, remoteFile);
        }
    }

    private void initSnippetSet() {
        when(remoteFile.getName()).thenReturn(SnippetFileHelper.getFileName(SNIPPET_ID));
        when(snippetsDirectory.getFiles()).thenReturn(List.of(remoteFile));
        snippetSet = new RemoteSnippetSet(snippetReader, snippetWriter, codestoreDirectory);
        snippetSet.getItemIds();
    }

    private void initEmptySnippetSet() {
        snippetSet = new RemoteSnippetSet(snippetReader, snippetWriter, codestoreDirectory);
    }

    private OffsetDateTime truncatedToSecondsUTC(OffsetDateTime time) {
        return time.truncatedTo(ChronoUnit.SECONDS).withOffsetSameInstant(ZoneOffset.UTC);
    }
}