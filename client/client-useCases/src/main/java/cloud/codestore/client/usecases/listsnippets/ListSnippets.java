package cloud.codestore.client.usecases.listsnippets;

import cloud.codestore.client.SnippetRepository;
import cloud.codestore.client.UseCase;

import javax.annotation.Nonnull;

/**
 * Use case: read all code snippets.
 */
@UseCase
public class ListSnippets {
    private final SnippetRepository repository;

    public ListSnippets(SnippetRepository repository) {
        this.repository = repository;
    }

    @Nonnull
    public SnippetPage list() {
        return repository.get();
    }

    @Nonnull
    public SnippetPage list(@Nonnull String url) {
        return repository.get();
    }
}
