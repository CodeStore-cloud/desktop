package cloud.codestore.core.api.synchronization;

import cloud.codestore.core.usecases.synchronizesnippets.SynchronizationReport;
import cloud.codestore.jsonapi.resource.ResourceObject;

class SynchronizationReportResource extends ResourceObject {
    private static final String RESOURCE_TYPE = "synchronizationReport";

    SynchronizationReportResource(int syncId, SynchronizationReport report) {
        super(RESOURCE_TYPE, String.valueOf(syncId));
        setSelfLink(createLink(syncId));
        // TODO show elements
    }

    private String createLink(int syncId) {
        return SynchronizationResource.createLink(syncId) + "/report";
    }
}
