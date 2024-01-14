package cloud.codestore.core.usecases.readsnippet;

import cloud.codestore.core.Language;
import cloud.codestore.core.Snippet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("The read-snippet use case")
class ReadSnippetTest {
    @Mock
    private ReadSnippetQuery query;
    private ReadSnippet useCase;

    @BeforeEach
    void setUp() {
        useCase = new ReadSnippet(query);
    }

    @Test
    @DisplayName("reads a code snippet from the repository")
    void returnSnippet() throws Exception {
        var expectedSnippet = testSnippet();
        var snippetId = expectedSnippet.getId();
        when(query.read(snippetId)).thenReturn(expectedSnippet);

        var snippet = useCase.read(snippetId);
        assertThat(snippet).isEqualTo(expectedSnippet);
    }

    private Snippet testSnippet() {
        return Snippet.builder()
                      .id(UUID.randomUUID().toString())
                      .language(Language.TEXT)
                      .title("title")
                      .code("code")
                      .created(OffsetDateTime.now())
                      .build();
    }
}