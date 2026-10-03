package cloud.codestore.core.repositories.serialization;

import cloud.codestore.core.Language;
import cloud.codestore.core.Snippet;
import cloud.codestore.core.repositories.DefaultLocale;
import cloud.codestore.core.repositories.File;
import cloud.codestore.core.repositories.RepositoryException;
import cloud.codestore.core.usecases.synchronizesnippets.SnippetSynchronizationState;
import cloud.codestore.core.usecases.synchronizesnippets.SnippetSynchronizations;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.Optional;

import static java.time.temporal.ChronoUnit.SECONDS;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(DefaultLocale.class)
@ExtendWith(MockitoExtension.class)
@DisplayName("A snippet reader")
class SnippetReaderTest {
    private static final String SNIPPET_ID = "12345";

    @Mock
    private File testFile;
    @Mock
    private SnippetSynchronizations syncStates;
    private SnippetReader snippetReader;

    @BeforeEach
    void setUp() {
        snippetReader = new SnippetReader(new ObjectMapper(), syncStates);
        lenient().when(testFile.getName()).thenReturn(SnippetFileHelper.getFileName(SNIPPET_ID));
    }

    @Test
    @DisplayName("reads a snippet from a file")
    void readSnippet() {
        String fileContent = """
                {
                "title":"Test Snippet",
                "description":"This is a test snippet",
                "code":"System.out.println(\\"Hello, World!\\");",
                "tags":["test", "hello", "world"],
                "language":10,
                "created":"2022-06-25T10:55:45Z",
                "modified":"2022-12-02T14:30:26Z"
                }""";
        when(testFile.readOrElse(anyString())).thenReturn(fileContent);
        var syncState = mock(SnippetSynchronizationState.class);
        when(syncStates.getOptional(SNIPPET_ID)).thenReturn(Optional.of(syncState));

        Snippet snippet = snippetReader.read(testFile);

        assertThat(snippet).isNotNull();
        assertThat(snippet.getId()).isEqualTo(SNIPPET_ID);
        assertThat(snippet.getTitle()).isEqualTo("Test Snippet");
        assertThat(snippet.getDescription()).isEqualTo("This is a test snippet");
        assertThat(snippet.getCode()).isEqualTo("System.out.println(\"Hello, World!\");");
        assertThat(snippet.getTags()).containsExactlyInAnyOrder("test", "hello", "world");
        assertThat(snippet.getLanguage()).isEqualTo(Language.JAVA);
        assertThat(snippet.getCreated()).isEqualTo(OffsetDateTime.parse("2022-06-25T10:55:45Z"));
        assertThat(snippet.getModified()).isEqualTo(OffsetDateTime.parse("2022-12-02T14:30:26Z"));
        assertThat(snippet.getSynchronizationState()).isPresent().containsSame(syncState);
    }

    @Test
    @DisplayName("sets default values if the file does not exist")
    void readSnippetEmptyFile() {
        when(testFile.readOrElse(anyString())).thenReturn("{}");
        when(syncStates.getOptional(SNIPPET_ID)).thenReturn(Optional.empty());

        Snippet snippet = snippetReader.read(testFile);

        assertThat(snippet).isNotNull();
        assertThat(snippet.getId()).isEqualTo(SNIPPET_ID);
        assertThat(snippet.getTitle()).isEmpty();
        assertThat(snippet.getDescription()).isEmpty();
        assertThat(snippet.getCode()).isEmpty();
        assertThat(snippet.getTags()).isEmpty();
        assertThat(snippet.getLanguage()).isEqualTo(Language.TEXT);
        assertThat(snippet.getCreated()).isCloseTo(OffsetDateTime.now(), within(3, SECONDS));
        assertThat(snippet.getModified()).isNull();
        assertThat(snippet.getSynchronizationState()).isEmpty();
    }

    @Test
    @DisplayName("throws a RepositoryException if the file content is not JSON")
    void invalidFormat() {
        when(testFile.path()).thenReturn(Path.of("test.json"));
        when(testFile.readOrElse(anyString())).thenReturn("invalid-json");

        assertThatThrownBy(() -> snippetReader.read(testFile))
                .isInstanceOf(RepositoryException.class)
                .hasMessage("The format of the file test.json is invalid.");
    }

    @Test
    @DisplayName("preserves additional properties")
    void preserveAdditionalProperties() {
        String fileContent = """
                {
                  "title":"Test Snippet",
                  "additionalProperty":"Hello, World!"
                }""";
        when(testFile.readOrElse(anyString())).thenReturn(fileContent);

        Snippet snippet = snippetReader.read(testFile);

        assertThat(snippet).isNotNull();
        assertThat(snippet).isInstanceOf(ExtendedSnippet.class);
        ExtendedSnippet extendedSnippet = (ExtendedSnippet) snippet;
        assertThat(extendedSnippet.getAdditionalProperties()).containsEntry("additionalProperty", "Hello, World!");
    }
}