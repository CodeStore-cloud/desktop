package cloud.codestore.core.usecases.updatesnippet;

import cloud.codestore.core.SnippetNotExistsException;
import cloud.codestore.core.SnippetRepository;
import cloud.codestore.core.validation.SnippetValidator;

import javax.annotation.Nonnull;

/**
 * Use case: update an existing code snippet.
 */
public class UpdateSnippet {
    private final SnippetRepository repository;
    private final SnippetValidator validator;

    public UpdateSnippet(SnippetRepository repository, SnippetValidator validator) {
        this.repository = repository;
        this.validator = validator;
    }

    public void update(@Nonnull UpdatedSnippetDto dto) throws SnippetNotExistsException {
        if (!repository.contains(dto.id())) {
            throw new SnippetNotExistsException();
        }


    }
}
