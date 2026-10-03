package cloud.codestore.core.api.synchronization;

import cloud.codestore.core.api.ErrorResponseBuilder;
import cloud.codestore.core.usecases.synchronizesnippets.SnippetSynchronizationState;
import cloud.codestore.jsonapi.error.ErrorObject;
import cloud.codestore.jsonapi.resource.ResourceObject;
import com.fasterxml.jackson.annotation.JsonGetter;

import java.time.OffsetDateTime;

class SnippetSynchronizationResource extends ResourceObject {
    private static final String RESOURCE_TYPE = "snippet-synchronization";

    private final SnippetSynchronizationState synchronization;

    SnippetSynchronizationResource(String snippetId, SnippetSynchronizationState synchronization) {
        super(RESOURCE_TYPE, snippetId);
        this.synchronization = synchronization;
        setSelfLink(createLink(snippetId));
    }

    @JsonGetter("status")
    public String getStatus() {
        return synchronization.getStatus().name();
    }

    @JsonGetter("startTime")
    public OffsetDateTime getStartTime() {
        return synchronization.getStartTime();
    }

    @JsonGetter("endTime")
    public OffsetDateTime getEndTime() {
        return synchronization.getEndTime();
    }

    @JsonGetter("error")
    public ErrorObject getError() {
        Throwable error = synchronization.getError();
        return error == null ? null : new ErrorResponseBuilder().createError("SYNCHRONIZATION_FAILED", "synchronization.error.title", error.getMessage());
    }

    private static String createLink(String snippetId) {
        return SynchronizationProcessResource.createLink(snippetId);
    }
}
