package cloud.codestore.core.repositories.synchronization;

import cloud.codestore.core.Snippet;
import cloud.codestore.core.repositories.Directory;
import cloud.codestore.core.repositories.File;
import cloud.codestore.core.repositories.serialization.SnippetFileHelper;
import cloud.codestore.core.repositories.serialization.SnippetReader;
import cloud.codestore.core.repositories.serialization.SnippetWriter;
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
    private final SnippetReader snippetReader;
    private final SnippetWriter snippetWriter;
    private Set<String> snippetIds;

    LocalSnippetSet(
            @Qualifier("snippets") Directory snippetsDirectory,
            SnippetReader snippetReader,
            SnippetWriter snippetWriter
    ) {
        this.snippetsDirectory = snippetsDirectory;
        this.snippetReader = snippetReader;
        this.snippetWriter = snippetWriter;
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
    public String getEtag(String snippetId) {
        Snippet snippet = getItem(snippetId);
        OffsetDateTime timestamp = snippet.getOptionalModified().orElseGet(snippet::getCreated);
        return timestamp.toString();
    }

    @Override
    public Snippet getItem(String snippetId) {
        return snippetReader.read(file(snippetId));
    }

    @Override
    public void addItem(String snippetId, Snippet snippet) {
        LOGGER.debug("Create {} on local system.", snippetId);
        snippetWriter.write(snippet, file(snippetId));
    }

    @Override
    public void delete(String snippetId) throws Exception {
        LOGGER.debug("Delete {} on local system.", snippetId);
        file(snippetId).delete();
    }

    @Override
    public void updateItem(String snippetId, Snippet snippet) {
        LOGGER.debug("Update {} on local system.", snippetId);
        snippetWriter.write(snippet, file(snippetId));
    }

    private File file(String snippetId) {
        return snippetsDirectory.getFile(SnippetFileHelper.getFileName(snippetId));
    }
}
