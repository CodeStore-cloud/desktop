package cloud.codestore.core;

/**
 * A repository for storing and reading code snippets.
 */
public interface SnippetRepository {
    /**
     * Reads the code snippet with the given id.
     *
     * @param snippetId the id of a code snippet.
     * @return the code snippet with the given id.
     */
    Snippet get(String snippetId);
}
