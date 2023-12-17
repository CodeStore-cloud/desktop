package cloud.codestore.client;

import cloud.codestore.client.usecases.listsnippets.SnippetPage;

import javax.annotation.Nonnull;

/**
 * A repository for storing and reading code snippets.
 */
public interface SnippetRepository {
    /**
     * Reads all code snippets.
     * @return all code snippets.
     */
    @Nonnull
    SnippetPage get();

    /**
     * Reads the code snippet with the given URI.
     * @param snippetUri the URI of the snippet to read.
     * @return the corresponding code snippet.
     */
    @Nonnull
    Snippet get(String snippetUri);
}
