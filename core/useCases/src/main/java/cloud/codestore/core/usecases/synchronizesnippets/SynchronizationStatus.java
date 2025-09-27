package cloud.codestore.core.usecases.synchronizesnippets;

public enum SynchronizationStatus {
    PENDING, IN_PROGRESS, COMPLETED, FAILED;

    /**
     * @return whether this status indicates that the synchronization is done ({@code COMPLETED} or {@code FAILED}).
     */
    public boolean isDone() {
        return this == COMPLETED || this == FAILED;
    }
}
