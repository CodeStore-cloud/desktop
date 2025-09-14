package cloud.codestore.core.api.synchronization;

import cloud.codestore.core.api.UriFactory;
import cloud.codestore.core.usecases.synchronizesnippets.Synchronization;
import cloud.codestore.core.usecases.synchronizesnippets.SynchronizationProgress;
import cloud.codestore.core.usecases.synchronizesnippets.SynchronizationStatus;
import cloud.codestore.jsonapi.relationship.Relationship;
import cloud.codestore.jsonapi.resource.ResourceObject;
import com.fasterxml.jackson.annotation.JsonGetter;

import java.time.OffsetDateTime;

class SynchronizationResource extends ResourceObject {
    static final String PATH = "/synchronizations";
    private static final String RESOURCE_TYPE = "synchronization";

    private final SynchronizationProgress progress;
    private Relationship report;

    public SynchronizationResource(int syncId, Synchronization synchronization) {
        super(RESOURCE_TYPE, String.valueOf(syncId));
        progress = synchronization.getProgress();
        setSelfLink(createLink(syncId));

        SynchronizationStatus status = progress.getStatus();
        if (status == SynchronizationStatus.COMPLETED || status == SynchronizationStatus.FAILED) {
            report = asRelationship(new SynchronizationReportResource(syncId, synchronization.getReport()));
        }
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

    @JsonGetter("report")
    public Relationship getReport() {
        return report;
    }

    static String createLink(int syncId) {
        return UriFactory.createUri(PATH + "/" + syncId);
    }
}
