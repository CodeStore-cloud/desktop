package cloud.codestore.core.repositories.synchronization;

import cloud.codestore.core.Snippet;
import cloud.codestore.core.SnippetNotExistsException;
import cloud.codestore.core.repositories.Directory;
import cloud.codestore.core.repositories.File;
import cloud.codestore.core.usecases.createsnippet.CreateSnippetQuery;
import cloud.codestore.core.usecases.deletesnippet.DeleteSnippetQuery;
import cloud.codestore.core.usecases.readsnippet.ReadSnippetQuery;
import cloud.codestore.core.usecases.synchronizesnippets.SynchronizationReport;
import cloud.codestore.core.usecases.updatesnippet.UpdateSnippetQuery;
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

    @Mock
    private Directory snippetsDirectory;
    @Mock
    private ReadSnippetQuery readSnippetQuery;
    @Mock
    private CreateSnippetQuery createSnippetQuery;
    @Mock
    private DeleteSnippetQuery deleteSnippetQuery;
    @Mock
    private UpdateSnippetQuery updateSnippetQuery;
    @Mock
    private SynchronizationReport syncReport;
    private LocalSnippetSet snippetSet;
    private Snippet testSnippet = Snippet.builder().build();

    @BeforeEach
    void setUp() throws SnippetNotExistsException {
        lenient().when(readSnippetQuery.read(anyString())).thenReturn(testSnippet);
        when(snippetsDirectory.getFiles()).thenReturn(List.of(
                new File(Path.of("snippet-1.json")),
                new File(Path.of("snippet-2.json")),
                new File(Path.of("snippet-3.json")),
                new File(Path.of("snippet-4.json")),
                new File(Path.of("snippet-5.json"))
        ));

        snippetSet = new LocalSnippetSet(
                snippetsDirectory,
                readSnippetQuery,
                createSnippetQuery,
                deleteSnippetQuery,
                updateSnippetQuery,
                syncReport
        );
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
    @DisplayName("creates a new snippet on the system and adds it to the report")
    void createSnippet() {
        snippetSet.addItem(SNIPPET_ID, testSnippet);
        verify(createSnippetQuery).create(testSnippet);
        verify(syncReport).snippetCreatedLocally(testSnippet);
    }

    @Test
    @DisplayName("updates a snippet on the system and adds it to the report")
    void updateSnippet() throws Exception {
        snippetSet.updateItem(SNIPPET_ID, testSnippet);
        verify(updateSnippetQuery).update(testSnippet);
        verify(syncReport).snippetUpdatedLocally(testSnippet);
    }

    @Test
    @DisplayName("deletes a snippet on the system and adds it to the report")
    void deleteSnippet() throws Exception {
        snippetSet.delete(SNIPPET_ID);
        verify(deleteSnippetQuery).delete(SNIPPET_ID);
        verify(syncReport).snippetDeletedLocally(testSnippet);
    }

    @Nested
    @DisplayName("calculates the etag")
    class EtagTest {
        private static final OffsetDateTime CREATED_TIME = OffsetDateTime.now();
        private static final OffsetDateTime MODIFIED_TIME = CREATED_TIME.plusHours(3);

        @Test
        @DisplayName("from the modified-timestamp")
        void modifiedTimestamp() throws Exception {
            testSnippet = Snippet.builder().modified(MODIFIED_TIME).build();
            when(readSnippetQuery.read(anyString())).thenReturn(testSnippet);

            String etag = snippetSet.getEtag(SNIPPET_ID);
            String expectedEtag = MODIFIED_TIME.truncatedTo(ChronoUnit.SECONDS)
                                               .withOffsetSameInstant(ZoneOffset.UTC)
                                               .toString();
            assertThat(etag).isEqualTo(expectedEtag);
        }

        @Test
        @DisplayName("from the creation-timestamp if the modified-timestamp is not set")
        void creationTimestamp() throws Exception {
            testSnippet = Snippet.builder().created(CREATED_TIME).build();
            when(readSnippetQuery.read(anyString())).thenReturn(testSnippet);

            String etag = snippetSet.getEtag(SNIPPET_ID);
            String expectedEtag = CREATED_TIME.truncatedTo(ChronoUnit.SECONDS)
                                              .withOffsetSameInstant(ZoneOffset.UTC)
                                              .toString();
            assertThat(etag).isEqualTo(expectedEtag);
        }
    }
}