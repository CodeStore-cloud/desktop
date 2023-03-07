package cloud.codestore.core.usecases.createsnippet;

import cloud.codestore.core.Snippet;
import cloud.codestore.core.SnippetBuilder;
import cloud.codestore.core.SnippetRepository;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Use case: create a new snippet.
 */
public class CreateSnippet {
    private final SnippetRepository repository;

    public CreateSnippet(SnippetRepository repository) {
        this.repository = repository;
    }

    /**
     * Creates the code snippet represented by the given dto.
     *
     * @param newSnippet a dto representing the snippet to create.
     * @return the created code snippet.
     */
    public Snippet create(NewSnippetDto newSnippet) {
        Snippet snippet = new SnippetBuilder().id(UUID.randomUUID().toString())
                .created(OffsetDateTime.now())
                .language(newSnippet.language())
                .title(newSnippet.title())
                .code(newSnippet.code())
                .description(newSnippet.description())
                .build();

        repository.put(snippet);
        return snippet;
    }
}
