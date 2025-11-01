package cloud.codestore.core.usecases.updatesnippet;

import cloud.codestore.core.Injectable;
import cloud.codestore.core.Snippet;
import cloud.codestore.core.SnippetNotExistsException;
import cloud.codestore.core.usecases.readsnippet.ReadSnippet;
import cloud.codestore.core.validation.InvalidSnippetException;
import cloud.codestore.core.validation.SnippetValidator;

import javax.annotation.Nonnull;
import java.time.OffsetDateTime;

/**
 * Use case: update a code snippet.
 */
@Injectable
public class UpdateSnippet {
    private final ReadSnippet readSnippetUseCase;
    private final UpdateSnippetQuery query;
    private final SnippetValidator validator;

    public UpdateSnippet(ReadSnippet readSnippetUseCase, UpdateSnippetQuery query, SnippetValidator validator) {
        this.readSnippetUseCase = readSnippetUseCase;
        this.query = query;
        this.validator = validator;
    }

    /**
     * Updates the code snippet represented by the given dto.
     *
     * @param dto a (partially filled) dto representing the snippet to update.
     * @throws SnippetNotExistsException if the code snippet does not exist.
     * @throws InvalidSnippetException   if the code snippet is invalid.
     */
    public void update(@Nonnull UpdatedSnippetDto dto) throws SnippetNotExistsException, InvalidSnippetException {
        Snippet currentSnippet = readSnippetUseCase.read(dto.id());
        Snippet updatedSnippet = currentSnippet.toBuilder()
                                               .modified(OffsetDateTime.now())
                                               .language(dto.languageOrElse(currentSnippet.getLanguage()))
                                               .title(dto.titleOrElse(currentSnippet.getTitle()))
                                               .code(dto.codeOrElse(currentSnippet.getCode()))
                                               .tags(dto.tagsOrElse(currentSnippet.getTags()))
                                               .description(dto.descriptionOrElse(currentSnippet.getDescription()))
                                               .build();

        validator.validate(updatedSnippet);
        query.update(updatedSnippet);
    }
}
