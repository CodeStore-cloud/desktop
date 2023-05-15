package cloud.codestore.core.usecases.updatesnippet;

import cloud.codestore.core.*;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("The update-snippet use case")
class UpdateSnippetTest {
    private static final String SNIPPET_ID = UUID.randomUUID().toString();

    @Mock
    private SnippetRepository repository;
    @Mock
    private SnippetValidator validator;
    private UpdateSnippet useCase;

    @BeforeEach
    void setUp() {
        useCase = new UpdateSnippet(repository, validator);
    }

    @Test
    @DisplayName("throws a SnippetNotExistsException if the code snippet does not exist")
    void snippetNotExist() {
        String snippetId = UUID.randomUUID().toString();
        when(repository.contains(snippetId)).thenReturn(false);
        UpdatedSnippetDto dto = new UpdatedSnippetDto(snippetId, Language.TEXT, "", "", "");
        assertThatThrownBy(() -> useCase.update(dto)).isInstanceOf(SnippetNotExistsException.class);
    }

    @DisplayName("puts the updated code snippet into the repository")
    @Test
    void passSnippetToRepository() throws Exception {
        OffsetDateTime now = OffsetDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        ArgumentCaptor<Snippet> snippetArgument = ArgumentCaptor.forClass(Snippet.class);

        Snippet currentSnippet = currentSnippet();
        when(repository.contains(SNIPPET_ID)).thenReturn(true);
        when(repository.get(SNIPPET_ID)).thenReturn(currentSnippet);

        UpdatedSnippetDto dto = updatedSnippet();
        useCase.update(dto);

        verify(validator).validate(snippetArgument.capture());
        verify(repository).put(snippetArgument.capture());
        Snippet snippet = snippetArgument.getValue();
        assertThat(snippet.getId()).isEqualTo(SNIPPET_ID);
        assertThat(snippet.getLanguage()).isEqualTo(dto.language());
        assertThat(snippet.getTitle()).isEqualTo(dto.title());
        assertThat(snippet.getCode()).isEqualTo(dto.code());
        assertThat(snippet.getDescription()).isEqualTo(dto.description());
        assertThat(snippet.getCreated()).isEqualTo(currentSnippet.getCreated());
        assertThat(snippet.getModified()).isEqualTo(now);
    }

    private Snippet currentSnippet() {
        OffsetDateTime now = OffsetDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        OffsetDateTime fiveWeeksAgo = now.minus(5, ChronoUnit.WEEKS);
        return new SnippetBuilder()
                .id(SNIPPET_ID)
                .language(Language.JAVASCRIPT)
                .title("title")
                .description("description")
                .code("code")
                .created(fiveWeeksAgo)
                .build();
    }

    private static UpdatedSnippetDto updatedSnippet() {
        return new UpdatedSnippetDto(
                SNIPPET_ID,
                Language.JAVA,
                "new title",
                "new code",
                "new description"
        );
    }
}