package cloud.codestore.client.usecases.updatesnippet;

import cloud.codestore.client.Snippet;

import javax.annotation.Nonnull;

/**
 * Use Case: update an existing code snippet;
 */
public interface UpdateSnippetUseCase {
    /**
     * Updates a code snippet in the core with the values of the given DTO.
     *
     * @param snippetDto a DTO providing the updated content of the code snippet.
     * @return an instance of the updated code snippet.
     */
    Snippet update(@Nonnull UpdatedSnippetDto snippetDto);
}
