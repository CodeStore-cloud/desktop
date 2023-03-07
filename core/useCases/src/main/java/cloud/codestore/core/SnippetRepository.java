package cloud.codestore.core;

/**
 * A repository for storing and reading code snippets.
 */
public interface SnippetRepository {
    /**
     * Checks whether this repository contains the code snippet with the given id.
     *
     * @param snippetId the id of a code snippet.
     * @return whether this repository contains the code snippet.
     */
    boolean contains(String snippetId);

    /**
     * Reads the code snippet with the given id.
     *
     * @param snippetId the id of a code snippet.
     * @return the code snippet with the given id.
     */
    Snippet get(String snippetId);

    /**
     * Puts the given code snippet into this repository.
     * If the code snippet already exists, it will be overridden.
     *
     * @param snippet a code snippet.
     */
    void put(Snippet snippet);
}
