package cloud.codestore.client.usecases.readsnippet;

import cloud.codestore.client.Snippet;
import cloud.codestore.client.UseCase;

import javax.annotation.Nonnull;

@UseCase
public class ReadSnippet {
    private final ReadSnippetQuery snippetQuery;

    public ReadSnippet(ReadSnippetQuery query) {
        this.snippetQuery = query;
    }

    /**
     * Reads the code snippet with the given URI.
     *
     * @param snippetUri the URI of the snippet to read.
     * @return the corresponding code snippet.
     */
    public Snippet readSnippet(@Nonnull String snippetUri) {
        return snippetQuery.readSnippet(snippetUri);
    }
}
