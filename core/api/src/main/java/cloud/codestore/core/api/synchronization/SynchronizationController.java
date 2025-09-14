package cloud.codestore.core.api.synchronization;

import cloud.codestore.core.api.ErrorResponseBuilder;
import cloud.codestore.core.usecases.synchronizesnippets.ExecutedSynchronizations;
import cloud.codestore.core.usecases.synchronizesnippets.Synchronization;
import cloud.codestore.core.usecases.synchronizesnippets.SynchronizationNotExistsException;
import cloud.codestore.jsonapi.document.JsonApiDocument;
import cloud.codestore.jsonapi.error.ErrorObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = SynchronizationResource.PATH, produces = JsonApiDocument.MEDIA_TYPE)
class SynchronizationController {
    private final ExecutedSynchronizations executedSynchronizations;

    @Autowired
    SynchronizationController(ExecutedSynchronizations executedSynchronizations) {
        this.executedSynchronizations = executedSynchronizations;
    }

    @GetMapping("/{syncId}")
    public JsonApiDocument getSynchronization(
            @PathVariable("syncId") String syncIdParameter
    ) throws SynchronizationNotExistsException {
        int syncId = parseSynchronizationId(syncIdParameter);
        Synchronization synchronization = executedSynchronizations.get(syncId);
        return new SynchronizationResource(syncId, synchronization).asDocument();
    }

    @GetMapping("/{syncId}/report")
    public JsonApiDocument getReport(
            @PathVariable("syncId") String syncIdParameter
    ) throws SynchronizationNotExistsException {
        int syncId = parseSynchronizationId(syncIdParameter);
        Synchronization synchronization = executedSynchronizations.get(syncId);
        return new SynchronizationReportResource(syncId, synchronization.getReport()).asDocument();
    }

    private int parseSynchronizationId(String syncId) throws SynchronizationNotExistsException {
        try {
            return Integer.parseInt(syncId);
        } catch (NumberFormatException exception) {
            throw new SynchronizationNotExistsException();
        }
    }

    @ExceptionHandler(SynchronizationNotExistsException.class)
    public ResponseEntity<Object> nonExistingSynchronization() {
        ErrorResponseBuilder responseBuilder = new ErrorResponseBuilder();
        ErrorObject errorObject = responseBuilder.createError("NOT_FOUND", "notFound.title", "notFound.detail.synchronization");
        return responseBuilder.createResponse(HttpStatus.NOT_FOUND, errorObject);
    }
}
