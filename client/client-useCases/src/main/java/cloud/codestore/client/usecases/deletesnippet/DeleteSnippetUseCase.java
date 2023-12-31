package cloud.codestore.client.usecases.deletesnippet;

import javax.annotation.Nonnull;

/**
 * Use Case: delete a single code snippet.
 */
public interface DeleteSnippetUseCase {
    /**
     * Deletes the code snippet with the given URI.
     * @param snippetUri the URI of a code snippet.
     */
    void deleteSnippet(@Nonnull String snippetUri);
}
