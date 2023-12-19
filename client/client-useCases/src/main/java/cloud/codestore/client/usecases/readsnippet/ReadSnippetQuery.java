package cloud.codestore.client.usecases.readsnippet;

import cloud.codestore.client.Snippet;

import javax.annotation.Nonnull;

/**
 * A query for reading a single code snippet.
 */
public interface ReadSnippetQuery {
    /**
     * Reads the code snippet with the given URI.
     * @param snippetUri the URI of the snippet to read.
     * @return the corresponding code snippet.
     */
    @Nonnull
    Snippet readSnippet(String snippetUri);
}
