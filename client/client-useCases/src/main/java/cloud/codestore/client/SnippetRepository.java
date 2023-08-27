package cloud.codestore.client;

import cloud.codestore.client.usecases.listsnippets.SnippetListItem;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * A repository for storing and reading code snippets.
 */
public interface SnippetRepository {
    /**
     * Reads all code snippets.
     * @return all code snippets.
     */
    @Nonnull
    List<SnippetListItem> get();
}
