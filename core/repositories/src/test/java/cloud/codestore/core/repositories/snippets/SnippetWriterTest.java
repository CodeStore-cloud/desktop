package cloud.codestore.core.repositories.snippets;

import cloud.codestore.core.Language;
import cloud.codestore.core.Snippet;
import cloud.codestore.core.repositories.DefaultLocale;
import cloud.codestore.core.repositories.File;
import cloud.codestore.core.repositories.RepositoryException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.mockito.Mockito.*;

@ExtendWith(DefaultLocale.class)
@ExtendWith(MockitoExtension.class)
@DisplayName("A snippet writer")
class SnippetWriterTest {
    @Mock
    private File snippetFile;
    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();
    private SnippetWriter snippetWriter;

    @BeforeEach
    void setUp() {
        snippetWriter = new SnippetWriter(objectMapper);
    }

    @Test
    @DisplayName("writes a snippet to a file")
    void writeSnippet() {
        Snippet snippet = testSnippet();
        snippetWriter.write(snippet, snippetFile);
        verify(snippetFile).write(expectedOutput());
    }

    @Test
    @DisplayName("throws a RepositoryException if the file could not be saved")
    void throwRepositoryException() throws JsonProcessingException {
        when(snippetFile.toString()).thenReturn("test.json");
        when(objectMapper.writeValueAsString(any())).thenThrow(JsonProcessingException.class);
        Assertions.assertThatThrownBy(() -> snippetWriter.write(testSnippet(), snippetFile))
                  .isInstanceOf(RepositoryException.class)
                  .hasMessage("The file test.json could not be saved.");
    }

    private Snippet testSnippet() {
        var created = OffsetDateTime.of(2023, 6, 24, 14, 47, 28, 100, ZoneOffset.UTC);
        var modified = OffsetDateTime.of(2023, 7, 13, 9, 15, 53, 20, ZoneOffset.UTC);

        return Snippet.builder().id("")
                                   .title("A random title")
                                   .description("A random description")
                                   .code("System.out.println(\"Hello, World!\")")
                                   .language(Language.JAVA)
                                   .created(created)
                                   .modified(modified)
                                   .build();
    }

    private String expectedOutput() {
        return "{\"language\":10,\"title\":\"A random title\",\"description\":\"A random description\",\"code\":\"System.out.println(\\\"Hello, World!\\\")\",\"created\":\"2023-06-24T14:47:28Z\",\"modified\":\"2023-07-13T09:15:53Z\"}";
    }
}