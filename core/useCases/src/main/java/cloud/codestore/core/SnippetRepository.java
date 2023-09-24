package cloud.codestore.core;

import cloud.codestore.core.usecases.listsnippets.FilterProperties;

import java.util.List;

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
     * Reads all code snippets.
     * @param filterProperties defines how to filter the code snippets.
     * @return all code snippets.
     */
    List<Snippet> get(FilterProperties filterProperties);

    /**
     * Puts the given code snippet into this repository.
     * If the code snippet already exists, it will be overridden.
     *
     * @param snippet a code snippet.
     */
    void put(Snippet snippet);

    /**
     * Deletes the code snippet with the given id from this repository.
     * @param snippetId the id of a code snippet.
     */
    void delete(String snippetId);
}
