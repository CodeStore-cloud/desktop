package cloud.codestore.core.usecases.synchronizesnippets;

import cloud.codestore.core.Snippet;
import cloud.codestore.synchronization.ItemSet;

/**
 * A factory for creating local and remote {@link ItemSet}s.
 */
public interface SnippetSetFactory {
    /**
     * @return the {@link ItemSet} that represents the set of snippets located on the local system.
     */
    ItemSet<Snippet> createLocalSnippetSet();

    /**
     * @return the {@link ItemSet} that represents the set of snippets located on the given cloud system.
     */
    ItemSet<Snippet> createRemoteSnippetSet(CloudService cloudService);
}
