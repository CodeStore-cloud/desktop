package cloud.codestore.core.usecases.synchronizesnippets;

import cloud.codestore.core.Injectable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Injectable
public class SynchronizationProgress {
    private SynchronizationStatus status = SynchronizationStatus.PENDING;
    private OffsetDateTime startTime;
    private OffsetDateTime endTime;
    private int totalSnippets;
    private int processedSnippets;

    @Nonnull
    public SynchronizationStatus getStatus() {
        return status;
    }

    public int getProgressInPercent() {
        if (totalSnippets == 0) {
            return 0;
        }
        return (int) ((processedSnippets / (double) totalSnippets) * 100);
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
        if (this.status.isDone()) {
            throw new IllegalStateException("Synchronization already finished.");
        }

        this.status = status;
        switch (status) {
            case IN_PROGRESS -> startTime = OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.UTC);
            case COMPLETED, FAILED -> endTime = OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.UTC);
        }
    }

    int getDuration() {
        if (startTime != null && endTime != null) {
            return (int) Duration.between(startTime, endTime).toMillis();
        } else {
            return 0;
        }
    }

    void setTotalSnippets(int totalSnippets) {
        this.totalSnippets = totalSnippets;
    }

    void setProcessedSnippets(int processedSnippets) {
        this.processedSnippets = processedSnippets;
    }
}
