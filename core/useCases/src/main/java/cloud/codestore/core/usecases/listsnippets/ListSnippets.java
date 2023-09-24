package cloud.codestore.core.usecases.listsnippets;

import cloud.codestore.core.Snippet;
import cloud.codestore.core.SnippetRepository;
import cloud.codestore.core.UseCase;

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
     * @param filterProperties defines how to filter the code snippets.
     * @return the code snippets.
     */
    @Nonnull
    public List<Snippet> list(FilterProperties filterProperties) {
        return repository.get(filterProperties);
    }
}
