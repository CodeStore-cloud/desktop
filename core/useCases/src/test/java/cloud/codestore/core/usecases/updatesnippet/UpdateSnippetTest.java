package cloud.codestore.core.usecases.updatesnippet;

import cloud.codestore.core.Language;
import cloud.codestore.core.SnippetNotExistsException;
import cloud.codestore.core.SnippetRepository;
import cloud.codestore.core.validation.SnippetValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("The update-snippet use case")
class UpdateSnippetTest {
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
}