package cloud.codestore.core.repositories.snippets;

import cloud.codestore.core.Language;
import cloud.codestore.core.Snippet;
import cloud.codestore.core.repositories.DefaultLocale;
import cloud.codestore.core.repositories.File;
import cloud.codestore.core.repositories.RepositoryException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.time.OffsetDateTime;

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
    private SnippetReader snippetReader;

    @BeforeEach
    void setUp() {
        snippetReader = new SnippetReader(new ObjectMapper());
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
    }

    @Test
    @DisplayName("sets default values if the file does not exist")
    void readSnippetEmptyFile() {
        when(testFile.readOrElse(anyString())).thenReturn("{}");

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
}