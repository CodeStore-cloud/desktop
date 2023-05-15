package cloud.codestore.core.usecases.deletesnippet;

import cloud.codestore.core.SnippetRepository;

import javax.annotation.Nonnull;

/**
 * Use case: delete a code snippet.
 */
public class DeleteSnippet {
    private final SnippetRepository repository;

    public DeleteSnippet(SnippetRepository repository) {
        this.repository = repository;
    }

    public void delete(@Nonnull String snippetId) {

    }
}
