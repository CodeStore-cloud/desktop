package cloud.codestore.client.usecases.listsnippets;

import cloud.codestore.client.SnippetRepository;
import cloud.codestore.client.UseCase;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Use case: read all code snippets.
 */
@UseCase
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
    public List<SnippetListItem> list() {
        return repository.get();
    }
}
