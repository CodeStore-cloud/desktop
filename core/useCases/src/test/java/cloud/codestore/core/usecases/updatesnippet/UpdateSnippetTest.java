package cloud.codestore.core.usecases.updatesnippet;

import cloud.codestore.core.Language;
import cloud.codestore.core.Snippet;
import cloud.codestore.core.SnippetNotExistsException;
import cloud.codestore.core.usecases.readsnippet.ReadSnippet;
import cloud.codestore.core.validation.SnippetValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static java.time.temporal.ChronoUnit.SECONDS;
import static java.time.temporal.ChronoUnit.WEEKS;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("The update-snippet use case")
class UpdateSnippetTest {
    private static final String SNIPPET_ID = UUID.randomUUID().toString();

    @Mock
    private ReadSnippet readSnippetUseCase;
    @Mock
    private UpdateSnippetQuery query;
    @Mock
    private SnippetValidator validator;
    private UpdateSnippet useCase;

    @BeforeEach
    void setUp() {
        useCase = new UpdateSnippet(readSnippetUseCase, query, validator);
    }

    @Test
    @DisplayName("throws a SnippetNotExistsException if the code snippet does not exist")
    void snippetNotExist() throws SnippetNotExistsException {
        String snippetId = UUID.randomUUID().toString();
        when(readSnippetUseCase.read(snippetId)).thenThrow(new SnippetNotExistsException());
        UpdatedSnippetDto dto = new UpdatedSnippetDto(snippetId, Language.TEXT, "", "", null, "");
        assertThatThrownBy(() -> useCase.update(dto)).isInstanceOf(SnippetNotExistsException.class);
    }

    @DisplayName("puts the updated code snippet into the repository")
    @Test
    void passSnippetToRepository() throws Exception {
        ArgumentCaptor<Snippet> snippetArgument = ArgumentCaptor.forClass(Snippet.class);

        Snippet currentSnippet = currentSnippet();
        when(readSnippetUseCase.read(SNIPPET_ID)).thenReturn(currentSnippet);

        UpdatedSnippetDto dto = updatedSnippet();
        useCase.update(dto);

        verify(validator).validate(snippetArgument.capture());
        verify(query).update(snippetArgument.capture());
        Snippet snippet = snippetArgument.getValue();
        assertThat(snippet.getId()).isEqualTo(SNIPPET_ID);
        assertThat(snippet.getLanguage()).isEqualTo(dto.language());
        assertThat(snippet.getTitle()).isEqualTo(dto.title());
        assertThat(snippet.getCode()).isEqualTo(dto.code());
        assertThat(snippet.getDescription()).isEqualTo(dto.description());
        assertThat(snippet.getTags()).containsExactlyInAnyOrder("other", "tags");
        assertThat(snippet.getCreated()).isEqualTo(currentSnippet.getCreated());
        assertThat(snippet.getModified()).isCloseTo(OffsetDateTime.now(), within(3, SECONDS));
    }

    private Snippet currentSnippet() {
        OffsetDateTime now = OffsetDateTime.now().truncatedTo(SECONDS);
        OffsetDateTime fiveWeeksAgo = now.minus(5, WEEKS);
        return Snippet.builder()
                      .id(SNIPPET_ID)
                      .language(Language.JAVASCRIPT)
                      .title("title")
                      .description("description")
                      .code("code")
                      .tags(List.of("hello", "world"))
                      .created(fiveWeeksAgo)
                      .build();
    }

    private static UpdatedSnippetDto updatedSnippet() {
        return new UpdatedSnippetDto(
                SNIPPET_ID,
                Language.JAVA,
                "new title",
                "new code",
                List.of("other", "tags"),
                "new description"
        );
    }
}