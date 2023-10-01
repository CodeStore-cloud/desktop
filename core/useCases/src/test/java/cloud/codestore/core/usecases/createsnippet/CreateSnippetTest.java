package cloud.codestore.core.usecases.createsnippet;

import cloud.codestore.core.Language;
import cloud.codestore.core.Snippet;
import cloud.codestore.core.validation.InvalidSnippetException;
import cloud.codestore.core.validation.SnippetValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("The create-snippet use case")
class CreateSnippetTest {
    @Mock
    private CreateSnippetQuery query;
    @Mock
    private SnippetValidator validator;
    private CreateSnippet useCase;

    @BeforeEach
    void setUp() {
        useCase = new CreateSnippet(query, validator);
    }

    @Test
    @DisplayName("puts the new code snippet into the repository")
    void createSnippet() throws InvalidSnippetException {
        var now = OffsetDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        var snippetArgument = ArgumentCaptor.forClass(Snippet.class);

        var dto = createDto();
        useCase.create(dto);

        verify(validator).validate(snippetArgument.capture());
        verify(query).create(snippetArgument.capture());
        Snippet snippet = snippetArgument.getValue();
        assertThat(snippet.getId()).isNotNull();
        assertThat(snippet.getLanguage()).isEqualTo(dto.language());
        assertThat(snippet.getTitle()).isEqualTo(dto.title());
        assertThat(snippet.getCode()).isEqualTo(dto.code());
        assertThat(snippet.getDescription()).isEqualTo(dto.description());
        assertThat(snippet.getTags()).containsExactlyInAnyOrder("hello", "world");
        assertThat(snippet.getCreated()).isEqualTo(now);
        assertThat(snippet.getModified()).isNull();
    }

    private NewSnippetDto createDto() {
        return new NewSnippetDto(
                Language.JAVA,
                "hello world",
                "System.out.println(\"Hello, World!\")",
                List.of("hello", "world"),
                "A Java hello world example."
        );
    }
}