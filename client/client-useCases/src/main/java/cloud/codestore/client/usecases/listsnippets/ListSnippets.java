package cloud.codestore.client.usecases.listsnippets;

import cloud.codestore.client.Snippet;
import cloud.codestore.client.SnippetRepository;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Use case: read all code snippets.
 */
public class ListSnippets {
    private final SnippetRepository repository;

    public ListSnippets(SnippetRepository repository) {
        this.repository = repository;
    }

    /**
     * Reads all code snippets.
     *
     * @return the code snippets.
     */
    @Nonnull
    public List<Snippet> list() {
        return repository.get();
    }
}
