package cloud.codestore.core.repositories.snippets;

import cloud.codestore.core.repositories.File;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("The SnippetFileHelper")
class SnippetFileHelperTest {
    private static final String SNIPPET_ID = "12345";
    private static final String FILE_NAME = "12345.json";

    @Mock
    private File mockFile;

    @Test
    @DisplayName("converts a snippet ID to a file name")
    void getFileName() {
        String fileName = SnippetFileHelper.getFileName(SNIPPET_ID);
        assertThat(fileName).isEqualTo(FILE_NAME);
    }

    @Test
    @DisplayName("extracts a snippet ID from a file name")
    void getSnippetIdFromFileName() {
        String snippetId = SnippetFileHelper.getSnippetId(FILE_NAME);
        assertThat(snippetId).isEqualTo(SNIPPET_ID);
    }

    @Test
    @DisplayName("extracts a snippet ID from a File object")
    void getSnippetIdFromFile() {
        when(mockFile.getName()).thenReturn(FILE_NAME);
        String snippetId = SnippetFileHelper.getSnippetId(mockFile);
        assertThat(snippetId).isEqualTo(SNIPPET_ID);
    }
}