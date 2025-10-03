package cloud.codestore.core.usecases.synchronizesnippets;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * Base class for objects that contain progress and status information of the synchronization.
 */
class SynchronizationResult {
    private SyncState syncState;
    private SynchronizationStatus status;
    private OffsetDateTime startTime;
    private OffsetDateTime endTime;
    private Throwable error;

    SynchronizationResult() {
        syncState = new PendingState();
    }

    void start() {
        syncState.start();
    }

    void complete() {
        syncState.complete();
    }

    void fail(@Nonnull Throwable error) {
        syncState.fail(error);
    }

    @Nonnull
    public SynchronizationStatus getStatus() {
        return status;
    }

    @Nullable
    public OffsetDateTime getStartTime() {
        return startTime;
    }

    @Nullable
    public OffsetDateTime getEndTime() {
        return endTime;
    }

    @Nullable
    public Throwable getError() {
        return error;
    }

    int getDuration() {
        if (startTime != null && endTime != null) {
            return (int) Duration.between(startTime, endTime).toMillis();
        } else {
            return 0;
        }
    }

    private interface SyncState {
        default void start() {
            throw getIllegalStateException();
        }

        default void complete() {
            throw getIllegalStateException();
        }

        default void fail(Throwable error) {
            throw getIllegalStateException();
        }

        IllegalStateException getIllegalStateException();
    }

    private class PendingState implements SyncState {
        PendingState() {
            status = SynchronizationStatus.PENDING;
        }

        @Override
        public void start() {
            syncState = new InProgressState();
        }

        @Override
        public IllegalStateException getIllegalStateException() {
            return new IllegalStateException("Synchronization not started.");
        }
    }

    private class InProgressState implements SyncState {
        InProgressState() {
            status = SynchronizationStatus.IN_PROGRESS;
            startTime = OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.UTC);
        }

        @Override
        public void complete() {
            syncState = new FinishedState(SynchronizationStatus.COMPLETED);
        }

        @Override
        public void fail(Throwable throwable) {
            error = throwable;
            syncState = new FinishedState(SynchronizationStatus.FAILED);
        }

        @Override
        public IllegalStateException getIllegalStateException() {
            return new IllegalStateException("Synchronization already started.");
        }
    }

    private class FinishedState implements SyncState {
        FinishedState(SynchronizationStatus finalStatus) {
            status = finalStatus;
            endTime = OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.UTC);
        }

        @Override
        public IllegalStateException getIllegalStateException() {
            return new IllegalStateException("Synchronization already finished.");
        }
    }
}
