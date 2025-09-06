package cloud.codestore.core.repositories.synchronization;

import cloud.codestore.core.Snippet;
import cloud.codestore.core.repositories.Repository;
import cloud.codestore.core.usecases.synchronizesnippets.SynchronizationReport;
import cloud.codestore.synchronization.ItemSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.Set;

/**
 * Represents the set of code snippets on the remote system. Usually a cloud storage.
 */
@Repository
@Qualifier("remote")
class RemoteSnippetSet implements ItemSet<Snippet> {
    private static final Logger LOGGER = LoggerFactory.getLogger(RemoteSnippetSet.class);
    private final SynchronizationReport syncReport;

    RemoteSnippetSet(SynchronizationReport syncReport) {
        this.syncReport = syncReport;
    }

    @Override
    public Set<String> getItemIds() {
        return Set.of();
    }

    @Override
    public boolean contains(String snippetId) {
        return false;
    }

    @Override
    public String getEtag(String snippetId) throws Exception {
        return "";
    }

    @Override
    public Snippet getItem(String snippetId) throws Exception {
        return null;
    }

    @Override
    public void addItem(String snippetId, Snippet snippet) throws Exception {
        LOGGER.debug("Create {} ({}) on remote system.", snippetId, snippet.getTitle());
        syncReport.snippetCreatedRemotely(snippet);
    }

    @Override
    public void delete(String snippetId) throws Exception {
        Snippet snippet = getItem(snippetId);
        LOGGER.debug("Delete {} ({}) on remote system.", snippetId, snippet.getTitle());
        syncReport.snippetDeletedRemotely(snippet);
    }

    @Override
    public void updateItem(String snippetId, Snippet snippet) throws Exception {
        LOGGER.debug("Update {} ({}) on remote system.", snippetId, snippet.getTitle());
        syncReport.snippetUpdatedRemotely(snippet);
    }
}
