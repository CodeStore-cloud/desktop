package cloud.codestore.core.usecases.synchronizesnippets;

import cloud.codestore.core.Injectable;

@Injectable
public class InitialSynchronizationProgress extends SynchronizationResult {
    private int totalSnippets;
    private int processedSnippets;

    void setTotalSnippets(int totalSnippets) {
        this.totalSnippets = totalSnippets;
    }

    void setProcessedSnippets(int processedSnippets) {
        this.processedSnippets = processedSnippets;
    }

    public int getProgressInPercent() {
        if (totalSnippets == 0) {
            return 0;
        }
        return (int) ((processedSnippets / (double) totalSnippets) * 100);
    }
}
