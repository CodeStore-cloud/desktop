package cloud.codestore.core.usecases.readsnippet;

import cloud.codestore.core.Snippet;
import cloud.codestore.core.SnippetRepository;

import javax.annotation.Nonnull;

/**
 * Use case: read a code snippet by its id.
 */
public class ReadSnippet {
    private final SnippetRepository repository;

    public ReadSnippet(SnippetRepository repository) {
        this.repository = repository;
    }

    public Snippet read(@Nonnull String snippetId) {
        return repository.get(snippetId);
    }
}
