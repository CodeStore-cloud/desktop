package cloud.codestore.core.repositories.synchronization;

import cloud.codestore.core.Snippet;
import cloud.codestore.core.repositories.Directory;
import cloud.codestore.core.repositories.serialization.SnippetFileHelper;
import cloud.codestore.core.usecases.createsnippet.CreateSnippetQuery;
import cloud.codestore.core.usecases.deletesnippet.DeleteSnippetQuery;
import cloud.codestore.core.usecases.readsnippet.ReadSnippetQuery;
import cloud.codestore.core.usecases.updatesnippet.UpdateSnippetQuery;
import cloud.codestore.synchronization.ItemSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Represents the set of code snippets on the local system.
 */
@Component
@Qualifier("local")
class LocalSnippetSet implements ItemSet<Snippet> {
    private static final Logger LOGGER = LoggerFactory.getLogger(LocalSnippetSet.class);

    private final Directory snippetsDirectory;
    private final ReadSnippetQuery readSnippetQuery;
    private final CreateSnippetQuery createSnippetQuery;
    private final DeleteSnippetQuery deleteSnippetQuery;
    private final UpdateSnippetQuery updateSnippetQuery;
    private Set<String> snippetIds;

    LocalSnippetSet(
            @Qualifier("snippets") Directory snippetsDirectory,
            ReadSnippetQuery readSnippetQuery,
            CreateSnippetQuery createSnippetQuery,
            DeleteSnippetQuery deleteSnippetQuery,
            UpdateSnippetQuery updateSnippetQuery
    ) {
        this.snippetsDirectory = snippetsDirectory;
        this.readSnippetQuery = readSnippetQuery;
        this.createSnippetQuery = createSnippetQuery;
        this.deleteSnippetQuery = deleteSnippetQuery;
        this.updateSnippetQuery = updateSnippetQuery;
    }

    @Override
    public Set<String> getItemIds() {
        snippetIds = snippetsDirectory.getFiles()
                                      .stream()
                                      .map(SnippetFileHelper::getSnippetId)
                                      .collect(Collectors.toSet());

        return snippetIds;
    }

    @Override
    public boolean contains(String snippetId) {
        return snippetIds.contains(snippetId);
    }

    @Override
    public String getEtag(String snippetId) throws Exception {
        Snippet snippet = readSnippetQuery.read(snippetId);
        OffsetDateTime timestamp = snippet.getOptionalModified().orElseGet(snippet::getCreated);
        return timestamp.toString();
    }

    @Override
    public Snippet getItem(String snippetId) throws Exception {
        return readSnippetQuery.read(snippetId);
    }

    @Override
    public void addItem(String snippetId, Snippet snippet) {
        LOGGER.debug("Create {} on local system.", snippetId);
        createSnippetQuery.create(snippet);
    }

    @Override
    public void delete(String snippetId) throws Exception {
        LOGGER.debug("Delete {} on local system.", snippetId);
        deleteSnippetQuery.delete(snippetId);
    }

    @Override
    public void updateItem(String snippetId, Snippet snippet) throws Exception {
        LOGGER.debug("Update {} on local system.", snippetId);
        updateSnippetQuery.update(snippet);
    }
}
