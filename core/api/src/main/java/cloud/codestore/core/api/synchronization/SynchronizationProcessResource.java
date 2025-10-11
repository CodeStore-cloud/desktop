package cloud.codestore.core.api.synchronization;

import cloud.codestore.core.api.UriFactory;
import cloud.codestore.core.usecases.synchronizesnippets.SynchronizationProgress;
import cloud.codestore.core.usecases.synchronizesnippets.SynchronizationState;
import cloud.codestore.jsonapi.error.ErrorObject;
import cloud.codestore.jsonapi.resource.ResourceObject;
import com.fasterxml.jackson.annotation.JsonGetter;

import java.time.OffsetDateTime;

public class SynchronizationProcessResource extends ResourceObject {
    static final String PATH = "/synchronizations";
    private static final String RESOURCE_TYPE = "synchronization";
    private static final String INITIAL_SYNC_ID = "1";

    private final SynchronizationState state;
    private final SynchronizationProgress progress;

    SynchronizationProcessResource(SynchronizationState state, SynchronizationProgress progress) {
        super(RESOURCE_TYPE, INITIAL_SYNC_ID);
        this.state = state;
        this.progress = progress;
        setSelfLink(createLink(getId()));
    }

    @JsonGetter("status")
    public String getStatus() {
        return state.getStatus().name();
    }

    @JsonGetter("progressPercent")
    public int getProgressPercent() {
        return progress.getProgressInPercent();
    }

    @JsonGetter("startTime")
    public OffsetDateTime getStartTime() {
        return state.getStartTime();
    }

    @JsonGetter("endTime")
    public OffsetDateTime getEndTime() {
        return state.getEndTime();
    }

    @JsonGetter("error")
    public ErrorObject getError() {
        ErrorObject errorObject = null;
        if (state.getError() != null) {
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
