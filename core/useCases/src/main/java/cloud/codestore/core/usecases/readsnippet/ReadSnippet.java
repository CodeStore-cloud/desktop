package cloud.codestore.core.usecases.readsnippet;

import cloud.codestore.core.Snippet;
import cloud.codestore.core.SnippetNotExistsException;
import cloud.codestore.core.SnippetRepository;

import javax.annotation.Nonnull;

/**
 * Use case: read a code snippet by its id.
 */
public class ReadSnippet {
    private final SnippetRepository repository;

    public ReadSnippet(SnippetRepository repository) {
        this.repository = repository;
    }

    /**
     * Reads the code snippet with the given id.
     *
     * @param snippetId the id of a code snippet.
     * @return the code snippet.
     * @throws SnippetNotExistsException if the code snippet does not exist.
     */
    public Snippet read(@Nonnull String snippetId) throws SnippetNotExistsException {
        if (!repository.contains(snippetId)) {
            throw new SnippetNotExistsException();
        }

        return repository.get(snippetId);
    }
}
