package cloud.codestore.core.usecases.synchronizesnippets;

import cloud.codestore.core.Sync;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Sync
public class SynchronizationProgress {
    private SynchronizationStatus status;
    private OffsetDateTime startTime;
    private OffsetDateTime endTime;
    private int totalSnippets;
    private int processedSnippets;

    @Nullable
    public SynchronizationStatus getStatus() {
        return status;
    }

    public int getProgressInPercent() {
        return (processedSnippets / totalSnippets) * 100;
    }

    @Nullable
    public OffsetDateTime getStartTime() {
        return startTime;
    }

    @Nullable
    public OffsetDateTime getEndTime() {
        return endTime;
    }

    void setStatus(@Nonnull SynchronizationStatus status) {
        this.status = status;
        switch (status) {
            case IN_PROGRESS -> startTime = OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.UTC);
            case COMPLETED, FAILED -> endTime = OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.UTC);
        }
    }

    void setTotalSnippets(int totalSnippets) {
        this.totalSnippets = totalSnippets;
    }

    void setProcessedSnippets(int processedSnippets) {
        this.processedSnippets = processedSnippets;
    }
}
