package cloud.codestore.core.usecases.synchronizesnippets;

import cloud.codestore.core.Snippet;
import cloud.codestore.core.Sync;

import java.util.*;

/**
 * Provides information about the code snippets that were created, updated or deleted during the synchronization
 * on both, the local and remote system.
 */
@Sync
public class SynchronizationReport {

    private final List<Snippet> localNewSnippets = Collections.synchronizedList(new LinkedList<>());
    private final List<Snippet> localUpdatedSnippets = Collections.synchronizedList(new LinkedList<>());
    private final List<Snippet> localDeletedSnippets = Collections.synchronizedList(new LinkedList<>());
    private final List<Snippet> remoteNewSnippets = Collections.synchronizedList(new LinkedList<>());
    private final List<Snippet> remoteUpdatedSnippets = Collections.synchronizedList(new LinkedList<>());
    private final List<Snippet> remoteDeletedSnippets = Collections.synchronizedList(new LinkedList<>());
    private final Map<String, Throwable> errors = new HashMap<>();
    private int totalSnippetCount;

    public void snippetCreatedLocally(Snippet snippet) {
        localNewSnippets.add(snippet);
    }

    public void snippetUpdatedLocally(Snippet snippet) {
        localUpdatedSnippets.add(snippet);
    }

    public void snippetDeletedLocally(Snippet snippet) {
        localDeletedSnippets.add(snippet);
    }

    public void snippetCreatedRemotely(Snippet snippet) {
        remoteNewSnippets.add(snippet);
    }

    public void snippetUpdatedRemotely(Snippet snippet) {
        remoteUpdatedSnippets.add(snippet);
    }

    public void snippetDeletedRemotely(Snippet snippet) {
        remoteDeletedSnippets.add(snippet);
    }

    public List<Snippet> getLocallyCreatedSnippets() {
        return Collections.unmodifiableList(localNewSnippets);
    }

    public List<Snippet> getLocallyUpdatedSnippets() {
        return Collections.unmodifiableList(localUpdatedSnippets);
    }

    public List<Snippet> getLocallyDeletedSnippets() {
        return Collections.unmodifiableList(localDeletedSnippets);
    }

    public List<Snippet> getRemotelyNewSnippets() {
        return Collections.unmodifiableList(remoteNewSnippets);
    }

    public List<Snippet> getRemotelyUpdatedSnippets() {
        return Collections.unmodifiableList(remoteUpdatedSnippets);
    }

    public List<Snippet> getRemotelyDeletedSnippets() {
        return Collections.unmodifiableList(remoteDeletedSnippets);
    }

    void synchronizationFailed(String snippetId, Throwable error) {
        errors.put(snippetId, error);
    }

    public int getTotalSnippetCount() {
        return totalSnippetCount;
    }

    void setTotalSnippetCount(int totalSnippetCount) {
        this.totalSnippetCount = totalSnippetCount;
    }

    public int getSuccessCount() {
        return totalSnippetCount - getErrorCount();
    }

    public int getErrorCount() {
        return errors.size();
    }

    public Map<String, Throwable> getErrors() {
        return Collections.unmodifiableMap(errors);
    }
}
