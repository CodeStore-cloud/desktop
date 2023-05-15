package cloud.codestore.core.usecases.readsnippet;

import cloud.codestore.core.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("The read-snippet use case")
class ReadSnippetTest {
    @Mock
    private SnippetRepository repository;
    private ReadSnippet useCase;

    @BeforeEach
    void setUp() {
        useCase = new ReadSnippet(repository);
    }

    @Test
    @DisplayName("reads a code snippet from the repository")
    void returnSnippet() throws Exception {
        var expectedSnippet = testSnippet();
        var snippetId = expectedSnippet.getId();
        when(repository.contains(snippetId)).thenReturn(true);
        when(repository.get(snippetId)).thenReturn(expectedSnippet);

        var snippet = useCase.read(snippetId);
        assertThat(snippet).isEqualTo(expectedSnippet);
    }

    @Test
    @DisplayName("throws a SnippetNotExistsException if the code snippet does not exist")
    void snippetNotExist() {
        String snippetId = UUID.randomUUID().toString();
        when(repository.contains(snippetId)).thenReturn(false);
        assertThatThrownBy(() -> useCase.read(snippetId)).isInstanceOf(SnippetNotExistsException.class);
    }

    private Snippet testSnippet() {
        return new SnippetBuilder()
                .id(UUID.randomUUID().toString())
                .language(Language.TEXT)
                .title("title")
                .code("code")
                .created(OffsetDateTime.now())
                .build();
    }
}