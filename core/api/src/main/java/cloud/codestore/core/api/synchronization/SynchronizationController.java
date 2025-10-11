package cloud.codestore.core.api.synchronization;

import cloud.codestore.core.api.ErrorResponseBuilder;
import cloud.codestore.core.usecases.synchronizesnippets.SnippetSynchronizationState;
import cloud.codestore.core.usecases.synchronizesnippets.SnippetSynchronizations;
import cloud.codestore.core.usecases.synchronizesnippets.SynchronizationNotExistsException;
import cloud.codestore.core.usecases.synchronizesnippets.SynchronizationProcess;
import cloud.codestore.jsonapi.document.JsonApiDocument;
import cloud.codestore.jsonapi.error.ErrorObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = SynchronizationProcessResource.PATH, produces = JsonApiDocument.MEDIA_TYPE)
class SynchronizationController {
    private final SynchronizationProcess synchronizationProcess;
    private final SnippetSynchronizations executedSynchronizations;

    @Autowired
    SynchronizationController(
            SynchronizationProcess synchronizationProcess,
            SnippetSynchronizations executedSynchronizations
    ) {
        this.synchronizationProcess = synchronizationProcess;
        this.executedSynchronizations = executedSynchronizations;
    }

    @GetMapping("/1")
    public JsonApiDocument getInitialSynchronization() throws SynchronizationNotExistsException {
        if (synchronizationProcess.isSkipped()) {
            throw new SynchronizationNotExistsException();
        }

        return new SynchronizationProcessResource(
                synchronizationProcess.getState(),
                synchronizationProcess.getProgress()
        ).asDocument();
    }

    @GetMapping("/{snippetId}")
    public JsonApiDocument getSynchronization(
            @PathVariable("snippetId") String snippetId
    ) throws SynchronizationNotExistsException {
        SnippetSynchronizationState synchronization = executedSynchronizations.get(snippetId);
        return new SnippetSynchronizationResource(snippetId, synchronization).asDocument();
    }

    @ExceptionHandler(SynchronizationNotExistsException.class)
    public ResponseEntity<Object> nonExistingSynchronization() {
        ErrorResponseBuilder responseBuilder = new ErrorResponseBuilder();
        ErrorObject errorObject = responseBuilder.createError("NOT_FOUND", "notFound.title", "notFound.detail.synchronization");
        return responseBuilder.createResponse(HttpStatus.NOT_FOUND, errorObject);
    }
}
