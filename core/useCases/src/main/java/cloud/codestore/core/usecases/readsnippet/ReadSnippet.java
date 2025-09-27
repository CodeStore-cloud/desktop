package cloud.codestore.core.usecases.readsnippet;

import cloud.codestore.core.Injectable;
import cloud.codestore.core.Snippet;
import cloud.codestore.core.SnippetNotExistsException;

import javax.annotation.Nonnull;

/**
 * Use case: read a code snippet by its id.
 */
@Injectable
public class ReadSnippet {
    private final ReadSnippetQuery query;

    public ReadSnippet(ReadSnippetQuery query) {
        this.query = query;
    }

    /**
     * Reads the code snippet with the given id.
     *
     * @param snippetId the id of a code snippet.
     * @return the code snippet.
     * @throws SnippetNotExistsException if the code snippet does not exist.
     */
    @Nonnull
    public Snippet read(@Nonnull String snippetId) throws SnippetNotExistsException {
        return query.read(snippetId);
    }
}
