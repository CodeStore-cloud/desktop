package cloud.codestore.core.repositories.synchronization;

import cloud.codestore.core.Snippet;
import cloud.codestore.core.repositories.Directory;
import cloud.codestore.core.repositories.File;
import cloud.codestore.core.repositories.serialization.SnippetReader;
import cloud.codestore.core.repositories.serialization.SnippetWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("The local snippet set")
class LocalSnippetSetTest {
    private static final String SNIPPET_ID = "snippet-1";
    private static final String FILE_NAME =  SNIPPET_ID + ".json";

    @Mock
    private Directory snippetsDirectory;
    @Mock
    private File testFile;
    @Mock
    private SnippetReader snippetReader;
    @Mock
    private SnippetWriter snippetWriter;
    private LocalSnippetSet snippetSet;
    private Snippet testSnippet = Snippet.builder().build();

    @BeforeEach
    void setUp() {
        lenient().when(snippetsDirectory.getFile(FILE_NAME)).thenReturn(testFile);
        when(snippetsDirectory.getFiles()).thenReturn(List.of(
                new File(Path.of("snippet-1.json")),
                new File(Path.of("snippet-2.json")),
                new File(Path.of("snippet-3.json")),
                new File(Path.of("snippet-4.json")),
                new File(Path.of("snippet-5.json"))
        ));

        snippetSet = new LocalSnippetSet(snippetsDirectory, snippetReader, snippetWriter);
        snippetSet.getItemIds();
    }

    @Test
    @DisplayName("reads the IDs of all code snippets on the system")
    void readSnippetIds() {
        assertThat(snippetSet.getItemIds()).containsExactlyInAnyOrder(
                "snippet-1", "snippet-2", "snippet-3", "snippet-4", "snippet-5"
        );
    }

    @Test
    @DisplayName("checks whether a certain snippet exists on the system")
    void containsSnippet() {
        assertThat(snippetSet.contains("snippet-0")).isFalse();
        assertThat(snippetSet.contains("snippet-1")).isTrue();
        assertThat(snippetSet.contains("snippet-5")).isTrue();
        assertThat(snippetSet.contains("snippet-6")).isFalse();
    }

    @Test
    @DisplayName("reads a code snippet from the file system")
    void readSnippet() {
        snippetSet.getItem(SNIPPET_ID);
        verify(snippetReader).read(testFile);
    }

    @Test
    @DisplayName("creates a new snippet on the system and adds it to the report")
    void createSnippet() {
        snippetSet.addItem(SNIPPET_ID, testSnippet);
        verify(snippetWriter).write(testSnippet, testFile);
    }

    @Test
    @DisplayName("updates a snippet on the system and adds it to the report")
    void updateSnippet() {
        snippetSet.updateItem(SNIPPET_ID, testSnippet);
        verify(snippetWriter).write(testSnippet, testFile);
    }

    @Test
    @DisplayName("deletes a snippet on the system and adds it to the report")
    void deleteSnippet() throws Exception {
        snippetSet.delete(SNIPPET_ID);
        verify(testFile).delete();
    }

    @Nested
    @DisplayName("calculates the etag")
    class EtagTest {
        private static final OffsetDateTime CREATED_TIME = OffsetDateTime.now();
        private static final OffsetDateTime MODIFIED_TIME = CREATED_TIME.plusHours(3);

        @Test
        @DisplayName("from the modified-timestamp")
        void modifiedTimestamp() {
            testSnippet = Snippet.builder().modified(MODIFIED_TIME).build();
            when(snippetReader.read(testFile)).thenReturn(testSnippet);

            String etag = snippetSet.getEtag(SNIPPET_ID);
            String expectedEtag = MODIFIED_TIME.truncatedTo(ChronoUnit.SECONDS)
                                               .withOffsetSameInstant(ZoneOffset.UTC)
                                               .toString();
            assertThat(etag).isEqualTo(expectedEtag);
        }

        @Test
        @DisplayName("from the creation-timestamp if the modified-timestamp is not set")
        void creationTimestamp() {
            testSnippet = Snippet.builder().created(CREATED_TIME).build();
            when(snippetReader.read(testFile)).thenReturn(testSnippet);

            String etag = snippetSet.getEtag(SNIPPET_ID);
            String expectedEtag = CREATED_TIME.truncatedTo(ChronoUnit.SECONDS)
                                              .withOffsetSameInstant(ZoneOffset.UTC)
                                              .toString();
            assertThat(etag).isEqualTo(expectedEtag);
        }
    }
}