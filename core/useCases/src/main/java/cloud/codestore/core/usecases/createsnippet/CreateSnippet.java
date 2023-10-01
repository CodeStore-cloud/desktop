package cloud.codestore.core.usecases.createsnippet;

import cloud.codestore.core.Snippet;
import cloud.codestore.core.UseCase;
import cloud.codestore.core.validation.InvalidSnippetException;
import cloud.codestore.core.validation.SnippetValidator;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Use case: create a new code snippet.
 */
@UseCase
public class CreateSnippet {
    private final CreateSnippetQuery query;
    private final SnippetValidator validator;

    public CreateSnippet(CreateSnippetQuery query, SnippetValidator validator) {
        this.query = query;
        this.validator = validator;
    }

    /**
     * Creates the code snippet represented by the given dto.
     *
     * @param newSnippet a dto representing the snippet to create.
     * @return the created code snippet.
     * @throws InvalidSnippetException if the new code snippet is invalid.
     */
    public Snippet create(NewSnippetDto newSnippet) throws InvalidSnippetException {
        String snippetId = UUID.randomUUID().toString();
        Snippet snippet = Snippet.builder()
                                 .id(snippetId)
                                 .created(OffsetDateTime.now())
                                 .language(newSnippet.language())
                                 .title(newSnippet.title())
                                 .code(newSnippet.code())
                                 .tags(newSnippet.tags())
                                 .description(newSnippet.description())
                                 .build();

        validator.validate(snippet);
        query.create(snippet);
        return snippet;
    }
}
