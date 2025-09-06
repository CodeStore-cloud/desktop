package cloud.codestore.core.repositories.synchronization;

import cloud.codestore.core.Snippet;
import cloud.codestore.core.repositories.Directory;
import cloud.codestore.core.repositories.File;
import cloud.codestore.core.repositories.Repository;
import cloud.codestore.core.usecases.createsnippet.CreateSnippetQuery;
import cloud.codestore.core.usecases.deletesnippet.DeleteSnippetQuery;
import cloud.codestore.core.usecases.readsnippet.ReadSnippetQuery;
import cloud.codestore.core.usecases.updatesnippet.UpdateSnippetQuery;
import cloud.codestore.synchronization.ItemSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Represents the set of code snippets on the local system.
 */
@Repository
@Qualifier("local")
class LocalSnippetSet implements ItemSet<Snippet> {
    private static final Logger LOGGER = LoggerFactory.getLogger(LocalSnippetSet.class);

    private final Set<String> snippetIds;
    private final ReadSnippetQuery readSnippetQuery;
    private final CreateSnippetQuery createSnippetQuery;
    private final DeleteSnippetQuery deleteSnippetQuery;
    private final UpdateSnippetQuery updateSnippetQuery;

    LocalSnippetSet(
            @Qualifier("snippets") Directory snippetsDirectory,
            ReadSnippetQuery readSnippetQuery,
            CreateSnippetQuery createSnippetQuery,
            DeleteSnippetQuery deleteSnippetQuery,
            UpdateSnippetQuery updateSnippetQuery
    ) {
        snippetIds = snippetsDirectory.getFiles()
                                      .stream()
                                      .map(File::getName)
                                      .collect(Collectors.toSet());

        this.readSnippetQuery = readSnippetQuery;
        this.createSnippetQuery = createSnippetQuery;
        this.deleteSnippetQuery = deleteSnippetQuery;
        this.updateSnippetQuery = updateSnippetQuery;
    }

    @Override
    public Set<String> getItemIds() {
        return snippetIds;
    }

    @Override
    public boolean contains(String snippetId) {
        return snippetIds.contains(snippetId);
    }

    @Override
    public String getEtag(String snippetId) throws Exception {
        Snippet snippet = getItem(snippetId);
        OffsetDateTime timestamp = snippet.getOptionalModified().orElseGet(snippet::getCreated);
        return timestamp.toString();
    }

    @Override
    public Snippet getItem(String snippetId) throws Exception {
        return readSnippetQuery.read(snippetId);
    }

    @Override
    public void addItem(String snippetId, Snippet snippet) {
        LOGGER.debug("Create {} ({}) on local system.", snippetId, snippet.getTitle());
        createSnippetQuery.create(snippet);
    }

    @Override
    public void delete(String snippetId) throws Exception {
        Snippet snippet = getItem(snippetId);
        LOGGER.debug("Delete {} ({}) on local system.", snippetId, snippet.getTitle());
        deleteSnippetQuery.delete(snippetId);
    }

    @Override
    public void updateItem(String snippetId, Snippet snippet) throws Exception {
        LOGGER.debug("Update {} ({}) on local system.", snippetId, snippet.getTitle());
        updateSnippetQuery.update(snippet);
    }
}
