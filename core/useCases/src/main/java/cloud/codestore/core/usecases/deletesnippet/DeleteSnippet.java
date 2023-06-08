package cloud.codestore.core.usecases.deletesnippet;

import cloud.codestore.core.SnippetNotExistsException;
import cloud.codestore.core.SnippetRepository;
import cloud.codestore.core.UseCase;

import javax.annotation.Nonnull;

/**
 * Use case: delete a code snippet.
 */
@UseCase
public class DeleteSnippet {
    private final SnippetRepository repository;

    public DeleteSnippet(SnippetRepository repository) {
        this.repository = repository;
    }

    /**
     * Deletes the code snippet with the given id.
     * @param snippetId the id of the code snippet to delete.
     * @throws SnippetNotExistsException if the code snippet does not exist.
     */
    public void delete(@Nonnull String snippetId) throws SnippetNotExistsException {
        if (!repository.contains(snippetId)) {
            throw new SnippetNotExistsException();
        }

        repository.delete(snippetId);
    }
}
