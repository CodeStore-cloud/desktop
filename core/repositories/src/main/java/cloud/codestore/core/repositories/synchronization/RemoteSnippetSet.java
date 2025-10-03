package cloud.codestore.core.repositories.synchronization;

import cloud.codestore.core.Snippet;
import cloud.codestore.synchronization.ItemSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Represents the set of code snippets on the remote system. Usually a cloud storage.
 */
@Component
@Qualifier("remote")
class RemoteSnippetSet implements ItemSet<Snippet> {
    private static final Logger LOGGER = LoggerFactory.getLogger(RemoteSnippetSet.class);

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
        LOGGER.debug("Create {} on remote system.", snippetId);
    }

    @Override
    public void delete(String snippetId) throws Exception {
        LOGGER.debug("Delete {} on remote system.", snippetId);
    }

    @Override
    public void updateItem(String snippetId, Snippet snippet) throws Exception {
        LOGGER.debug("Update {} on remote system.", snippetId);
    }
}
