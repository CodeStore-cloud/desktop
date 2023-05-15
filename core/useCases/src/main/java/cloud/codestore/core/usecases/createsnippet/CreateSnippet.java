package cloud.codestore.core.usecases.createsnippet;

import cloud.codestore.core.Snippet;
import cloud.codestore.core.SnippetBuilder;
import cloud.codestore.core.SnippetRepository;
import cloud.codestore.core.validation.InvalidSnippetException;
import cloud.codestore.core.validation.SnippetValidator;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Use case: create a new code snippet.
 */
public class CreateSnippet {
    private final SnippetRepository repository;
    private final SnippetValidator validator;

    public CreateSnippet(SnippetRepository repository, SnippetValidator validator) {
        this.repository = repository;
        this.validator = validator;
    }

    /**
     * Creates the code snippet represented by the given dto.
     *
     * @param newSnippet a dto representing the snippet to create.
     * @return the created code snippet.
     *
     * @throws InvalidSnippetException if the new code snippet is invalid.
     */
    public Snippet create(NewSnippetDto newSnippet) throws InvalidSnippetException {
        String snippetId = UUID.randomUUID().toString();
        Snippet snippet = new SnippetBuilder().id(snippetId)
                                              .created(OffsetDateTime.now())
                                              .language(newSnippet.language())
                                              .title(newSnippet.title())
                                              .code(newSnippet.code())
                                              .description(newSnippet.description())
                                              .build();

        validator.validate(snippet);
        repository.put(snippet);
        return snippet;
    }
}
