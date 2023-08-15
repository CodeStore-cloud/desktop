package cloud.codestore.client.repositories.snippets;

import cloud.codestore.client.Snippet;
import cloud.codestore.client.SnippetRepository;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

/**
 * A {@link SnippetRepository} which saves/loads the code snippets from the local {CodeStore} Core.
 */
class LocalSnippetRepository implements SnippetRepository {
    @Nonnull
    @Override
    public List<Snippet> get() {
        return Collections.emptyList();
    }
}
