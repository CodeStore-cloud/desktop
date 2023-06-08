package cloud.codestore.core.usecases.updatesnippet;

import cloud.codestore.core.*;
import cloud.codestore.core.validation.InvalidSnippetException;
import cloud.codestore.core.validation.SnippetValidator;

import javax.annotation.Nonnull;
import java.time.OffsetDateTime;

/**
 * Use case: update a code snippet.
 */
@UseCase
public class UpdateSnippet {
    private final SnippetRepository repository;
    private final SnippetValidator validator;

    public UpdateSnippet(SnippetRepository repository, SnippetValidator validator) {
        this.repository = repository;
        this.validator = validator;
    }

    /**
     * Updates the code snippet represented by the given dto.
     *
     * @param dto a dto representing the snippet to update.
     *
     * @throws SnippetNotExistsException if the code snippet does not exist.
     * @throws InvalidSnippetException if the code snippet is invalid.
     */
    public void update(@Nonnull UpdatedSnippetDto dto) throws SnippetNotExistsException, InvalidSnippetException {
        if (!repository.contains(dto.id())) {
            throw new SnippetNotExistsException();
        }

        Snippet currentSnippet = repository.get(dto.id());
        Snippet updatedSnippet = new SnippetBuilder().id(currentSnippet.getId())
                                                     .created(currentSnippet.getCreated())
                                                     .modified(OffsetDateTime.now())
                                                     .language(dto.language())
                                                     .title(dto.title())
                                                     .code(dto.code())
                                                     .description(dto.description())
                                                     .build();

        validator.validate(updatedSnippet);
        repository.put(updatedSnippet);
    }
}
