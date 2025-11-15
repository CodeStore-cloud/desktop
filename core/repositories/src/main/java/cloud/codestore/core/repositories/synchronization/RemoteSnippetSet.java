package cloud.codestore.core.repositories.synchronization;

import cloud.codestore.core.Snippet;
import cloud.codestore.core.repositories.serialization.SnippetFileHelper;
import cloud.codestore.core.repositories.serialization.SnippetReader;
import cloud.codestore.core.repositories.serialization.SnippetWriter;
import cloud.codestore.synchronization.ItemSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

class RemoteSnippetSet implements ItemSet<Snippet> {
    private static final Logger LOGGER = LoggerFactory.getLogger(RemoteSnippetSet.class);

    private final SnippetReader snippetReader;
    private final SnippetWriter snippetWriter;
    private RemoteDirectory snippetsDirectory;
    private Map<String, RemoteFile> remoteFiles = new HashMap<>();

    RemoteSnippetSet(
            SnippetReader snippetReader,
            SnippetWriter snippetWriter,
            RemoteDirectory codeStoreDirectory
    ) {
        this.snippetReader = snippetReader;
        this.snippetWriter = snippetWriter;

        if (!codeStoreDirectory.exists()) {
            codeStoreDirectory.create();
        }

        snippetsDirectory = codeStoreDirectory.getSubDirectory("snippets");
        if (!snippetsDirectory.exists()) {
            snippetsDirectory.create();
        }
    }

    @Override
    public Set<String> getItemIds() {
        if (remoteFiles.isEmpty()) {
            for (RemoteFile file : snippetsDirectory.getFiles()) {
                String snippetId = SnippetFileHelper.getSnippetId(file.getName());
                remoteFiles.put(snippetId, file);
            }
        }

        return remoteFiles.keySet();
    }

    @Override
    public boolean contains(String snippetId) {
        return remoteFiles.containsKey(snippetId);
    }

    @Override
    public String getEtag(String snippetId) {
        RemoteFile file = remoteFiles.get(snippetId);
        OffsetDateTime modified = file.getModified();
        if (modified == null) {
            throw new IllegalStateException(String.format("Remote file %s has no modified timestamp", file.getPath()));
        }

        return modified.truncatedTo(ChronoUnit.SECONDS)
                       .withOffsetSameInstant(ZoneOffset.UTC)
                       .toString();
    }

    @Override
    public Snippet getItem(String snippetId) {
        RemoteFile file = remoteFiles.get(snippetId);
        return snippetReader.read(file);
    }

    @Override
    public void addItem(String snippetId, Snippet snippet) {
        LOGGER.debug("Create {} on remote system.", snippetId);
        RemoteFile file = snippetsDirectory.newFile(SnippetFileHelper.getFileName(snippetId));
        file.setCreated(snippet.getCreated());
        file.setModified(snippet.getOptionalModified().orElse(snippet.getCreated()));
        snippetWriter.write(snippet, file);
    }

    @Override
    public void updateItem(String snippetId, Snippet snippet) {
        LOGGER.debug("Update {} on remote system.", snippetId);
        RemoteFile file = remoteFiles.get(snippetId);
        file.setModified(snippet.getOptionalModified().orElse(snippet.getCreated()));
        snippetWriter.write(snippet, file);
    }

    @Override
    public void delete(String snippetId) {
        LOGGER.debug("Delete {} on remote system.", snippetId);
        remoteFiles.get(snippetId).delete();
    }
}
