package cloud.codestore.core.usecases.listsnippets;

import cloud.codestore.core.Snippet;
import cloud.codestore.core.UseCase;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Use case: read all code snippets.
 */
@UseCase
public class ListSnippets {
    private final ReadSnippetsQuery query;

    public ListSnippets(ReadSnippetsQuery query) {
        this.query = query;
    }

    @Nonnull
    public List<Snippet> list(FilterProperties filterProperties, SortProperties sortProperties) {
        return query.readSnippets(filterProperties, sortProperties);
    }
}
