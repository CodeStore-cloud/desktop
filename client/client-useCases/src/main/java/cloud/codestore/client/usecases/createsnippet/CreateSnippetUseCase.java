package cloud.codestore.client.usecases.createsnippet;

import cloud.codestore.client.Snippet;

import javax.annotation.Nonnull;

/**
 * Use Case: create a new code snippet;
 */
public interface CreateSnippetUseCase {
    /**
     * Creates a new code snippet in the core with the values of the given DTO.
     *
     * @param snippetDto a DTO providing the content of the code snippet.
     * @return an instance of the created code snippet.
     */
    Snippet create(@Nonnull NewSnippetDto snippetDto);
}
