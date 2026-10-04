package cloud.codestore.core.usecases.synchronizesnippets;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Objects;

/**
 * Represents the state of the synchronization.
 * It provides access to the status of the synchronization as well as the duration and error information.
 */
public class SynchronizationState {
    private SynchronizationStatus status;
    private OffsetDateTime startTime;
    private OffsetDateTime endTime;
    private Throwable error;
    private InternalState state;

    SynchronizationState() {
        state = new PendingState();
    }

    void start() {
        state.start();
    }

    void complete() {
        state.complete();
    }

    void fail(@Nonnull Throwable error) {
        state.fail(error);
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

    private interface InternalState {
        default void start() {}

        default void complete() {}

        default void fail(Throwable throwable) {}
    }

    private class PendingState implements InternalState {
        PendingState() {
            status = SynchronizationStatus.PENDING;
        }

        @Override
        public void start() {
            state = new InProgressState();
        }

        @Override
        public void fail(Throwable throwable) {
            error = throwable;
            state = new FinishedState(SynchronizationStatus.FAILED);
        }
    }

    private class InProgressState implements InternalState {
        InProgressState() {
            status = SynchronizationStatus.IN_PROGRESS;
            startTime = OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.UTC);
        }

        @Override
        public void complete() {
            state = new FinishedState(SynchronizationStatus.COMPLETED);
        }

        @Override
        public void fail(Throwable throwable) {
            error = throwable;
            state = new FinishedState(SynchronizationStatus.FAILED);
        }
    }

    private class FinishedState implements InternalState {
        FinishedState(SynchronizationStatus finalStatus) {
            status = finalStatus;
            endTime = OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.UTC);
        }
    }

    @Override
    protected Object clone() {
        var clone = new SynchronizationState();
        clone.status = status;
        clone.startTime = startTime;
        clone.endTime = endTime;
        clone.error = error;
        return error;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass())
            return false;

        SynchronizationState that = (SynchronizationState) o;
        return status == that.status &&
               Objects.equals(startTime, that.startTime) &&
               Objects.equals(endTime, that.endTime) &&
               Objects.equals(error, that.error);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(status) +
               Objects.hashCode(startTime) +
               Objects.hashCode(endTime) +
               Objects.hashCode(error);
    }
}
