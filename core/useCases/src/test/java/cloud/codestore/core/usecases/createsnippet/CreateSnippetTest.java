package cloud.codestore.core.usecases.createsnippet;

import cloud.codestore.core.Language;
import cloud.codestore.core.Snippet;
import cloud.codestore.core.SnippetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("The create-snippet use case")
class CreateSnippetTest {
    @Mock
    private SnippetRepository repository;
    private CreateSnippet useCase;

    @BeforeEach
    void setUp() {
        useCase = new CreateSnippet(repository);
    }

    @Test
    @DisplayName("puts the new code snippet into the repository")
    void createSnippet() {
        var now = OffsetDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        var snippetArgument = ArgumentCaptor.forClass(Snippet.class);

        var dto = createDto();
        useCase.create(dto);

        verify(repository).put(snippetArgument.capture());
        Snippet snippet = snippetArgument.getValue();
        assertThat(snippet.id()).isNotNull();
        assertThat(snippet.language()).isEqualTo(dto.language());
        assertThat(snippet.title()).isEqualTo(dto.title());
        assertThat(snippet.code()).isEqualTo(dto.code());
        assertThat(snippet.description()).isEqualTo(dto.description());
        assertThat(snippet.created()).isEqualTo(now);
        assertThat(snippet.modified()).isNull();
    }

    private NewSnippetDto createDto() {
        return new NewSnippetDto(
                Language.JAVA,
                "hello world",
                "System.out.println(\"Hello, World!\")",
                "A Java hello world example."
        );
    }
}