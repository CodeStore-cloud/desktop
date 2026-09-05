package cloud.codestore.core.usecases.synchronizesnippets;

import cloud.codestore.core.Snippet;
import cloud.codestore.synchronization.ItemSet;

import javax.annotation.Nonnull;

/**
 * A factory for creating local and remote {@link ItemSet}s.
 */
public interface SnippetSetFactory {
    /**
     * @return the {@link ItemSet} that represents the set of snippets located on the local system.
     */
    @Nonnull
    ItemSet<Snippet> createLocalSnippetSet();

    /**
     * @return the {@link ItemSet} that represents the set of snippets located on the given cloud system.
     */
    @Nonnull
    ItemSet<Snippet> createRemoteSnippetSet(@Nonnull CloudService cloudService);
}
