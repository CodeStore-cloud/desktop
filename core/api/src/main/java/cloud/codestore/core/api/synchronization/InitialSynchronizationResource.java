package cloud.codestore.core.api.synchronization;

import cloud.codestore.core.api.UriFactory;
import cloud.codestore.core.usecases.synchronizesnippets.InitialSynchronization;
import cloud.codestore.core.usecases.synchronizesnippets.InitialSynchronizationProgress;
import cloud.codestore.jsonapi.error.ErrorObject;
import cloud.codestore.jsonapi.resource.ResourceObject;
import com.fasterxml.jackson.annotation.JsonGetter;

import java.time.OffsetDateTime;

public class InitialSynchronizationResource extends ResourceObject {
    static final String PATH = "/synchronizations";
    private static final String RESOURCE_TYPE = "synchronization";
    private static final String INITIAL_SYNC_ID = "1";

    private final InitialSynchronizationProgress progress;

    InitialSynchronizationResource(InitialSynchronization synchronization) {
        super(RESOURCE_TYPE, INITIAL_SYNC_ID);
        progress = synchronization.getProgress();
        setSelfLink(createLink(getId()));
    }

    @JsonGetter("status")
    public String getStatus() {
        return progress.getStatus().name();
    }

    @JsonGetter("progressPercent")
    public int getProgressPercent() {
        return progress.getProgressInPercent();
    }

    @JsonGetter("startTime")
    public OffsetDateTime getStartTime() {
        return progress.getStartTime();
    }

    @JsonGetter("endTime")
    public OffsetDateTime getEndTime() {
        return progress.getEndTime();
    }

    @JsonGetter("error")
    public ErrorObject getError() {
        ErrorObject errorObject = null;
        if (progress.getError() != null) {
            errorObject = new ErrorObject();
            //TODO fill error object
        }

        return errorObject;
    }

    public static String createLink() {
        return createLink(INITIAL_SYNC_ID);
    }

    static String createLink(String id) {
        return UriFactory.createUri(PATH + "/" + id);
    }
}
